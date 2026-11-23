package com.aminsula.pilot.sync;

import java.math.BigDecimal;

public class SourceProduct {

    private final String code;
    private final String name;
    private final BigDecimal price;
    private final BigDecimal stock;

    public SourceProduct(String code, String name, BigDecimal price, BigDecimal stock) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getStock() {
        return stock;
    }
}
