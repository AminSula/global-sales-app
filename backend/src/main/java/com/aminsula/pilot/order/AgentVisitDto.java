package com.aminsula.pilot.order;

/** One customer in the agent's list for today. */
public class AgentVisitDto {

    private Long customerId;
    private String code;
    private String name;
    private String address;
    private int visitOrder;
    private long ordersToday;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getVisitOrder() {
        return visitOrder;
    }

    public void setVisitOrder(int visitOrder) {
        this.visitOrder = visitOrder;
    }

    public long getOrdersToday() {
        return ordersToday;
    }

    public void setOrdersToday(long ordersToday) {
        this.ordersToday = ordersToday;
    }
}
