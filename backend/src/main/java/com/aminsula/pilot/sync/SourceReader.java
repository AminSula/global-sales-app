package com.aminsula.pilot.sync;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SourceReader {

    private final JdbcTemplate jdbc;
    private final String productsQuery;
    private final String customersQuery;

    public SourceReader(
            JdbcTemplate jdbc,
            @Value("${app.source.products-query:SELECT code, name, price, stock FROM erp_products}")
            String productsQuery,
            @Value("${app.source.customers-query:SELECT code, name, address FROM erp_customers}")
            String customersQuery) {
        this.jdbc = jdbc;
        this.productsQuery = productsQuery;
        this.customersQuery = customersQuery;
    }

    public List<SourceProduct> readProducts() {
        return jdbc.query(productsQuery, (rs, rowNum) -> new SourceProduct(
                rs.getString("code"),
                rs.getString("name"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("stock")));
    }

    public List<SourceCustomer> readCustomers() {
        return jdbc.query(customersQuery, (rs, rowNum) -> new SourceCustomer(
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("address")));
    }
}
