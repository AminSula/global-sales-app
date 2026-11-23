package com.aminsula.pilot.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.aminsula.pilot.order.OrderDto;

/** The admin's Overview page: today at a glance. pending = the newest orders waiting for approval. */
public class OverviewDto {

    private int ordersToday;
    private BigDecimal valueToday;
    private BigDecimal averageToday;
    private int pendingOrders;
    private BigDecimal pendingValue;
    private int visitsAssigned;
    private int visitsOrdered;
    private int activeOffers;
    private int products;
    private int customers;
    private String lastSyncedAt;
    private List<OrderDto> pending = new ArrayList<>();
    private List<ReportAgentDto> agents = new ArrayList<>();

    public int getOrdersToday() {
        return ordersToday;
    }

    public void setOrdersToday(int ordersToday) {
        this.ordersToday = ordersToday;
    }

    public BigDecimal getValueToday() {
        return valueToday;
    }

    public void setValueToday(BigDecimal valueToday) {
        this.valueToday = valueToday;
    }

    public BigDecimal getAverageToday() {
        return averageToday;
    }

    public void setAverageToday(BigDecimal averageToday) {
        this.averageToday = averageToday;
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

    public int getVisitsAssigned() {
        return visitsAssigned;
    }

    public void setVisitsAssigned(int visitsAssigned) {
        this.visitsAssigned = visitsAssigned;
    }

    public int getVisitsOrdered() {
        return visitsOrdered;
    }

    public void setVisitsOrdered(int visitsOrdered) {
        this.visitsOrdered = visitsOrdered;
    }

    public int getActiveOffers() {
        return activeOffers;
    }

    public void setActiveOffers(int activeOffers) {
        this.activeOffers = activeOffers;
    }

    public int getProducts() {
        return products;
    }

    public void setProducts(int products) {
        this.products = products;
    }

    public int getCustomers() {
        return customers;
    }

    public void setCustomers(int customers) {
        this.customers = customers;
    }

    public String getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(String lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public List<OrderDto> getPending() {
        return pending;
    }

    public void setPending(List<OrderDto> pending) {
        this.pending = pending;
    }

    public List<ReportAgentDto> getAgents() {
        return agents;
    }

    public void setAgents(List<ReportAgentDto> agents) {
        this.agents = agents;
    }
}
