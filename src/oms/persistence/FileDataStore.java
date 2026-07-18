package oms.persistence;

import oms.domain.Customer;
import oms.domain.Order;
import oms.domain.OrderItem;
import oms.domain.OrderStatus;
import oms.domain.Product;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileDataStore {
    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String PRODUCTS_FILE = "products.txt";
    private static final String ORDERS_FILE = "orders.txt";
    private static final String ORDER_ITEMS_FILE = "order_items.txt";

    private final Path dataDirectory;

    public FileDataStore(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public StoreSnapshot load() {
        try {
            Files.createDirectories(dataDirectory);
            List<Customer> customers = loadCustomers();
            List<Product> products = loadProducts();
            List<Order> orders = loadOrdersWithItems();
            return new StoreSnapshot(customers, products, orders);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data from disk.", e);
        }
    }

    public void save(StoreSnapshot snapshot) {
        try {
            Files.createDirectories(dataDirectory);
            saveCustomers(snapshot.getCustomers());
            saveProducts(snapshot.getProducts());
            saveOrders(snapshot.getOrders());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save data to disk.", e);
        }
    }

    private List<Customer> loadCustomers() throws IOException {
        Path file = dataDirectory.resolve(CUSTOMERS_FILE);
        List<Customer> customers = new ArrayList<>();
        if (!Files.exists(file)) {
            return customers;
        }

        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            List<String> parts = DelimitedFileUtil.split(line);
            customers.add(new Customer(
                    parts.get(0),
                    parts.get(1),
                    parts.get(2),
                    parts.get(3),
                    LocalDateTime.parse(parts.get(4))));
        }
        return customers;
    }

    private List<Product> loadProducts() throws IOException {
        Path file = dataDirectory.resolve(PRODUCTS_FILE);
        List<Product> products = new ArrayList<>();
        if (!Files.exists(file)) {
            return products;
        }

        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            List<String> parts = DelimitedFileUtil.split(line);
            products.add(new Product(
                    parts.get(0),
                    parts.get(1),
                    parts.get(2),
                    new BigDecimal(parts.get(3)),
                    Boolean.parseBoolean(parts.get(4))));
        }
        return products;
    }

    private List<Order> loadOrdersWithItems() throws IOException {
        Path ordersFile = dataDirectory.resolve(ORDERS_FILE);
        Path orderItemsFile = dataDirectory.resolve(ORDER_ITEMS_FILE);
        Map<String, Order> ordersById = new LinkedHashMap<>();

        if (Files.exists(ordersFile)) {
            for (String line : Files.readAllLines(ordersFile, StandardCharsets.UTF_8)) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                List<String> parts = DelimitedFileUtil.split(line);
                Order order = new Order(
                        parts.get(0),
                        parts.get(1),
                        OrderStatus.valueOf(parts.get(2)),
                        new BigDecimal(parts.get(3)),
                        LocalDateTime.parse(parts.get(4)),
                        LocalDateTime.parse(parts.get(5)));
                ordersById.put(order.getId(), order);
            }
        }

        if (Files.exists(orderItemsFile)) {
            for (String line : Files.readAllLines(orderItemsFile, StandardCharsets.UTF_8)) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                List<String> parts = DelimitedFileUtil.split(line);
                String orderId = parts.get(0);
                Order order = ordersById.get(orderId);
                if (order == null) {
                    continue;
                }
                OrderItem item = new OrderItem(
                        parts.get(1),
                        parts.get(2),
                        Integer.parseInt(parts.get(3)),
                        new BigDecimal(parts.get(4)));
                order.addRestoredItem(item);
            }
        }

        return new ArrayList<>(ordersById.values());
    }

    private void saveCustomers(List<Customer> customers) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# id|name|email|phone|createdAt");
        for (Customer customer : customers) {
            lines.add(DelimitedFileUtil.join(
                    customer.getId(),
                    customer.getName(),
                    customer.getEmail(),
                    customer.getPhone(),
                    customer.getCreatedAt().toString()));
        }
        Files.write(dataDirectory.resolve(CUSTOMERS_FILE), lines, StandardCharsets.UTF_8);
    }

    private void saveProducts(List<Product> products) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# id|name|sku|price|active");
        for (Product product : products) {
            lines.add(DelimitedFileUtil.join(
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    product.getPrice().toPlainString(),
                    String.valueOf(product.isActive())));
        }
        Files.write(dataDirectory.resolve(PRODUCTS_FILE), lines, StandardCharsets.UTF_8);
    }

    private void saveOrders(List<Order> orders) throws IOException {
        List<String> orderLines = new ArrayList<>();
        List<String> itemLines = new ArrayList<>();
        orderLines.add("# id|customerId|status|discountAmount|createdAt|updatedAt");
        itemLines.add("# orderId|productId|productName|quantity|unitPrice");

        for (Order order : orders) {
            orderLines.add(DelimitedFileUtil.join(
                    order.getId(),
                    order.getCustomerId(),
                    order.getStatus().name(),
                    order.getDiscountAmount().toPlainString(),
                    order.getCreatedAt().toString(),
                    order.getUpdatedAt().toString()));

            for (OrderItem item : order.getItems()) {
                itemLines.add(DelimitedFileUtil.join(
                        order.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        String.valueOf(item.getQuantity()),
                        item.getUnitPrice().toPlainString()));
            }
        }

        Files.write(dataDirectory.resolve(ORDERS_FILE), orderLines, StandardCharsets.UTF_8);
        Files.write(dataDirectory.resolve(ORDER_ITEMS_FILE), itemLines, StandardCharsets.UTF_8);
    }
}
