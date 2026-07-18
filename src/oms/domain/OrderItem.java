package oms.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class OrderItem {
    private final String productId;
    private final String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public OrderItem(String productId, String productName, int quantity, BigDecimal unitPrice) {
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.productName = Objects.requireNonNull(productName, "productName must not be null");
        setQuantity(quantity);
        setUnitPrice(unitPrice);
        recalculateLineTotal();
    }

    public void increaseQuantity(int additionalQuantity) {
        setQuantity(this.quantity + additionalQuantity);
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
        recalculateLineTotal();
    }

    private void setUnitPrice(BigDecimal unitPrice) {
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero.");
        }
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    private void recalculateLineTotal() {
        if (unitPrice == null) {
            return;
        }
        lineTotal = unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
