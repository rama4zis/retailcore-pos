# RetailCore POS

RetailCore POS is a mini point-of-sale system for small retail stores. The goal is to build a production-style backend first, then add the frontend after the API is stable.

## Project Goal

Build a POS system with:

- Product and category management
- Inventory stock tracking
- Sales checkout
- Payment tracking
- Receipt generation
- User authentication and role-based authorization
- Basic reporting dashboard

## Tech Stack

### Backend

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Security
- JWT authentication
- PostgreSQL
- Flyway database migrations
- Maven

### Frontend

Planned later:

- React
- Vite
- Tailwind CSS
- React Router
- TanStack Query
- Axios

### Testing

- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers later for PostgreSQL integration tests

### Deployment

- Docker Compose
- PostgreSQL container first
- Full backend/frontend deployment later

## Current Status Checklist

### Already Done

- [x] Repository created
- [x] Project specification created in `docs/project-spec.md`
- [x] Backend Spring Boot project created under `backend/`
- [x] Java version set to 21
- [x] Spring Boot 3.x selected
- [x] Basic backend dependencies added:
  - [x] Spring Web
  - [x] Spring Data JPA
  - [x] Spring Validation
  - [x] Spring Actuator
  - [x] PostgreSQL driver
  - [x] Lombok
  - [x] Spring Boot Test
- [x] Basic PostgreSQL datasource config added in `backend/src/main/resources/application.yaml`
- [x] Actuator health/info endpoint exposure configured

### Not Done Yet

- [x] Add Docker Compose for PostgreSQL
- [x] Add Flyway dependency
- [x] Add first database migration
- [x] Add Spring Security dependency
- [x] Add JWT dependency
- [x] Add OpenAPI/Swagger dependency
- [x] Add Testcontainers dependency
- [x] Verify backend starts against local PostgreSQL
- [ ] Build category module
- [ ] Build product module
- [ ] Build inventory module
- [ ] Build authentication module
- [ ] Build sales checkout module
- [ ] Build payment handling
- [ ] Build receipt response
- [ ] Build reporting endpoints
- [ ] Build frontend

## Main Roles

| Role | Access |
|---|---|
| Admin | Full system access, user management, reports |
| Manager | Product, inventory, sales, reports |
| Cashier | Checkout and receipt operations |

## Main Modules

1. Authentication and authorization
2. User management
3. Category management
4. Product management
5. Inventory management
6. Sales and checkout
7. Payment management
8. Receipt generation
9. Reporting dashboard
10. Frontend dashboard
11. Dockerized deployment

## Business Rules

- SKU must be unique.
- Barcode must be unique when provided.
- Stock cannot become negative.
- Every stock change must create a stock movement record.
- Sale item price must be copied at sale time.
- Completed sales must not be edited.
- Refunds must not exceed the original sale amount.
- Passwords must be hashed with BCrypt.
- Product records should be disabled instead of hard-deleted after sales exist.
- Category records should not be deleted while products still use them.

## Recommended Build Order

This is the current implementation path.

### Phase 0 — Project Baseline

- [x] Create Spring Boot backend
- [x] Configure Java 21
- [x] Configure PostgreSQL datasource
- [x] Fix local database startup using Docker Compose
- [x] Add Flyway migrations
- [x] Add missing dependencies
- [x] Verify app startup

### Phase 1 — Category Module

- [x] Create `category` package
- [x] Create `Category` entity
- [x] Create Flyway migration for `categories` table
- [x] Create `CategoryRepository`
- [x] Create request/response DTOs
- [x] Create `CategoryService`
- [x] Create `CategoryController`
- [x] Add validation
- [x] Add duplicate-name protection
- [x] Add delete protection when products exist
- [x] Add tests
- [x] Manually test API endpoints

Endpoints planned:

```http
POST   /api/categories
GET    /api/categories
GET    /api/categories/{id}
PUT    /api/categories/{id}
DELETE /api/categories/{id}
```

### Phase 2 — Product Module

- [ ] Create `product` package
- [ ] Create `Product` entity
- [ ] Create Flyway migration for `products` table
- [ ] Create `ProductRepository`
- [ ] Create product DTOs
- [ ] Create `ProductService`
- [ ] Create `ProductController`
- [ ] Add SKU uniqueness rule
- [ ] Add barcode uniqueness rule
- [ ] Add positive price validation
- [ ] Add active/inactive product status
- [ ] Add tests
- [ ] Manually test API endpoints

Endpoints planned:

```http
POST   /api/products
GET    /api/products
GET    /api/products/{id}
PUT    /api/products/{id}
PATCH  /api/products/{id}/active
```

### Phase 3 — Inventory Module

- [ ] Create `inventory` package
- [ ] Create `InventoryStock` entity
- [ ] Create `StockMovement` entity
- [ ] Create migrations for inventory tables
- [ ] Create inventory repositories
- [ ] Create inventory DTOs
- [ ] Create `InventoryService`
- [ ] Create `InventoryController`
- [ ] Prevent negative stock
- [ ] Record every stock movement
- [ ] Add stock adjustment endpoint
- [ ] Add low-stock query
- [ ] Add tests

Endpoints planned:

```http
GET  /api/inventory
GET  /api/inventory/{productId}
POST /api/inventory/{productId}/adjust
GET  /api/inventory/{productId}/movements
```

### Phase 4 — Authentication and Authorization

