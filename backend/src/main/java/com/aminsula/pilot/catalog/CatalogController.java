package com.aminsula.pilot.catalog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aminsula.pilot.offer.AppliedOffer;
import com.aminsula.pilot.offer.OfferService;

/** Products and customers as shown in the admin panel. */
@RestController
@RequestMapping("/api/admin")
public class CatalogController {

    private final ProductRepository products;
    private final CustomerRepository customers;
    private final OfferService offerService;

    public CatalogController(ProductRepository products,
                             CustomerRepository customers,
                             OfferService offerService) {
        this.products = products;
        this.customers = customers;
        this.offerService = offerService;
    }

    @GetMapping("/products")
    public List<ProductDto> products() {
        // Offers for all customers, valid today (customer-specific offers are shown on the Offers page).
        Map<Long, AppliedOffer> offers = offerService.bestOffers(null, LocalDate.now());

        List<ProductDto> result = new ArrayList<>();
        for (Product product : products.findByActiveTrueOrderByCodeAsc()) {
            ProductDto dto = new ProductDto();
            dto.setId(product.getId());
            dto.setCode(product.getCode());
            dto.setName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setStock(product.getStock());
            dto.setSyncedAt(toIso(product.getSyncedAt()));
            AppliedOffer offer = offers.get(product.getId());
            if (offer != null) {
                dto.setOfferPercent(offer.getPercent());
                dto.setOfferName(offer.getName());
            }
            result.add(dto);
        }
        return result;
    }

    @GetMapping("/customers")
    public List<CustomerDto> customers() {
        List<CustomerDto> result = new ArrayList<>();
        for (Customer customer : customers.findByActiveTrueOrderByCodeAsc()) {
            CustomerDto dto = new CustomerDto();
            dto.setId(customer.getId());
            dto.setCode(customer.getCode());
            dto.setName(customer.getName());
            dto.setAddress(customer.getAddress());
            dto.setSyncedAt(toIso(customer.getSyncedAt()));
            result.add(dto);
        }
        return result;
    }

    /** Adds the server's time zone, so the browser shows the right local time. */
    private static String toIso(LocalDateTime value) {
        return value.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString();
    }
}
