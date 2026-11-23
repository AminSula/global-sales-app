package com.aminsula.pilot.assignment;

import java.time.LocalDate;

import com.aminsula.pilot.catalog.Customer;
import com.aminsula.pilot.user.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One customer an agent visits on one day, with its position in the day's order. */
@Entity
@Table(name = "visit_assignment")
public class VisitAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    private AppUser agent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "visit_order", nullable = false)
    private int visitOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getAgent() {
        return agent;
    }

    public void setAgent(AppUser agent) {
        this.agent = agent;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public int getVisitOrder() {
        return visitOrder;
    }

    public void setVisitOrder(int visitOrder) {
        this.visitOrder = visitOrder;
    }
}
