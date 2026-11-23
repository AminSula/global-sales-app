package com.aminsula.pilot.catalog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByActiveTrueOrderByCodeAsc();
}
