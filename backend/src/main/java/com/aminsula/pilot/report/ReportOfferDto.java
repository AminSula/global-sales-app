package com.aminsula.pilot.report;

import java.math.BigDecimal;

/** An offer in the period: how many order lines used it, the discount it gave and the sales value of those lines. */
public class ReportOfferDto {

    private String offerName;
    private int lines;
    private BigDecimal discountValue;
    private BigDecimal value;

    public String getOfferName() {
        return offerName;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
