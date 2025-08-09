#### Cart Service Microservice

A Spring Boot 3.x microservice that provides a RESTful API for a product cart, including price quotation, promotion application, and atomic inventory reservation.

#### Functional Overview

* **Product Management**: An API to create products with categories, prices, and stock.
* **Promotions Engine**: A pluggable promotions engine that supports different rule types (e.g., `PERCENT_OFF_CATEGORY`, `BUY_X_GET_Y`).
* **Cart Flow**:
    * `POST /cart/quote`: Calculates the total cost of a cart and applies promotions without reserving inventory.
    * `POST /cart/confirm`: Atomically decrements stock for products and generates an order. This endpoint is designed to be thread-safe and idempotent.

#### Assumptions

* **Java 17+** and **Gradle** are installed on your machine.
* The application runs with an in-memory **H2 database** by default.
* Tests assume an empty database for each run.

#### How to Run

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/ya6ets/cart-service.git
    cd cart-service
    ```
2.  **Build and Run the application:**
    ```bash
    ./gradlew bootRun
    ```
    The service will start on `http://localhost:8080`.

#### Sample API Requests

All requests should have the `Content-Type: application/json` header.

**1. Create Products (`POST /products`)**

This command creates two products: one for electronics and one for books.

```bash
curl -X POST http://localhost:8080/products \
-H 'Content-Type: application/json' \
-d '[
  {
    "name": "Wireless Mouse",
    "category": "ELECTRONICS",
    "price": 25.50,
    "stock": 50
  },
  {
    "name": "Java Programming Book",
    "category": "BOOKS",
    "price": 40.00,
    "stock": 20
  }
]'
```

**2. Create Promotions (`POST /promotions`)**

This creates a `PERCENT_OFF_CATEGORY` promotion for all `BOOKS` and a `BUY_X_GET_Y` promotion for the wireless mouse product (assuming its ID is `3b1c678a-c60f-48e0-a93d-d6a5e1a49f55`).

```bash
curl -X POST http://localhost:8080/promotions \
-H 'Content-Type: application/json' \
-d '[
  {
    "type": "PERCENT_OFF_CATEGORY",
    "name": "10% off Books",
    "rulesData": {
      "category": "BOOKS",
      "percentage": 10
    }
  },
  {
    "type": "BUY_X_GET_Y",
    "name": "Buy 2 Get 1 Free",
    "rulesData": {
      "productId": "3b1c678a-c60f-48e0-a93d-d6a5e1a49f55",
      "buyCount": 2,
      "getCount": 1
    }
  }
]'
```

**3. Get a Cart Quote (`POST /cart/quote`)**

This calculates the price for a cart with 3 books and 3 mice. The promotions created above will be applied.

```bash
curl -X POST http://localhost:8080/cart/quote \
-H 'Content-Type: application/json' \
-d '{
  "items": [
    {
      "productId": "7e3b5e4c-1e2b-4d5c-9c6a-4d7a4b8e2f0a",
      "qty": 3
    },
    {
      "productId": "3b1c678a-c60f-48e0-a93d-d6a5e1a49f55",
      "qty": 3
    }
  ],
  "customerSegment": "REGULAR"
}'
```

**4. Confirm a Cart (`POST /cart/confirm`)**

This command confirms the cart and reserves the inventory. Use an `Idempotency-Key` to prevent duplicate orders if the request is resent.

```bash
curl -X POST http://localhost:8080/cart/confirm \
-H 'Content-Type: application/json' \
-H 'Idempotency-Key: a1b2c3d4-e5f6-7890-1234-567890abcdef' \
-d '{
  "items": [
    {
      "productId": "7e3b5e4c-1e2b-4d5c-9c6a-4d7a4b8e2f0a",
      "qty": 3
    },
    {
      "productId": "3b1c678a-c60f-48e0-a93d-d6a5e1a49f55",
      "qty": 3
    }
  ],
  "customerSegment": "REGULAR"
}'
```

* **Expected Behavior for Concurrency**: If two `confirm` requests try to reserve the same product at the same time, one will succeed and the other will fail with a `409 CONFLICT` error.
* **Expected Behavior for Idempotency**: If you re-run the `confirm` command with the same `Idempotency-Key`, the service will not decrement stock again. Instead, it will return the same `orderId` from the first successful request.