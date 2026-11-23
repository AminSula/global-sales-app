package com.aminsula.pilot.report;

import java.math.BigDecimal;

/** A product in the period: quantity sold and value after discounts. */
public class ReportProductDto {

    private String code;
    private String name;
    private BigDecimal quantity;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
