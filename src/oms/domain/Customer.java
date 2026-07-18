package oms.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Customer {
    private final String id;
    private final String name;
    private final String email;
    private final String phone;
    private final LocalDateTime createdAt;

    public Customer(String id, String name, String email, String phone, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.phone = phone == null ? "" : phone;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
