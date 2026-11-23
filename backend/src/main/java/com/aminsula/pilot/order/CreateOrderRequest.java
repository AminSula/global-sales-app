package com.aminsula.pilot.order;

import java.math.BigDecimal;
import java.util.List;

/** An order sent from the phone. clientRequestId is created once per order on the phone and makes re-sending safe; expectedTotal is the total the agent saw. */
public class CreateOrderRequest {

    private String clientRequestId;
    private Long customerId;
    private List<CreateOrderLine> lines;
    private BigDecimal expectedTotal;

    public String getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<CreateOrderLine> getLines() {
        return lines;
    }

    public void setLines(List<CreateOrderLine> lines) {
        this.lines = lines;
    }

    public BigDecimal getExpectedTotal() {
        return expectedTotal;
    }

    public void setExpectedTotal(BigDecimal expectedTotal) {
        this.expectedTotal = expectedTotal;
    }
}
