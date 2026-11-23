package com.aminsula.pilot.report;

import java.math.BigDecimal;

/** A customer in the period. */
public class ReportCustomerDto {

    private String code;
    private String name;
    private int orders;
    private BigDecimal value;

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
}
