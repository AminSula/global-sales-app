package com.aminsula.pilot.report;

import java.math.BigDecimal;

/** One agent in the period. customersOrdered = assigned visits on which the agent sent at least one order for that customer that day. */
public class ReportAgentDto {

    private String agentName;
    private int orders;
    private BigDecimal value;
    private int customersAssigned;
    private int customersOrdered;

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
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

    public int getCustomersAssigned() {
        return customersAssigned;
    }

    public void setCustomersAssigned(int customersAssigned) {
        this.customersAssigned = customersAssigned;
    }

    public int getCustomersOrdered() {
        return customersOrdered;
    }

    public void setCustomersOrdered(int customersOrdered) {
        this.customersOrdered = customersOrdered;
    }
}
