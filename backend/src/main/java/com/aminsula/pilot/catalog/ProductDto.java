package com.aminsula.pilot.catalog;

import java.math.BigDecimal;

public class ProductDto {

    private Long id;
    private String code;
    private String name;
    private BigDecimal price;
    private BigDecimal stock;
    private String syncedAt;
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

    /** Today's best offer that applies to ALL customers (empty when there is none). */
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

    public String getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(String syncedAt) {
        this.syncedAt = syncedAt;
    }
}
