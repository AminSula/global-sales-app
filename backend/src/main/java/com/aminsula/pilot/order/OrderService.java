package com.aminsula.pilot.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aminsula.pilot.assignment.VisitAssignment;
import com.aminsula.pilot.assignment.VisitAssignmentRepository;
import com.aminsula.pilot.catalog.Customer;
import com.aminsula.pilot.catalog.CustomerRepository;
import com.aminsula.pilot.catalog.Product;
import com.aminsula.pilot.catalog.ProductRepository;
import com.aminsula.pilot.offer.AppliedOffer;
import com.aminsula.pilot.offer.OfferService;
import com.aminsula.pilot.security.AuthUser;
import com.aminsula.pilot.user.AppUserRepository;

/**
 * Orders. For agents: today's customers, the products for one customer, sending an order and
 * their own orders; those methods take the agent from the login token (AuthUser), so an agent
 * can only ever reach their own customers and orders. For the administrator: all orders and approval.
 */
@Service
public class OrderService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final SalesOrderRepository orders;
    private final VisitAssignmentRepository assignments;
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final AppUserRepository users;
    private final OfferService offerService;

    public OrderService(SalesOrderRepository orders,
                        VisitAssignmentRepository assignments,
                        ProductRepository products,
                        CustomerRepository customers,
                        AppUserRepository users,
                        OfferService offerService) {
        this.orders = orders;
        this.assignments = assignments;
        this.products = products;
        this.customers = customers;
        this.users = users;
        this.offerService = offerService;
    }

    // ---------- The one formula for money ----------

    /**
     * Line total = quantity x unit price x (100 - discount %) / 100, rounded to 2 decimals (half up).
     * The order total is the sum of the line totals. The phone shows the same formula,
     * but only this server-side result is ever saved.
     */
    static BigDecimal lineTotal(BigDecimal quantity, BigDecimal unitPrice, BigDecimal discountPercent) {
        return quantity
                .multiply(unitPrice)
                .multiply(HUNDRED.subtract(discountPercent))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    // ---------- Reading ----------

    @Transactional(readOnly = true)
    public AgentDayDto today(AuthUser agent) {
        LocalDate today = LocalDate.now();
        AgentDayDto day = new AgentDayDto();
        day.setDate(today.toString());

        for (VisitAssignment assignment
                : assignments.findByAgentIdAndVisitDateOrderByVisitOrderAsc(agent.getId(), today)) {
            Customer customer = assignment.getCustomer();
            AgentVisitDto visit = new AgentVisitDto();
            visit.setCustomerId(customer.getId());
            visit.setCode(customer.getCode());
            visit.setName(customer.getName());
            visit.setAddress(customer.getAddress());
            visit.setVisitOrder(assignment.getVisitOrder());
            visit.setOrdersToday(orders.countByAgentIdAndCustomerIdAndCreatedAtGreaterThanEqual(
                    agent.getId(), customer.getId(), today.atStartOfDay()));
            day.getVisits().add(visit);
        }
        return day;
    }

    /** Products with the price, stock and winning offer for this customer. */
    @Transactional(readOnly = true)
    public List<OrderProductDto> catalog(AuthUser agent, Long customerId) {
        LocalDate today = LocalDate.now();
        requireAssignedToday(agent, customerId, today);

        Map<Long, AppliedOffer> offers = offerService.bestOffers(customerId, today);
        List<OrderProductDto> result = new ArrayList<>();
        for (Product product : products.findByActiveTrueOrderByCodeAsc()) {
            OrderProductDto dto = new OrderProductDto();
            dto.setId(product.getId());
            dto.setCode(product.getCode());
            dto.setName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setStock(product.getStock());
            AppliedOffer offer = offers.get(product.getId());
            if (offer != null) {
                dto.setOfferPercent(offer.getPercent());
                dto.setOfferName(offer.getName());
            }
            result.add(dto);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> ownOrders(AuthUser agent) {
        List<OrderDto> result = new ArrayList<>();
        for (SalesOrder order : orders.findTop100ByAgentIdOrderByCreatedAtDescIdDesc(agent.getId())) {
            result.add(toDto(order));
        }
        return result;
    }

    /** The agent's order with this request id, or null. Used when a repeated send hits the database's UNIQUE rule. */
    @Transactional(readOnly = true)
    public OrderDto findOwnByRequestId(AuthUser agent, String clientRequestId) {
        SalesOrder order = orders.findByClientRequestId(clientRequestId).orElse(null);
        if (order == null || !order.getAgent().getId().equals(agent.getId())) {
            return null;
        }
        return toDto(order);
    }

    // ---------- Admin: all orders and approval ----------

    @Transactional(readOnly = true)
    public List<OrderDto> allOrders() {
        List<OrderDto> result = new ArrayList<>();
        for (SalesOrder order : orders.findTop500ByOrderByCreatedAtDescIdDesc()) {
            result.add(toDto(order));
        }
        return result;
    }

    /**
     * Marks an order "E miratuar" and records who did it and when.
     * Approval is final. Approving an already approved order changes nothing and returns it as it is,
     * so a repeated click is harmless.
     */
    @Transactional
    public OrderDto approve(AuthUser admin, Long orderId) {
        SalesOrder order = orders.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.APPROVED);
            order.setApprovedAt(LocalDateTime.now());
            order.setApprovedBy(users.getReferenceById(admin.getId()));
        }
        return toDto(order);
    }

    // ---------- Sending an order ----------

    /**
     * Saves an order sent from the phone. Answers:
     *   200 + the order  saved now, OR already saved earlier with the same clientRequestId (never twice)
     *   400              the request itself is wrong (no lines, bad quantity...)
     *   403              the customer is not on this agent's list today
     *   409              a product, price or offer changed since the phone loaded them
     *                    (the total differs from what the agent saw): nothing is saved, the phone reloads
     */
    @Transactional
    public OrderDto create(AuthUser agent, CreateOrderRequest request) {
        String requestId = request.getClientRequestId() == null ? "" : request.getClientRequestId().trim();
        if (requestId.isEmpty() || requestId.length() > 36) {
            throw invalid("clientRequestId is required (max 36 characters)");
        }

        // Sent before (double tap, or a retry after a lost connection)? Give back the saved order.
        SalesOrder existing = orders.findByClientRequestId(requestId).orElse(null);
        if (existing != null) {
            if (!existing.getAgent().getId().equals(agent.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Request id already used");
            }
            return toDto(existing);
        }

        if (request.getCustomerId() == null) {
            throw invalid("customerId is required");
        }
        LocalDate today = LocalDate.now();
        requireAssignedToday(agent, request.getCustomerId(), today);
        Customer customer = customers.findById(request.getCustomerId())
                .orElseThrow(() -> invalid("Unknown customer"));

        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw invalid("The order has no lines");
        }

        Map<Long, AppliedOffer> offers = offerService.bestOffers(customer.getId(), today);

        SalesOrder order = new SalesOrder();
        order.setClientRequestId(requestId);
        // Temporary unique value; replaced below once the database has given the order its id.
        order.setOrderNumber("T-" + requestId.substring(0, Math.min(18, requestId.length())));
        order.setCustomer(customer);
        order.setAgent(users.getReferenceById(agent.getId()));
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO.setScale(2);
        Set<Long> seenProducts = new HashSet<>();
        int lineNo = 1;

        for (CreateOrderLine requested : request.getLines()) {
            if (requested == null || requested.getProductId() == null || requested.getQuantity() == null) {
                throw invalid("Each line needs a product and a quantity");
            }
            BigDecimal quantity = requested.getQuantity();
            if (quantity.signum() <= 0 || quantity.stripTrailingZeros().scale() > 2) {
                throw invalid("Quantity must be above 0, with at most 2 decimals");
            }
            if (!seenProducts.add(requested.getProductId())) {
                throw invalid("A product appears twice");
            }

            Product product = products.findById(requested.getProductId()).orElse(null);
            if (product == null || !product.isActive()) {
                throw changed("A product is no longer available");
            }

            AppliedOffer offer = offers.get(product.getId());
            BigDecimal discount = offer == null ? BigDecimal.ZERO.setScale(2) : offer.getPercent();

            SalesOrderLine line = new SalesOrderLine();
            line.setOrder(order);
            line.setLineNo(lineNo++);
            line.setProduct(product);
            line.setProductCode(product.getCode());
            line.setProductName(product.getName());
            line.setQuantity(quantity.setScale(2, RoundingMode.UNNECESSARY));
            line.setUnitPrice(product.getPrice());
            line.setDiscountPercent(discount);
            line.setOfferName(offer == null ? null : offer.getName());
            line.setLineTotal(lineTotal(quantity, product.getPrice(), discount));
            order.getLines().add(line);

            total = total.add(line.getLineTotal());
        }
        order.setTotal(total);

        // The agent must never send one total and have another one saved.
        if (request.getExpectedTotal() != null && request.getExpectedTotal().compareTo(total) != 0) {
            throw changed("Prices or offers changed");
        }

        order = orders.saveAndFlush(order);
        order.setOrderNumber(String.format("POR-%06d", order.getId()));
        return toDto(order);
    }

    // ---------- Helpers ----------

    private void requireAssignedToday(AuthUser agent, Long customerId, LocalDate today) {
        if (!assignments.existsByAgentIdAndCustomerIdAndVisitDate(agent.getId(), customerId, today)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer is not assigned to you today");
        }
    }

    private static ResponseStatusException invalid(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private static ResponseStatusException changed(String reason) {
        return new ResponseStatusException(HttpStatus.CONFLICT, reason);
    }

    public static OrderDto toDto(SalesOrder order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setCustomerCode(order.getCustomer().getCode());
        dto.setCustomerName(order.getCustomer().getName());
        dto.setAgentName(order.getAgent().getFullName());
        dto.setCreatedAt(toIso(order.getCreatedAt()));
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus());
        dto.setApprovedAt(toIso(order.getApprovedAt()));
        dto.setApprovedByName(order.getApprovedBy() == null ? null : order.getApprovedBy().getFullName());
        for (SalesOrderLine line : order.getLines()) {
            OrderLineDto lineDto = new OrderLineDto();
            lineDto.setLineNo(line.getLineNo());
            lineDto.setProductCode(line.getProductCode());
            lineDto.setProductName(line.getProductName());
            lineDto.setQuantity(line.getQuantity());
            lineDto.setUnitPrice(line.getUnitPrice());
            lineDto.setDiscountPercent(line.getDiscountPercent());
            lineDto.setOfferName(line.getOfferName());
            lineDto.setLineTotal(line.getLineTotal());
            dto.getLines().add(lineDto);
        }
        return dto;
    }

    /** Adds the server's time zone, so the phone shows the right local time. */
    private static String toIso(LocalDateTime value) {
        return value == null ? null : value.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString();
    }
}
