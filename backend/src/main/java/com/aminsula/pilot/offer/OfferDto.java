package com.aminsula.pilot.offer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OfferDto {

    private Long id;
    private String name;
    private BigDecimal discountPercent;
    private boolean allCustomers;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean enabled;
    private OfferStatus status;
    private List<Long> productIds = new ArrayList<>();
    private List<Long> customerIds = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
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
