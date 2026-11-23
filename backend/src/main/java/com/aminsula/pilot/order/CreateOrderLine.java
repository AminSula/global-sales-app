package com.aminsula.pilot.order;

import java.math.BigDecimal;

/** One line of an order sent from the phone. Prices are never sent: the server uses its own. */
public class CreateOrderLine {

    private Long productId;
    private BigDecimal quantity;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
