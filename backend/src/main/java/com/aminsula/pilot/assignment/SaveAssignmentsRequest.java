package com.aminsula.pilot.assignment;

import java.util.List;

public class SaveAssignmentsRequest {

    /** Customer ids in visit order: the first is visited first. */
    private List<Long> customerIds;

    public List<Long> getCustomerIds() {
        return customerIds;
    }

    public void setCustomerIds(List<Long> customerIds) {
        this.customerIds = customerIds;
    }
}
