package com.aminsula.pilot.assignment;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitAssignmentRepository extends JpaRepository<VisitAssignment, Long> {

    /** An agent's customers for a day, in visit order. */
    List<VisitAssignment> findByAgentIdAndVisitDateOrderByVisitOrderAsc(Long agentId, LocalDate visitDate);

    /** Customers that OTHER agents have on that day. */
    List<VisitAssignment> findByVisitDateAndAgentIdNot(LocalDate visitDate, Long agentId);

    /** Every assignment in a period (both dates included). Used by the reports. */
    List<VisitAssignment> findByVisitDateBetween(LocalDate from, LocalDate to);

    /** Is this customer on this agent's list for that day? */
    boolean existsByAgentIdAndCustomerIdAndVisitDate(Long agentId, Long customerId, LocalDate visitDate);

    @Modifying
    @Query("delete from VisitAssignment v where v.agent.id = :agentId and v.visitDate = :visitDate")
    void deleteForDay(@Param("agentId") Long agentId, @Param("visitDate") LocalDate visitDate);
}
