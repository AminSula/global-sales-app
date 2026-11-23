package com.aminsula.pilot.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findByClientRequestId(String clientRequestId);

    /** An agent's own orders, newest first. */
    List<SalesOrder> findTop100ByAgentIdOrderByCreatedAtDescIdDesc(Long agentId);

    /** All orders for the admin panel, newest first. */
    List<SalesOrder> findTop500ByOrderByCreatedAtDescIdDesc();

    /** Orders created in [from, to), oldest first. Used by the reports. */
    List<SalesOrder> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
            LocalDateTime from, LocalDateTime to);

    /** Orders with one status, newest first. */
    List<SalesOrder> findByStatusOrderByCreatedAtDescIdDesc(OrderStatus status);

    long countByAgentIdAndCustomerIdAndCreatedAtGreaterThanEqual(Long agentId, Long customerId, LocalDateTime from);
}
