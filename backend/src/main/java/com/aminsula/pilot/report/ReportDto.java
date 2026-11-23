package com.aminsula.pilot.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** The whole reports page for one period. value = after discounts; grossValue = before discounts. */
public class ReportDto {

    private String from;
    private String to;
    private int orders;
    private BigDecimal value;
    private int pendingOrders;
    private BigDecimal pendingValue;
    private int approvedOrders;
    private BigDecimal approvedValue;
    private BigDecimal grossValue;
    private BigDecimal discountValue;
    private BigDecimal averageOrder;
    private List<ReportDayDto> days = new ArrayList<>();
    private List<ReportAgentDto> agents = new ArrayList<>();
    private List<ReportProductDto> products = new ArrayList<>();
    private List<ReportCustomerDto> customers = new ArrayList<>();
    private List<ReportOfferDto> offers = new ArrayList<>();

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public int getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(int pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public BigDecimal getPendingValue() {
        return pendingValue;
    }

    public void setPendingValue(BigDecimal pendingValue) {
        this.pendingValue = pendingValue;
    }

    public int getApprovedOrders() {
        return approvedOrders;
    }

    public void setApprovedOrders(int approvedOrders) {
        this.approvedOrders = approvedOrders;
    }

    public BigDecimal getApprovedValue() {
        return approvedValue;
    }

    public void setApprovedValue(BigDecimal approvedValue) {
        this.approvedValue = approvedValue;
    }

    public BigDecimal getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(BigDecimal grossValue) {
        this.grossValue = grossValue;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getAverageOrder() {
        return averageOrder;
    }

    public void setAverageOrder(BigDecimal averageOrder) {
        this.averageOrder = averageOrder;
    }

    public List<ReportDayDto> getDays() {
        return days;
    }

    public void setDays(List<ReportDayDto> days) {
        this.days = days;
    }

    public List<ReportAgentDto> getAgents() {
        return agents;
    }

    public void setAgents(List<ReportAgentDto> agents) {
        this.agents = agents;
    }

    public List<ReportProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<ReportProductDto> products) {
        this.products = products;
    }

    public List<ReportCustomerDto> getCustomers() {
        return customers;
    }

    public void setCustomers(List<ReportCustomerDto> customers) {
        this.customers = customers;
    }

    public List<ReportOfferDto> getOffers() {
        return offers;
    }

    public void setOffers(List<ReportOfferDto> offers) {
        this.offers = offers;
    }
}
