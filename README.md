# Hospital Order Management — Backend

## Live Deployment

Backend API: https://hospital-order-backend.onrender.com

## Tech Stack

- Java 21
- Spring Boot 3.5.1
- Maven
- Docker
- JUnit 5 + Mockito

## Architecture — The Method (4-Layer)

```
Client (REST Controllers)
    └── Manager (OrderManager)
            ├── Engine (TriagingEngine)
            ├── Resource Access (OrderAccess)
            └── Utility (NotificationService)
```

## Design Patterns

| Pattern   | Class(es)                                      | Layer          |
|-----------|------------------------------------------------|----------------|
| Strategy  | TriageStrategy, PriorityTriageStrategy         | Engine         |
| Observer  | NotificationService, ConsoleNotificationService| Utility        |
| Decorator | OrderHandler, ValidationDecorator,             | Manager        |
|           | PriorityBoostingDecorator, AuditLoggingDecorator|               |
| Factory   | OrderFactory                                   | Manager        |
| Command   | Command, SubmitOrderCommand, ClaimOrderCommand,| Manager        |
|           | CompleteOrderCommand, CancelOrderCommand       |                |

## API Endpoints

| Method  | URL                              | Description              |
|---------|----------------------------------|--------------------------|
| POST    | /api/orders                      | Submit a new order       |
| GET     | /api/orders/queue                | Get sorted triage queue  |
| GET     | /api/orders                      | Get all orders           |
| GET     | /api/orders/{id}                 | Get order by ID          |
| GET     | /api/orders/status/{status}      | Filter by status         |
| PATCH   | /api/orders/{id}/cancel          | Cancel a pending order   |
| POST    | /api/fulfilment/claim            | Claim next order         |
| PATCH   | /api/fulfilment/{id}/complete    | Complete a claimed order |
| GET     | /api/audit                       | Get audit trail          |
| POST    | /api/audit/undo                  | Undo last command        |

## Run Locally

### With Maven

```bash
mvn spring-boot:run
```

### With Docker

```bash
docker build -t order-management .
docker run -p 8080:8080 order-management
```

Visit: http://localhost:8080/api/orders/queue

## Run Tests

```bash
mvn test
```

## Project Structure

```
src/main/java/com/healthcare/ordermanagement/
├── domain/          # Order, OrderType, Priority, OrderStatus
├── pattern/
│   ├── strategy/    # TriageStrategy interface + PriorityTriageStrategy
│   ├── factory/     # OrderFactory
│   ├── command/     # Command interface + 4 command classes + CommandLog
│   ├── observer/    # NotificationService interface + ConsoleNotificationService
│   └── decorator/   # OrderHandler + 3 decorators + OrderHandlerFactory
├── resource/        # OrderAccess interface + InMemoryOrderAccess
├── engine/          # TriagingEngine
├── manager/         # OrderManager
└── client/          # OrderController, FulfilmentController, AuditController
```

## Design Document

The full design document with diagrams is in **DesignDocument.pdf** in this repository.

Open it in a browser to view:

- Layered component diagram (all 6 layers)
- Call chain — Submit Order (all 5 patterns firing)
- Call chain — Fulfil Order (Claim + Complete)
- Design pattern justifications table
  