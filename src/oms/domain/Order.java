package oms.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {
    private final String id;
    private final String customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order(String id, String customerId, LocalDateTime createdAt) {
        this(id, customerId, OrderStatus.DRAFT, BigDecimal.ZERO, createdAt, createdAt);
    }

    public Order(
            String id,
            String customerId,
            OrderStatus status,
            BigDecimal discountAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.discountAmount = normalizeMoney(discountAmount);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.items = new ArrayList<>();
        this.subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        recalculateTotals();
    }

    public void addOrMergeItem(OrderItem newItem) {
        for (OrderItem existingItem : items) {
            if (existingItem.getProductId().equals(newItem.getProductId())) {
                existingItem.increaseQuantity(newItem.getQuantity());
                touch();
                recalculateTotals();
                return;
            }
        }
        items.add(newItem);
        touch();
        recalculateTotals();
    }

    public boolean removeItemByProductId(String productId) {
        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));
        if (removed) {
            touch();
            recalculateTotals();
        }
        return removed;
    }

    public void addRestoredItem(OrderItem item) {
        items.add(item);
        recalculateTotals();
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = normalizeMoney(discountAmount);
        touch();
        recalculateTotals();
    }

    public void setStatus(OrderStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
        touch();
    }

    public boolean isEditable() {
        return status != OrderStatus.SHIPPED && status != OrderStatus.CANCELLED;
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    public void recalculateTotals() {
        subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (OrderItem item : items) {
            subtotal = subtotal.add(item.getLineTotal());
        }

        if (discountAmount.compareTo(subtotal) > 0) {
            discountAmount = subtotal;
        }

        total = subtotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        Objects.requireNonNull(value, "money value must not be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money values cannot be negative.");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private void touch() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
