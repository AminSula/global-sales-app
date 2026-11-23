package com.aminsula.pilot.assignment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aminsula.pilot.catalog.Customer;
import com.aminsula.pilot.catalog.CustomerRepository;
import com.aminsula.pilot.user.AppUser;
import com.aminsula.pilot.user.AppUserRepository;
import com.aminsula.pilot.user.Role;

@Service
public class AssignmentService {

    private final VisitAssignmentRepository assignments;
    private final AppUserRepository users;
    private final CustomerRepository customers;

    public AssignmentService(VisitAssignmentRepository assignments,
                             AppUserRepository users,
                             CustomerRepository customers) {
        this.assignments = assignments;
        this.users = users;
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public List<AgentDto> agents() {
        List<AgentDto> result = new ArrayList<>();
        for (AppUser agent : users.findByRoleAndActiveTrueOrderByFullNameAsc(Role.AGENT)) {
            result.add(new AgentDto(agent.getId(), agent.getFullName()));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public AssignmentDayDto getDay(Long agentId, LocalDate date) {
        requireAgent(agentId);
        return buildDay(agentId, date);
    }

    /**
     * Replaces the agent's whole list for that day with the given customers, in the given order.
     * All or nothing: if any check fails, the previous list stays exactly as it was.
     */
    @Transactional
    public AssignmentDayDto saveDay(Long agentId, LocalDate date, List<Long> customerIds) {
        AppUser agent = requireAgent(agentId);

        if (customerIds == null || customerIds.contains(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerIds is required");
        }
        if (new HashSet<>(customerIds).size() != customerIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A customer appears twice");
        }

        // One agent per customer per day.
        Set<Long> takenByOthers = new HashSet<>();
        for (VisitAssignment other : assignments.findByVisitDateAndAgentIdNot(date, agentId)) {
            takenByOthers.add(other.getCustomer().getId());
        }
        for (Long customerId : customerIds) {
            if (takenByOthers.contains(customerId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A customer is already assigned to another agent on this day");
            }
        }

        Map<Long, Customer> byId = new HashMap<>();
        for (Customer customer : customers.findAllById(customerIds)) {
            byId.put(customer.getId(), customer);
        }

        assignments.deleteForDay(agentId, date);

        int order = 1;
        for (Long customerId : customerIds) {
            Customer customer = byId.get(customerId);
            if (customer == null || !customer.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown customer: " + customerId);
            }
            VisitAssignment assignment = new VisitAssignment();
            assignment.setAgent(agent);
            assignment.setCustomer(customer);
            assignment.setVisitDate(date);
            assignment.setVisitOrder(order++);
            assignments.save(assignment);
        }

        return buildDay(agentId, date);
    }

    private AssignmentDayDto buildDay(Long agentId, LocalDate date) {
        AssignmentDayDto day = new AssignmentDayDto();

        for (VisitAssignment assignment
                : assignments.findByAgentIdAndVisitDateOrderByVisitOrderAsc(agentId, date)) {
            Customer customer = assignment.getCustomer();
            AssignedCustomerDto dto = new AssignedCustomerDto();
            dto.setCustomerId(customer.getId());
            dto.setCode(customer.getCode());
            dto.setName(customer.getName());
            dto.setAddress(customer.getAddress());
            day.getAssigned().add(dto);
        }

        for (VisitAssignment other : assignments.findByVisitDateAndAgentIdNot(date, agentId)) {
            day.getTaken().add(new TakenCustomerDto(other.getCustomer().getId(), other.getAgent().getFullName()));
        }

        return day;
    }

    private AppUser requireAgent(Long agentId) {
        AppUser agent = users.findById(agentId).orElse(null);
        if (agent == null || agent.getRole() != Role.AGENT || !agent.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent not found");
        }
        return agent;
    }
}
