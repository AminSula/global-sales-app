package com.aminsula.pilot.report;

import java.math.BigDecimal;

/** Orders on one day (date is yyyy-MM-dd). Days without orders are included with zeros. */
public class ReportDayDto {

    private String date;
    private int orders;
    private BigDecimal value;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
