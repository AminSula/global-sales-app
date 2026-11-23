package com.aminsula.pilot.assignment;

/** A customer that another agent already has on that day. */
public class TakenCustomerDto {

    private Long customerId;
    private String agentName;

    public TakenCustomerDto() {
    }

    public TakenCustomerDto(Long customerId, String agentName) {
        this.customerId = customerId;
        this.agentName = agentName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
}
