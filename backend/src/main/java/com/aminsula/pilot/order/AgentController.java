package com.aminsula.pilot.order;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aminsula.pilot.security.AuthUser;

/** The phone's API. Only agents can call it, and the agent always comes from the login token. */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final OrderService service;

    public AgentController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/visits")
    public AgentDayDto today(@AuthenticationPrincipal AuthUser agent) {
        return service.today(agent);
    }

    @GetMapping("/customers/{customerId}/catalog")
    public List<OrderProductDto> catalog(@AuthenticationPrincipal AuthUser agent,
                                         @PathVariable("customerId") Long customerId) {
        return service.catalog(agent, customerId);
    }

    @GetMapping("/orders")
    public List<OrderDto> orders(@AuthenticationPrincipal AuthUser agent) {
        return service.ownOrders(agent);
    }

    @PostMapping("/orders")
    public OrderDto create(@AuthenticationPrincipal AuthUser agent, @RequestBody CreateOrderRequest request) {
        try {
            return service.create(agent, request);
        } catch (DataIntegrityViolationException e) {
            // Two identical sends arrived at the same moment: the database's UNIQUE rule on
            // client_request_id let only one through. Answer the other with that saved order.
            OrderDto saved = request.getClientRequestId() == null
                    ? null
                    : service.findOwnByRequestId(agent, request.getClientRequestId().trim());
            if (saved != null) {
                return saved;
            }
            throw e;
        }
    }
}
