package com.aminsula.pilot.assignment;

import java.util.ArrayList;
import java.util.List;

/** Everything the assignments screen needs for one agent and one day. */
public class AssignmentDayDto {

    private List<AssignedCustomerDto> assigned = new ArrayList<>();
    private List<TakenCustomerDto> taken = new ArrayList<>();

    public List<AssignedCustomerDto> getAssigned() {
        return assigned;
    }

    public void setAssigned(List<AssignedCustomerDto> assigned) {
        this.assigned = assigned;
    }

    public List<TakenCustomerDto> getTaken() {
        return taken;
    }

    public void setTaken(List<TakenCustomerDto> taken) {
        this.taken = taken;
    }
}
