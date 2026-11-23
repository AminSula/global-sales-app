package com.aminsula.pilot.order;

import java.math.BigDecimal;

/** A product as the agent sees it for ONE customer: price, stock and the winning offer (empty when none). */
public class OrderProductDto {

    private Long id;
    private String code;
    private String name;
    private BigDecimal price;
    private BigDecimal stock;
    private BigDecimal offerPercent;
    private String offerName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }

    public BigDecimal getOfferPercent() {
        return offerPercent;
    }

    public void setOfferPercent(BigDecimal offerPercent) {
        this.offerPercent = offerPercent;
    }

    public String getOfferName() {
        return offerName;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }
}
