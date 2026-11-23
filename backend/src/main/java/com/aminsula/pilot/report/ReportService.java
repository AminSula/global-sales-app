package com.aminsula.pilot.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aminsula.pilot.assignment.VisitAssignment;
import com.aminsula.pilot.assignment.VisitAssignmentRepository;
import com.aminsula.pilot.order.OrderStatus;
import com.aminsula.pilot.order.SalesOrder;
import com.aminsula.pilot.order.SalesOrderLine;
import com.aminsula.pilot.order.SalesOrderRepository;
import com.aminsula.pilot.user.AppUser;
import com.aminsula.pilot.user.AppUserRepository;
import com.aminsula.pilot.user.Role;

/**
 * Builds the reports page from the registered orders.
 **/
@Service
public class ReportService {

    private static final int MAX_DAYS = 366;
    private static final int TOP = 10;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final SalesOrderRepository orders;
    private final VisitAssignmentRepository assignments;
    private final AppUserRepository users;

    public ReportService(SalesOrderRepository orders, VisitAssignmentRepository assignments, AppUserRepository users) {
        this.orders = orders;
        this.assignments = assignments;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public ReportDto build(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'to' is before 'from'");
        }
        if (ChronoUnit.DAYS.between(from, to) >= MAX_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The period is longer than a year");
        }

        List<SalesOrder> period = orders.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        ReportDto report = new ReportDto();
        report.setFrom(from.toString());
        report.setTo(to.toString());

        // One entry per day of the period, so days without orders show as zero.
        Map<LocalDate, ReportDayDto> days = new LinkedHashMap<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            ReportDayDto dto = new ReportDayDto();
            dto.setDate(day.toString());
            dto.setValue(ZERO);
            days.put(day, dto);
        }

        // Every active agent appears, including those without orders.
        Map<Long, ReportAgentDto> agents = new LinkedHashMap<>();
        for (AppUser agent : users.findByRoleAndActiveTrueOrderByFullNameAsc(Role.AGENT)) {
            agents.put(agent.getId(), newAgent(agent.getFullName()));
        }

        Map<Long, ReportProductDto> products = new LinkedHashMap<>();
        Map<Long, ReportCustomerDto> customers = new LinkedHashMap<>();
        Map<String, ReportOfferDto> offers = new LinkedHashMap<>();
        // "agent|customer|day" for every order, to know which assigned visits produced an order.
        Set<String> orderedVisits = new HashSet<>();

        BigDecimal value = ZERO;
        BigDecimal pendingValue = ZERO;
        BigDecimal approvedValue = ZERO;
        BigDecimal gross = ZERO;
        int pending = 0;
        int approved = 0;

