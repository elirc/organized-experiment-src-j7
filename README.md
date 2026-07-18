# Order Management System

A beginner-friendly command-line Order Management System written in plain Java 17. It manages customers, products, orders, discounts, and order status changes, and it saves data to local files so the app remembers its state between runs.

## Project Overview

This project is a small business-style CLI application. It is larger than a basic CRUD demo because orders have real rules:

- You cannot create an order for a missing customer.
- You cannot add an inactive product to an order.
- You cannot edit shipped or cancelled orders.
- You cannot move an empty order to `PAID` or `SHIPPED`.
- You cannot apply a discount that makes the total negative.

That makes it a good practice project because it teaches that business apps are not just "save some objects." They also contain workflows, validation, calculations, and state transitions.

## Project Structure

```text
src/
  oms/
    App.java
    domain/
      Customer.java
      Product.java
      Order.java
      OrderItem.java
      OrderStatus.java
    service/
      CustomerService.java
      ProductService.java
      OrderService.java
    persistence/
      FileDataStore.java
      DelimitedFileUtil.java
      StoreSnapshot.java
data/
  customers.txt
  products.txt
  orders.txt
  order_items.txt
```

## How To Compile And Run

Compile:

```powershell
javac -d out (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

Run:

```powershell
java -cp out oms.App
```

## File-By-File Explanation

### `src/oms/App.java`

This is the entry point of the program.

It does these jobs:

- starts the application
- loads saved data from disk
- shows the CLI menu
- reads user input
- calls the correct service method
- saves data when the user chooses save or exit

Important idea: `App` handles user interaction, but it does not contain the real business rules. That keeps it simpler and easier to read.

### `src/oms/domain/Customer.java`

Represents one customer.

Fields:

- `id`: unique stable ID
- `name`: customer name
- `email`: customer email
- `phone`: optional phone number
- `createdAt`: when the customer was created

This class is mostly a data holder.

### `src/oms/domain/Product.java`

Represents one product.

Fields:

- `id`: unique stable ID
- `name`: product name
- `sku`: stock keeping unit
- `price`: product price as `BigDecimal`
- `active`: whether the product can be sold

This class validates that price is greater than zero.

### `src/oms/domain/OrderItem.java`

Represents one line inside an order.

Fields:

- `productId`: which product this item comes from
- `productName`: snapshot of the product name when the item was added
- `quantity`: number of units
- `unitPrice`: price per unit at the moment it was added
- `lineTotal`: `quantity * unitPrice`

This is important because orders should keep their own pricing history. If the product changes later, old order items still keep the old unit price.

### `src/oms/domain/OrderStatus.java`

An enum for the order workflow.

Values:

- `DRAFT`
- `SUBMITTED`
- `PAID`
- `SHIPPED`
- `CANCELLED`

It also contains `canTransitionTo(...)`, which defines allowed state changes.

### `src/oms/domain/Order.java`

Represents one order.

Fields:

- `id`
- `customerId`
- `items`
- `status`
- `subtotal`
- `discountAmount`
- `total`
- `createdAt`
- `updatedAt`

Important behaviors:

- merge items when the same product is added again
- remove items
- set discount
- recalculate totals
- reject editing once the order is shipped or cancelled

This class owns the pricing numbers because they belong to the order.

### `src/oms/service/CustomerService.java`

Handles customer-related operations.

Main responsibilities:

- create customers
- validate required fields
- list customers
- find customer by ID

This keeps customer rules out of `App`.

### `src/oms/service/ProductService.java`

Handles product-related operations.

Main responsibilities:

- create products
- validate required fields
- list products
- find product by ID

### `src/oms/service/OrderService.java`

This is the most important service because orders have the most business rules.

Main responsibilities:

- create orders
- add products to orders
- remove products from orders
- apply discounts
- update order status
- enforce all order workflow rules

This is where most of the business logic lives.

### `src/oms/persistence/StoreSnapshot.java`

A simple holder object used to move lists of customers, products, and orders between the application and the file store.

### `src/oms/persistence/DelimitedFileUtil.java`

A helper for saving and loading text files with a `|` delimiter.

It escapes special characters so names and emails do not break the file format.

### `src/oms/persistence/FileDataStore.java`

Handles saving and loading all persistent data.

It stores data in simple text files:

- `customers.txt`
- `products.txt`
- `orders.txt`
- `order_items.txt`

This keeps persistence logic separate from domain and service logic.

## Class-By-Class Explanation

### `Customer`

Simple domain object for customer data.

### `Product`

Simple domain object for product data with price validation.

### `OrderItem`

Represents one line in an order and calculates its own `lineTotal`.

### `OrderStatus`

Represents the order workflow stages and valid transitions.

### `Order`

Represents the whole order and owns all order totals.

### `CustomerService`

Manages customer operations and lookup.

### `ProductService`

Manages product operations and lookup.

### `OrderService`

Manages order rules, calculations, and state changes.

### `FileDataStore`

Reads and writes app data to local text files.

### `DelimitedFileUtil`

Utility for string escaping and splitting lines safely.

### `StoreSnapshot`

Small transport object for loaded and saved data.

### `App`

Runs the whole program and connects menu actions to services.

## Method-By-Method Explanation

### `App`

- `main(...)`: starts the program
- `run()`: main menu loop
- `printMenu()`: prints menu choices
- `createCustomer()`: asks for customer input and creates one
- `listCustomers()`: prints customers
- `createProduct()`: asks for product input and creates one
- `listProducts()`: prints products
- `createOrder()`: creates an order for a customer
- `addItemToOrder()`: adds a product to an order
- `removeItemFromOrder()`: removes a product from an order
- `viewOrderDetails()`: prints one order in detail
- `listOrders()`: prints all orders
- `applyDiscount()`: applies a discount amount
- `updateOrderStatus()`: changes the order status if allowed
- `saveNow()`: saves data manually
- `exitApplication()`: saves data and exits
- `saveData()`: builds a snapshot and sends it to the file store
- `printOrder(...)`: prints an order summary or detail view
- `prompt(...)`: reads a line from the console
- `readPositiveInt(...)`: safely reads an integer
- `readMoney(...)`: safely reads a money value
- `readYesNo(...)`: reads a yes/no answer
- `formatMoney(...)`: prints money in 2-decimal format
- `formatTime(...)`: prints timestamps in a friendly format

### `CustomerService`

- `createCustomer(...)`: validates and creates a customer
- `listCustomers()`: returns all customers
- `findById(...)`: looks up one customer
- `all()`: returns the internal collection for saving

### `ProductService`

- `createProduct(...)`: validates and creates a product
- `listProducts()`: returns all products
- `findById(...)`: looks up one product
- `all()`: returns the internal collection for saving

### `OrderService`

- `createOrder(...)`: creates a draft order if the customer exists
- `addProductToOrder(...)`: validates product and quantity, then adds it
- `removeProductFromOrder(...)`: removes a product from an editable order
- `applyDiscount(...)`: validates the discount and applies it
- `updateStatus(...)`: checks status transition rules and empty-order rules
- `findById(...)`: looks up one order
- `listOrders()`: returns all orders
- `all()`: returns the internal collection for saving
- `requireOrder(...)`: helper that throws if an order is missing
- `ensureEditable(...)`: helper that blocks editing shipped/cancelled orders

### `Order`

- `addOrMergeItem(...)`: adds a new item or increases quantity if the product already exists
- `removeItemByProductId(...)`: removes an item from the order
- `addRestoredItem(...)`: used only when loading from disk
- `setDiscountAmount(...)`: changes the discount and recalculates totals
- `setStatus(...)`: changes the status and updates `updatedAt`
- `isEditable()`: returns whether the order can still be changed
- `hasItems()`: returns whether the order has at least one item
- `recalculateTotals()`: recalculates subtotal and total
- `normalizeMoney(...)`: forces money values to be non-negative and 2-decimal
- `touch()`: updates `updatedAt`

### `OrderItem`

- `increaseQuantity(...)`: adds more quantity to the same item
- `setQuantity(...)`: validates quantity and updates line total
- `setUnitPrice(...)`: validates unit price
- `recalculateLineTotal()`: recalculates line total

### `OrderStatus`

- `canTransitionTo(...)`: checks if a target status is allowed
- `allowedTransitions()`: returns the valid next states for each enum value

### `FileDataStore`

- `load()`: loads everything from disk
- `save(...)`: saves everything to disk
- `loadCustomers()`: loads customers from file
- `loadProducts()`: loads products from file
- `loadOrdersWithItems()`: loads orders, then loads items into them
- `saveCustomers(...)`: writes customers
- `saveProducts(...)`: writes products
- `saveOrders(...)`: writes orders and order items

### `DelimitedFileUtil`

- `join(...)`: builds one escaped line for a file
- `split(...)`: splits one saved line back into fields
- `escape(...)`: escapes special characters before saving

## How Customers, Products, Orders, And Order Items Relate

- One customer can have many orders.
- One order belongs to one customer.
- One order contains many order items.
- Each order item points back to one product through `productId`.
- Each order item also stores `productName` and `unitPrice` as snapshots.

That snapshot idea is important. If a product name or price changes later, existing orders should still show what was true when the order was created.

## How Totals Are Calculated

Each `OrderItem` calculates:

```text
lineTotal = quantity * unitPrice
```

Then `Order.recalculateTotals()` calculates:

```text
subtotal = sum of all line totals
total = subtotal - discountAmount
```

If the discount becomes larger than the subtotal, the order clamps the discount down to the subtotal so the total never becomes negative.

## How Status Transitions Are Enforced

The transition rules are inside `OrderStatus`.

Allowed transitions:

- `DRAFT -> SUBMITTED`
- `DRAFT -> CANCELLED`
- `SUBMITTED -> PAID`
- `SUBMITTED -> CANCELLED`
- `PAID -> SHIPPED`
- `PAID -> CANCELLED`

Examples of invalid transitions:

- `DRAFT -> SHIPPED`
- `SHIPPED -> DRAFT`
- `CANCELLED -> PAID`

`OrderService.updateStatus(...)` checks those rules before changing the order.

It also adds another rule:

- an empty order cannot become `PAID` or `SHIPPED`

## Why `BigDecimal` Is Used

Money should not be stored in `double`.

`double` uses floating-point math, which can create tiny precision errors like:

```text
0.1 + 0.2 != 0.3
```

`BigDecimal` is much safer for prices, discounts, subtotals, and totals because it stores decimal values more accurately.

That is why this project uses `BigDecimal` everywhere money is involved.

## How Saving And Loading Works

The app saves to local text files in the `data` folder.

- On startup, `App` creates `FileDataStore`.
- `FileDataStore.load()` checks whether the files exist.
- If they exist, it reads them and recreates the objects.
- If they do not exist, the app starts with empty lists.
- When you choose save or exit, `App` creates a `StoreSnapshot`.
- `FileDataStore.save(...)` writes the current state back to disk.

The files are plain text so you can open them and see the stored data directly.

## Why The Project Was Structured This Way

This structure tries to teach good habits without becoming too abstract.

- `domain` classes represent business concepts
- `service` classes contain business rules
- `persistence` classes handle saving and loading
- `App` handles the menu and user input

Why this is useful:

- the code is easier to read
- the business rules are not mixed with console code
- changes are easier to make later
- each class has a clearer responsibility

This is a simple version of separation of concerns.

## What A Beginner Should Study First

Study in this order:

1. `App.java` so you understand the menu flow
2. `OrderService.java` because most business rules live there
3. `Order.java` to see how totals are recalculated
4. `OrderStatus.java` to understand state transitions
5. `FileDataStore.java` to learn how persistence is separated
6. the other domain and service classes after that

## 5 Small Exercises To Extend The Project

1. Add a search option to find customers by email or name.
2. Prevent duplicate product SKUs.
3. Add an option to change a product's active status.
4. Add quantity update support instead of only add/remove.
5. Add a simple report that shows total sales from paid and shipped orders.

## Notes For Learning

- The code favors readability over advanced Java tricks.
- IDs use `UUID` so they stay unique without managing numeric counters.
- Persistence uses plain text files instead of a database to keep the project understandable.
- The app handles bad input by printing messages instead of crashing.