- [ ] Create `auth` package
- [ ] Create `user` package
- [ ] Add Spring Security config
- [ ] Add BCrypt password hashing
- [ ] Add JWT token generation
- [ ] Add JWT authentication filter
- [ ] Create user entity
- [ ] Create role enum
- [ ] Create login endpoint
- [ ] Create current-user endpoint
- [ ] Add admin-only user management
- [ ] Add method-level authorization
- [ ] Add security tests

Endpoints planned:

```http
POST  /api/auth/login
GET   /api/auth/me
POST  /api/users
GET   /api/users
PATCH /api/users/{id}/role
PATCH /api/users/{id}/active
```

### Phase 5 — Sales Checkout

- [ ] Create `sale` package
- [ ] Create `Sale` entity
- [ ] Create `SaleItem` entity
- [ ] Create migrations for sales tables
- [ ] Create sale repositories
- [ ] Create checkout request/response DTOs
- [ ] Create `SaleService`
- [ ] Create `SaleController`
- [ ] Copy product price at sale time
- [ ] Check stock before completing sale
- [ ] Reduce stock after sale completion
- [ ] Create stock movement records for sold items
- [ ] Prevent editing completed sales
- [ ] Add tests

Endpoints planned:

```http
POST /api/sales/checkout
GET  /api/sales
GET  /api/sales/{id}
```

### Phase 6 — Payment Handling

- [ ] Create `payment` package
- [ ] Create `Payment` entity
- [ ] Add migration for payments table
- [ ] Support cash payment
- [ ] Support card payment
- [ ] Validate payment total equals sale total
- [ ] Support cash tendered/change calculation
- [ ] Add tests

Payment methods for MVP:

```text
CASH
CARD
```

### Phase 7 — Receipt Response

- [ ] Create receipt DTO
- [ ] Return receipt data after checkout
- [ ] Include sale number
- [ ] Include cashier name
- [ ] Include item snapshots
- [ ] Include totals
- [ ] Include payment details
- [ ] Include change amount
- [ ] Add printable receipt later

### Phase 8 — Refunds

- [ ] Create `RefundEntity`
- [ ] Create refund endpoint
- [ ] Prevent refund amount above original sale amount
- [ ] Prevent refund quantity above sold quantity
- [ ] Add stock movement for returned items
- [ ] Mark sale as partially refunded or refunded
- [ ] Add tests

Endpoint planned:

```http
POST /api/sales/{saleId}/refunds
```

### Phase 9 — Reports

- [ ] Daily sales total
- [ ] Monthly sales total
- [ ] Top-selling products
- [ ] Low-stock products
- [ ] Sales by cashier
- [ ] Payment method summary
- [ ] Add tests for report queries

Endpoints planned:

```http
GET /api/reports/daily-sales
GET /api/reports/monthly-sales
GET /api/reports/top-products
GET /api/reports/low-stock
GET /api/reports/payment-summary
```

### Phase 10 — API Documentation

- [ ] Add Springdoc OpenAPI
- [ ] Configure Swagger UI
- [ ] Document main request/response DTOs
- [ ] Verify Swagger page loads

Planned URL:

```http
/swagger-ui.html
```

### Phase 11 — Frontend

- [ ] Create React app
- [ ] Add Tailwind CSS
- [ ] Add routing
- [ ] Add login page
- [ ] Add dashboard page
- [ ] Add product/category pages
- [ ] Add inventory pages
- [ ] Add checkout page
- [ ] Add sales history page
- [ ] Add reports page
- [ ] Add admin user page

## Backend Package Structure

Use feature-based packages. The project is small, but POS systems grow naturally by business feature: category, product, inventory, sale, payment, receipt, and report.

```text
com.retailcore.pos
  category
    CategoryEntity.java
    CategoryRepository.java
    exception/
      CategoryNotFoundException.java

  product
    ProductController.java
    ProductService.java
    ProductRepository.java
    ProductEntity.java
    ProductDetails.java
    dto/
      ProductCreateRequest.java
      ProductUpdateRequest.java
      ProductActiveRequest.java
      ProductResponse.java
    exception/
      ProductNotFoundException.java
      DuplicateProductSkuException.java
      DuplicateProductBarcodeException.java

  inventory
  sale
  payment
  receipt
  report

  common
    dto/
    exception/

  config
```

Naming standard:

- JPA/database-mapped classes use `*Entity`, for example `ProductEntity`.
- API request/response classes live in feature-local `dto/` packages.
- Feature-specific exceptions stay inside the feature package.
- Cross-feature API errors and base exceptions stay in `common/`.
- Avoid vague `model/` packages. `model` can mean entity, DTO, MVC model, view model, or ML model.

Avoid layer-only package structure like this:

```text
controller
service
repository
entity
dto
```

Layer-based structure is acceptable for tiny tutorials, but feature-based structure is the project standard because it scales better and keeps each business capability together.

## Development Workflow Per Module

For every module, use this order:

1. Entity
2. Flyway migration
3. Repository
4. DTOs
5. Service
6. Controller
7. Tests
8. Manual API check

Do not expose JPA entities directly from controllers. Use DTOs.

## Immediate Next Step

Start with the baseline tasks:

1. Add Docker Compose for PostgreSQL
2. Add Flyway
3. Create the first migration
4. Verify backend startup
5. Build Category CRUD

That is the first real checkpoint. Everything else unlocks after that.
