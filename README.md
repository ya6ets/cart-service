# Cart Service

This is a robust and scalable e-commerce cart service built with Spring Boot. It provides core functionality for quoting cart prices, applying promotions, and confirming orders with a focus on concurrency, idempotency, and clean architecture.

## 🌟 Key Features

* **RESTful API**: Provides endpoints for managing products, promotions, and handling the cart lifecycle (quoting and confirming).
* **Concurrency Control**: Utilizes **optimistic locking** (`@Version`) on the `Product` entity to prevent race conditions during stock updates.
* **Idempotency**: Supports the `Idempotency-Key` header to ensure that a `confirm` request can be safely retried without creating duplicate orders or double-decrementing stock.
* **Pluggable Promotion Engine**: A flexible **Chain of Responsibility** and **Strategy** pattern-based promotion engine allows for easy addition of new promotion types without modifying core business logic.
* **Custom Exception Handling**: A centralized `@ControllerAdvice` provides structured, readable error responses for validation failures (`400 Bad Request`), resource not found (`404 Not Found`), and stock conflicts (`409 Conflict`).
* **In-Memory Database**: Uses H2 database for a lightweight and easy-to-run development environment.
* **Comprehensive Testing**: Includes both unit tests (`@WebMvcTest`) for isolating controller logic and integration tests (`@SpringBootTest`) for verifying end-to-end functionality, including concurrency and idempotency.

## 🚀 Getting Started

### Prerequisites

* Java 17+
* Gradle
* Docker (optional, for running with Testcontainers in a more realistic environment)

### Building the Project

Clone the repository and build the project using Gradle:

```bash
git clone https://github.com/ya6ets/cart-service.git
cd cart-service
gradle wrapper --gradle-version 8.5
./gradlew build
```

### Running the Application

You can run the application directly from the command line:

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

## 📖 API Endpoints

The API is structured around products, promotions, and the cart functionality.

### Products

**`POST /products`**
Creates one or more products.

* **Request Body**:
  ```json
  [
    {
      "name": "E-Reader",
      "category": "ELECTRONICS",
      "price": 129.99,
      "stock": 50
    },
    {
      "name": "The Pragmatic Programmer",
      "category": "BOOKS",
      "price": 45.00,
      "stock": 100
    }
  ]
  ```

### Promotions

**`POST /promotions`**
Creates one or more promotions.

* **Request Body**:
  ```json
  [
    {
      "type": "PERCENT_OFF_CATEGORY",
      "name": "10% Off Books",
      "rulesData": {
        "rule_key": "category",
        "rule_value": "BOOKS"
      },
      "rulesData": {
        "rule_key": "percentage",
        "rule_value": "10"
      }
    }
  ]
  ```

### Cart

**`POST /cart/quote`**
Calculates the total price of the items in the cart, including any applicable promotions, without changing stock levels.

* **Request Body**:
  ```json
  {
    "items": [
      {
        "productId": "...",
        "qty": 1
      }
    ]
  }
  ```

**`POST /cart/confirm`**
Finalizes the purchase, decrements stock, and creates a new order. Use the `Idempotency-Key` header to prevent duplicate orders.

* **Request Body**:
  ```json
  {
    "items": [
      {
        "productId": "...",
        "qty": 1
      }
    ]
  }
  ```
* **Request Headers**:
    * `Idempotency-Key`: A unique string for each transaction (e.g., a UUID).

## 🧪 Testing

The project uses JUnit 5 for testing. You can run all tests with the following command:

```bash
./gradlew test
```

This will run both the fast unit tests and the more involved integration tests that verify concurrent and idempotent behavior.

## 🤝 Contributing

Feel free to submit issues or pull requests. All feedback is welcome.