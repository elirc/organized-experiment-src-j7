package oms.persistence;

import oms.domain.Customer;
import oms.domain.Order;
import oms.domain.Product;

import java.util.List;

public class StoreSnapshot {
    private final List<Customer> customers;
    private final List<Product> products;
    private final List<Order> orders;

    public StoreSnapshot(List<Customer> customers, List<Product> products, List<Order> orders) {
        this.customers = customers;
        this.products = products;
        this.orders = orders;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
