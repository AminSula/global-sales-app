package com.aminsula.pilot.offer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aminsula.pilot.catalog.Customer;
import com.aminsula.pilot.catalog.CustomerRepository;
import com.aminsula.pilot.catalog.Product;
import com.aminsula.pilot.catalog.ProductRepository;

@Service
public class OfferService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final OfferRepository offers;
    private final ProductRepository products;
    private final CustomerRepository customers;

    public OfferService(OfferRepository offers, ProductRepository products, CustomerRepository customers) {
        this.offers = offers;
        this.products = products;
        this.customers = customers;
    }

    // ---------- The one rule that decides discounts ----------

    /**
     * The winning offer for each product, for one customer on one day.
     *
     * Rule: among the offers that are switched on, valid on that day and apply to that customer,
     * the HIGHEST percentage wins. Offers never add up. Products without an offer are not in the map.
     *
     * Pass customerId = null to get only the offers that apply to all customers.
     * Order calculation (phone and server) must always go through this method.
     */
    @Transactional(readOnly = true)
    public Map<Long, AppliedOffer> bestOffers(Long customerId, LocalDate date) {
        Map<Long, AppliedOffer> best = new HashMap<>();

        for (Offer offer : offers.findByEnabledTrueOrderByIdAsc()) {
            if (statusOn(offer, date) != OfferStatus.ACTIVE || !appliesTo(offer, customerId)) {
                continue;
            }
            for (Product product : offer.getProducts()) {
                AppliedOffer current = best.get(product.getId());
                if (current == null || offer.getDiscountPercent().compareTo(current.getPercent()) > 0) {
                    best.put(product.getId(), new AppliedOffer(offer.getName(), offer.getDiscountPercent()));
                }
            }
        }
        return best;
    }

    private static boolean appliesTo(Offer offer, Long customerId) {
        if (offer.isAllCustomers()) {
            return true;
        }
        if (customerId == null) {
            return false;
        }
        for (Customer customer : offer.getCustomers()) {
            if (customerId.equals(customer.getId())) {
                return true;
            }
        }
        return false;
    }

    static OfferStatus statusOn(Offer offer, LocalDate date) {
        if (!offer.isEnabled()) {
            return OfferStatus.PAUSED;
        }
        if (offer.getStartDate() != null && date.isBefore(offer.getStartDate())) {
            return OfferStatus.SCHEDULED;
        }
        if (offer.getEndDate() != null && date.isAfter(offer.getEndDate())) {
            return OfferStatus.EXPIRED;
        }
        return OfferStatus.ACTIVE;
    }

    // ---------- Admin: list, create, edit, delete ----------

    @Transactional(readOnly = true)
    public List<OfferDto> list() {
        LocalDate today = LocalDate.now();
        List<OfferDto> result = new ArrayList<>();
        for (Offer offer : offers.findAllByOrderByCreatedAtDescIdDesc()) {
            result.add(toDto(offer, today));
        }
        return result;
    }

    @Transactional
    public OfferDto create(OfferRequest request) {
        Offer offer = new Offer();
        offer.setCreatedAt(LocalDateTime.now());
        apply(offer, request);
        return toDto(offers.save(offer), LocalDate.now());
    }

    @Transactional
    public OfferDto update(Long id, OfferRequest request) {
        Offer offer = offers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        apply(offer, request);
        return toDto(offers.save(offer), LocalDate.now());
    }

    /** Orders already registered keep their own copy of the discount, so deleting is safe. */
    @Transactional
    public void delete(Long id) {
        Offer offer = offers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        offers.delete(offer);
    }

    /** Checks the request and copies it onto the offer. Throws 400 with the reason when invalid. */
    private void apply(Offer offer, OfferRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isEmpty() || name.length() > 100) {
            throw invalid("Name is required (max 100 characters)");
        }

        BigDecimal percent = request.getDiscountPercent();
        if (percent == null) {
            throw invalid("Discount percent is required");
        }
        percent = percent.setScale(2, RoundingMode.HALF_UP);
        if (percent.signum() <= 0 || percent.compareTo(HUNDRED) > 0) {
            throw invalid("Discount percent must be above 0 and at most 100");
        }

        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw invalid("End date is before start date");
        }

        Set<Long> productIds = distinct(request.getProductIds());
        if (productIds.isEmpty()) {
            throw invalid("Choose at least one product");
        }
        List<Product> chosenProducts = products.findAllById(productIds);
        if (chosenProducts.size() != productIds.size()) {
            throw invalid("Unknown product");
        }

        List<Customer> chosenCustomers = Collections.emptyList();
        if (!request.isAllCustomers()) {
            Set<Long> customerIds = distinct(request.getCustomerIds());
            if (customerIds.isEmpty()) {
                throw invalid("Choose at least one customer, or all customers");
            }
            chosenCustomers = customers.findAllById(customerIds);
            if (chosenCustomers.size() != customerIds.size()) {
                throw invalid("Unknown customer");
            }
        }

        offer.setName(name);
        offer.setDiscountPercent(percent);
        offer.setAllCustomers(request.isAllCustomers());
        offer.setStartDate(request.getStartDate());
        offer.setEndDate(request.getEndDate());
        offer.setEnabled(request.isEnabled());

        offer.getProducts().clear();
        offer.getProducts().addAll(chosenProducts);
        offer.getCustomers().clear();
        offer.getCustomers().addAll(chosenCustomers);
    }

    private static Set<Long> distinct(List<Long> ids) {
        Set<Long> result = new HashSet<>();
        if (ids != null) {
            for (Long id : ids) {
                if (id != null) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    private static ResponseStatusException invalid(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private static OfferDto toDto(Offer offer, LocalDate today) {
        OfferDto dto = new OfferDto();
        dto.setId(offer.getId());
        dto.setName(offer.getName());
        dto.setDiscountPercent(offer.getDiscountPercent());
        dto.setAllCustomers(offer.isAllCustomers());
        dto.setStartDate(offer.getStartDate());
        dto.setEndDate(offer.getEndDate());
        dto.setEnabled(offer.isEnabled());
        dto.setStatus(statusOn(offer, today));
        for (Product product : offer.getProducts()) {
            dto.getProductIds().add(product.getId());
        }
        for (Customer customer : offer.getCustomers()) {
            dto.getCustomerIds().add(customer.getId());
        }
        return dto;
    }
}
