package com.aminsula.pilot.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aminsula.pilot.catalog.CustomerRepository;
import com.aminsula.pilot.catalog.Product;
import com.aminsula.pilot.catalog.ProductRepository;
import com.aminsula.pilot.offer.OfferDto;
import com.aminsula.pilot.offer.OfferService;
import com.aminsula.pilot.offer.OfferStatus;
import com.aminsula.pilot.order.OrderService;
import com.aminsula.pilot.order.OrderStatus;
import com.aminsula.pilot.order.SalesOrder;
import com.aminsula.pilot.order.SalesOrderRepository;

/** Builds the admin's Overview page: today's numbers, what is waiting, and how fresh the data is. */
@Service
public class OverviewService {

    private static final int PENDING_SHOWN = 5;

    private final ReportService reports;
    private final SalesOrderRepository orders;
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final OfferService offers;

    public OverviewService(ReportService reports,
                           SalesOrderRepository orders,
                           ProductRepository products,
                           CustomerRepository customers,
                           OfferService offers) {
        this.reports = reports;
        this.orders = orders;
        this.products = products;
        this.customers = customers;
        this.offers = offers;
    }

    @Transactional(readOnly = true)
    public OverviewDto build() {
        LocalDate today = LocalDate.now();
        OverviewDto overview = new OverviewDto();

        // Today's figures are simply the report for a one-day period.
        ReportDto todayReport = reports.build(today, today);
        overview.setOrdersToday(todayReport.getOrders());
        overview.setValueToday(todayReport.getValue());
        overview.setAverageToday(todayReport.getAverageOrder());
        overview.getAgents().addAll(todayReport.getAgents());

        int assigned = 0;
        int ordered = 0;
        for (ReportAgentDto agent : todayReport.getAgents()) {
            assigned += agent.getCustomersAssigned();
            ordered += agent.getCustomersOrdered();
        }
        overview.setVisitsAssigned(assigned);
        overview.setVisitsOrdered(ordered);

        // Everything still waiting for approval, whatever day it was sent.
        List<SalesOrder> pending = orders.findByStatusOrderByCreatedAtDescIdDesc(OrderStatus.PENDING);
        BigDecimal pendingValue = BigDecimal.ZERO.setScale(2);
        for (SalesOrder order : pending) {
            pendingValue = pendingValue.add(order.getTotal());
        }
        overview.setPendingOrders(pending.size());
        overview.setPendingValue(pendingValue);
        for (SalesOrder order : pending.subList(0, Math.min(PENDING_SHOWN, pending.size()))) {
            overview.getPending().add(OrderService.toDto(order));
        }

        int activeOffers = 0;
        for (OfferDto offer : offers.list()) {
            if (offer.getStatus() == OfferStatus.ACTIVE) {
                activeOffers++;
            }
        }
        overview.setActiveOffers(activeOffers);

        List<Product> activeProducts = products.findByActiveTrueOrderByCodeAsc();
        overview.setProducts(activeProducts.size());
        overview.setCustomers(customers.findByActiveTrueOrderByCodeAsc().size());

        LocalDateTime lastSync = null;
        for (Product product : activeProducts) {
            if (lastSync == null || product.getSyncedAt().isAfter(lastSync)) {
                lastSync = product.getSyncedAt();
            }
        }
        overview.setLastSyncedAt(lastSync == null
                ? null
                : lastSync.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString());

        return overview;
    }
}