        for (SalesOrder order : period) {
            BigDecimal total = order.getTotal();
            value = value.add(total);
            if (order.getStatus() == OrderStatus.APPROVED) {
                approved++;
                approvedValue = approvedValue.add(total);
            } else {
                pending++;
                pendingValue = pendingValue.add(total);
            }

            LocalDate day = order.getCreatedAt().toLocalDate();
            ReportDayDto dayDto = days.get(day);
            if (dayDto != null) {
                dayDto.setOrders(dayDto.getOrders() + 1);
                dayDto.setValue(dayDto.getValue().add(total));
            }

            Long agentId = order.getAgent().getId();
            ReportAgentDto agentDto = agents.get(agentId);
            if (agentDto == null) {
                // An agent who has orders but is no longer active.
                agentDto = newAgent(order.getAgent().getFullName());
                agents.put(agentId, agentDto);
            }
            agentDto.setOrders(agentDto.getOrders() + 1);
            agentDto.setValue(agentDto.getValue().add(total));

            Long customerId = order.getCustomer().getId();
            ReportCustomerDto customerDto = customers.get(customerId);
            if (customerDto == null) {
                customerDto = new ReportCustomerDto();
                customerDto.setCode(order.getCustomer().getCode());
                customerDto.setName(order.getCustomer().getName());
                customerDto.setValue(ZERO);
                customers.put(customerId, customerDto);
            }
            customerDto.setOrders(customerDto.getOrders() + 1);
            customerDto.setValue(customerDto.getValue().add(total));

            orderedVisits.add(agentId + "|" + customerId + "|" + day);

            for (SalesOrderLine line : order.getLines()) {
                BigDecimal lineGross = line.getQuantity().multiply(line.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
                gross = gross.add(lineGross);

                Long productId = line.getProduct().getId();
                ReportProductDto productDto = products.get(productId);
                if (productDto == null) {
                    productDto = new ReportProductDto();
                    // The code and name as they were on the order.
                    productDto.setCode(line.getProductCode());
                    productDto.setName(line.getProductName());
                    productDto.setQuantity(ZERO);
                    productDto.setValue(ZERO);
                    products.put(productId, productDto);
                }
                productDto.setQuantity(productDto.getQuantity().add(line.getQuantity()));
                productDto.setValue(productDto.getValue().add(line.getLineTotal()));

                if (line.getOfferName() != null && line.getDiscountPercent().signum() > 0) {
                    ReportOfferDto offerDto = offers.get(line.getOfferName());
                    if (offerDto == null) {
                        offerDto = new ReportOfferDto();
                        offerDto.setOfferName(line.getOfferName());
                        offerDto.setDiscountValue(ZERO);
                        offerDto.setValue(ZERO);
                        offers.put(line.getOfferName(), offerDto);
                    }
                    offerDto.setLines(offerDto.getLines() + 1);
                    offerDto.setDiscountValue(offerDto.getDiscountValue().add(lineGross.subtract(line.getLineTotal())));
                    offerDto.setValue(offerDto.getValue().add(line.getLineTotal()));
                }
            }
        }

        // Assigned visits in the period, and how many of them produced an order.
        for (VisitAssignment visit : assignments.findByVisitDateBetween(from, to)) {
            Long agentId = visit.getAgent().getId();
            ReportAgentDto agentDto = agents.get(agentId);
            if (agentDto == null) {
                agentDto = newAgent(visit.getAgent().getFullName());
                agents.put(agentId, agentDto);
            }
            agentDto.setCustomersAssigned(agentDto.getCustomersAssigned() + 1);
            if (orderedVisits.contains(agentId + "|" + visit.getCustomer().getId() + "|" + visit.getVisitDate())) {
                agentDto.setCustomersOrdered(agentDto.getCustomersOrdered() + 1);
            }
        }

        report.setOrders(period.size());
        report.setValue(value);
        report.setPendingOrders(pending);
        report.setPendingValue(pendingValue);
        report.setApprovedOrders(approved);
        report.setApprovedValue(approvedValue);
        report.setGrossValue(gross);
        report.setDiscountValue(gross.subtract(value));
        report.setAverageOrder(period.isEmpty()
                ? ZERO
                : value.divide(BigDecimal.valueOf(period.size()), 2, RoundingMode.HALF_UP));

        report.getDays().addAll(days.values());

        List<ReportAgentDto> agentList = new ArrayList<>(agents.values());
        agentList.sort(Comparator.comparing(ReportAgentDto::getValue).reversed());
        report.getAgents().addAll(agentList);

        List<ReportProductDto> productList = new ArrayList<>(products.values());
        productList.sort(Comparator.comparing(ReportProductDto::getValue).reversed());
        report.getProducts().addAll(productList.subList(0, Math.min(TOP, productList.size())));

        List<ReportCustomerDto> customerList = new ArrayList<>(customers.values());
        customerList.sort(Comparator.comparing(ReportCustomerDto::getValue).reversed());
        report.getCustomers().addAll(customerList.subList(0, Math.min(TOP, customerList.size())));

        List<ReportOfferDto> offerList = new ArrayList<>(offers.values());
        offerList.sort(Comparator.comparing(ReportOfferDto::getDiscountValue).reversed());
        report.getOffers().addAll(offerList);

        return report;
    }

    private static ReportAgentDto newAgent(String name) {
        ReportAgentDto dto = new ReportAgentDto();
        dto.setAgentName(name);
        dto.setValue(ZERO);
        return dto;
    }
}
