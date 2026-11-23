package com.aminsula.pilot.order;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aminsula.pilot.security.AuthUser;

/** Admin: see every order and mark it approved. */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService service;

    public AdminOrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderDto> list() {
        return service.allOrders();
    }

    @PostMapping("/{id}/approve")
    public OrderDto approve(@AuthenticationPrincipal AuthUser admin, @PathVariable("id") Long id) {
        return service.approve(admin, id);
    }
}
