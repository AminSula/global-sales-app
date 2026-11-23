package com.aminsula.pilot.offer;

import java.math.BigDecimal;

/** The offer that wins for one product: its name and percentage. */
public class AppliedOffer {

    private final String name;
    private final BigDecimal percent;

    public AppliedOffer(String name, BigDecimal percent) {
        this.name = name;
        this.percent = percent;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPercent() {
        return percent;
    }
}
