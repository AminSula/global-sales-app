package com.aminsula.pilot.assignment;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin: which customers each agent visits on a day, and in what order. Dates are yyyy-MM-dd. */
@RestController
@RequestMapping("/api/admin")
public class AssignmentController {

    private final AssignmentService service;

    public AssignmentController(AssignmentService service) {
        this.service = service;
    }

    @GetMapping("/agents")
    public List<AgentDto> agents() {
        return service.agents();
    }

    @GetMapping("/assignments")
    public AssignmentDayDto day(
            @RequestParam("agentId") Long agentId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getDay(agentId, date);
    }

    @PutMapping("/assignments")
    public AssignmentDayDto save(
            @RequestParam("agentId") Long agentId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody SaveAssignmentsRequest request) {
        return service.saveDay(agentId, date, request.getCustomerIds());
    }
}
