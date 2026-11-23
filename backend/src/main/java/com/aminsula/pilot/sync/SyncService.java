package com.aminsula.pilot.sync;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aminsula.pilot.catalog.Customer;
import com.aminsula.pilot.catalog.CustomerRepository;
import com.aminsula.pilot.catalog.Product;
import com.aminsula.pilot.catalog.ProductRepository;

@Service
public class SyncService {

    private final SourceReader source;
    private final ProductRepository products;
    private final CustomerRepository customers;

    public SyncService(SourceReader source, ProductRepository products, CustomerRepository customers) {
        this.source = source;
        this.products = products;
        this.customers = customers;
    }

    @Transactional
    public SyncResult refresh() {
        LocalDateTime now = LocalDateTime.now();
        SyncResult result = new SyncResult();
        refreshProducts(now, result);
        refreshCustomers(now, result);
        return result;
    }

    private void refreshProducts(LocalDateTime now, SyncResult result) {
        Map<String, Product> existing = new HashMap<>();
        for (Product product : products.findAll()) {
            existing.put(product.getCode(), product);
        }

        Set<String> seen = new HashSet<>();
        int created = 0;
        int changed = 0;

        for (SourceProduct row : source.readProducts()) {
            String code = clean(row.getCode());
            // Skip rows without a code and repeated codes.
            if (code.isEmpty() || !seen.add(code)) {
                continue;
            }
            String name = clean(row.getName());
            BigDecimal price = money(row.getPrice());
            BigDecimal stock = money(row.getStock());

            Product product = existing.get(code);
            if (product == null) {
                product = new Product();
                product.setCode(code);
                created++;
            } else if (!product.isActive()
                    || !name.equals(product.getName())
                    || price.compareTo(product.getPrice()) != 0
                    || stock.compareTo(product.getStock()) != 0) {
                changed++;
            }

            product.setName(name);
            product.setPrice(price);
            product.setStock(stock);
            product.setActive(true);
            product.setSyncedAt(now);
            products.save(product);
        }

        int removed = 0;
        for (Product product : existing.values()) {
            if (product.isActive() && !seen.contains(product.getCode())) {
                product.setActive(false);
                products.save(product);
                removed++;
            }
        }

        result.setProducts(seen.size());
        result.setProductsNew(created);
        result.setProductsChanged(changed);
        result.setProductsRemoved(removed);
    }

    private void refreshCustomers(LocalDateTime now, SyncResult result) {
        Map<String, Customer> existing = new HashMap<>();
        for (Customer customer : customers.findAll()) {
            existing.put(customer.getCode(), customer);
        }

        Set<String> seen = new HashSet<>();
        int created = 0;
        int changed = 0;

        for (SourceCustomer row : source.readCustomers()) {
            String code = clean(row.getCode());
            if (code.isEmpty() || !seen.add(code)) {
                continue;
            }
            String name = clean(row.getName());
            String address = row.getAddress() == null ? null : row.getAddress().trim();

            Customer customer = existing.get(code);
            if (customer == null) {
                customer = new Customer();
                customer.setCode(code);
                created++;
            } else if (!customer.isActive()
                    || !name.equals(customer.getName())
                    || !Objects.equals(address, customer.getAddress())) {
                changed++;
            }

            customer.setName(name);
            customer.setAddress(address);
            customer.setActive(true);
            customer.setSyncedAt(now);
            customers.save(customer);
        }

        int removed = 0;
        for (Customer customer : existing.values()) {
            if (customer.isActive() && !seen.contains(customer.getCode())) {
                customer.setActive(false);
                customers.save(customer);
                removed++;
            }
        }

        result.setCustomers(seen.size());
        result.setCustomersNew(created);
        result.setCustomersChanged(changed);
        result.setCustomersRemoved(removed);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
