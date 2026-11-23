package com.aminsula.pilot.report;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin: the Overview page. */
@RestController
@RequestMapping("/api/admin/overview")
public class OverviewController {

    private final OverviewService service;

    public OverviewController(OverviewService service) {
        this.service = service;
    }

    @GetMapping
    public OverviewDto overview() {
        return service.build();
    }
}
