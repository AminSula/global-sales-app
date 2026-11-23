package com.aminsula.pilot.offer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** What the admin panel sends to create or edit an offer. Dates are yyyy-MM-dd or empty. */
public class OfferRequest {

    private String name;
    private BigDecimal discountPercent;
    private boolean allCustomers;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean enabled;
    private List<Long> productIds;
    private List<Long> customerIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public boolean isAllCustomers() {
        return allCustomers;
    }

    public void setAllCustomers(boolean allCustomers) {
        this.allCustomers = allCustomers;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public List<Long> getCustomerIds() {
        return customerIds;
    }

    public void setCustomerIds(List<Long> customerIds) {
        this.customerIds = customerIds;
    }
}
