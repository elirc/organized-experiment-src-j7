package oms.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Product {
    private final String id;
    private final String name;
    private final String sku;
    private final BigDecimal price;
    private final boolean active;

    public Product(String id, String name, String sku, BigDecimal price, boolean active) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.sku = Objects.requireNonNull(sku, "sku must not be null");
        this.price = normalizePrice(price);
        this.active = active;
    }

    private BigDecimal normalizePrice(BigDecimal value) {
        Objects.requireNonNull(value, "price must not be null");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero.");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }
}
