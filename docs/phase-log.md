# RetailCore POS Phase Log

This file is the compact handoff state for future sessions. Treat `README.md` and `docs/project-spec.md` as the broader source of truth; treat this file as the implementation checkpoint log.

## Current Checkpoint

Next recommended work: start Phase 6 payment handling.

## Phase 0 — Project Baseline

Status: Done

Implemented:
- Spring Boot backend under `backend/`
- Java 21 project configuration
- PostgreSQL datasource configuration
- Docker Compose PostgreSQL baseline
- Flyway dependency and baseline migration
- Spring Security, JWT, OpenAPI, and Testcontainers dependencies
- Actuator health/info exposure

Verification:
- Backend startup was previously verified against local PostgreSQL.
- Current full backend test suite passes.

Notes:
- In this WSL environment, Docker CLI is unavailable; Podman/Podman Compose are available.
- Port `8080` may already be occupied. Use another port such as `18080` for manual API checks if needed.

## Phase 1 — Category Module

Status: Done

Implemented:
- `category` feature package
- `CategoryEntity`
- Flyway migration: `V2__create_categories_table.sql`
- `CategoryRepository`
- Category DTOs:
  - `CategoryCreateRequest`
  - `CategoryUpdateRequest`
  - `CategoryResponse`
- `CategoryService`
- `CategoryController`
- Validation for required name, field sizes, and update active flag
- Duplicate-name protection using case-insensitive repository checks
- Delete protection when products still reference the category
- Category-specific exceptions:
  - `CategoryNotFoundException`
  - `DuplicateCategoryNameException`
  - `CategoryInUseException`
- Global error handling for category-in-use conflicts
- Unit/service tests and controller tests

Endpoints verified:

```http
POST   /api/categories
GET    /api/categories
GET    /api/categories/{id}
PUT    /api/categories/{id}
DELETE /api/categories/{id}
```

Verification:
- `./mvnw test` passed.
- Manual API checks passed on port `18080`:
  - `POST /api/categories` returned `201`
  - duplicate category create returned `409`
  - `GET /api/categories` returned `200`
  - `GET /api/categories/{id}` returned `200`
  - `PUT /api/categories/{id}` returned `200`
  - deleting unused category returned `204`
  - deleting category used by a product returned `409`

Commit:
- Phase 1 implementation was pushed to `origin/main` with commit message `feat: complete category module`.

## Phase 2 — Product Module

Status: Done

Implemented:
- `product` feature package
- `ProductEntity`
- Flyway migration: `V3__create_products_table.sql`
- `ProductRepository`
- Product DTOs:
  - `ProductCreateRequest`
  - `ProductUpdateRequest`
  - `ProductActiveRequest`
  - `ProductResponse`
- `ProductService`
- `ProductController`
- SKU uniqueness rule
- Barcode uniqueness rule
- Positive price validation
- Active/inactive product status
- Product service tests and controller tests

Endpoints implemented:

```http
POST   /api/products
GET    /api/products
GET    /api/products/{id}
PUT    /api/products/{id}
PATCH  /api/products/{id}/active
```

Verification:
- `./mvnw test` passed.
- Manual API checks passed on port `18080`:
  - `POST /api/products` returned `201`
  - duplicate SKU create returned `409`
  - duplicate barcode create returned `409`
  - `GET /api/products` returned `200`
  - `GET /api/products/{id}` returned `200`
  - `PUT /api/products/{id}` returned `200`
  - `PATCH /api/products/{id}/active` returned `200`

## Phase 3 — Inventory Module

Status: Done

Implemented:
- `inventory` feature package
- `InventoryStockEntity`
- `StockMovementEntity`
- `StockMovementType`
- Flyway migration: `V4__create_inventory_tables.sql`
- Inventory repositories:
  - `InventoryStockRepository`
  - `StockMovementRepository`
- Inventory DTOs:
  - `StockAdjustmentRequest`
  - `InventoryStockResponse`
  - `StockMovementResponse`
- `InventoryService`
- `InventoryController`
- Negative-stock protection
- Stock movement creation for every adjustment
- Stock adjustment endpoint
- Low-stock query endpoint
- Inventory service tests and controller tests

Endpoints implemented:

```http
GET  /api/inventory
GET  /api/inventory/low-stock
GET  /api/inventory/{productId}
POST /api/inventory/{productId}/adjust
GET  /api/inventory/{productId}/movements
```

Verification:
- `./mvnw test` passed.
- Manual API checks passed on port `18080`:
  - `POST /api/categories` returned `201` for setup data
  - `POST /api/products` returned `201` for setup data
  - `POST /api/inventory/{productId}/adjust` returned `200`
  - `GET /api/inventory/{productId}` returned `200`
  - `GET /api/inventory/{productId}/movements` returned `200`
  - negative stock adjustment returned `409`
  - `GET /api/inventory/low-stock` returned `200`

## Phase 4 — Authentication and Authorization

Status: Done

Implemented:
- `auth` feature package
- `user` feature package
- `UserEntity`
- `UserRole` enum with `ADMIN`, `MANAGER`, and `CASHIER`
- Flyway migration: `V5__create_users_table.sql`
- `UserRepository`
- User DTOs:
  - `UserCreateRequest`
  - `UserRoleRequest`
  - `UserActiveRequest`
  - `UserResponse`
- Auth DTOs:
  - `LoginRequest`
  - `AuthResponse`
- `UserService`
- `AuthService`
- `JwtService`
- `JwtAuthenticationFilter`
- Spring Security config with stateless JWT authentication
- BCrypt password hashing
- Login endpoint
- Current-user endpoint
- Admin-only user management endpoints using method-level authorization
- Auth and user service/controller tests

Endpoints implemented:

```http
POST  /api/auth/login
GET   /api/auth/me
POST  /api/users
GET   /api/users
PATCH /api/users/{id}/role
PATCH /api/users/{id}/active
```

Verification:
- `./mvnw test` passed with 76 tests, 0 failures, 0 errors.

## Phase 5 — Sales Checkout

Status: Done

Implemented:
- `sale` feature package
- `SaleEntity`
- `SaleItemEntity`
- `SaleStatus` enum with `COMPLETED`
- Flyway migration: `V6__create_sales_tables.sql`
- Sale repositories:
  - `SaleRepository`
  - `SaleItemRepository`
- Sale DTOs:
  - `CheckoutRequest`
  - `CheckoutItemRequest`
  - `SaleResponse`
  - `SaleItemResponse`
- `SaleService`
- `SaleController`
- Checkout endpoint that copies product price at sale time
- Stock availability check before sale completion
- Stock reduction after checkout
- `SALE` stock movement records for sold items
- Completed-only sale persistence; no edit endpoints exposed for completed sales
- Sale service tests and controller tests
- Sale conflict exceptions for insufficient stock and inactive products

Endpoints implemented:

```http
POST /api/sales/checkout
GET  /api/sales
GET  /api/sales/{id}
```

Verification:
- Focused sale tests passed: `./mvnw -Dtest=SaleServiceTest,SaleControllerTest test` with 11 tests, 0 failures, 0 errors.
- Full backend test suite passed: `./mvnw test` with 87 tests, 0 failures, 0 errors.

## Session Handoff Prompt

Use this when starting a fresh Hermes session:

```text
We are working on RetailCore POS.

Before coding, read:
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/pom.xml
- relevant existing modules under backend/src/main/java/com/retailcore/pos

Continue from the next incomplete phase/checkpoint only.
Follow existing project conventions:
- feature-based packages
- JPA classes named *Entity
- DTOs inside feature-local dto packages
- service/controller/repository pattern
- tests before or alongside implementation
- run ./mvnw test before final answer

Do not rewrite completed phases unless required by failing tests.
```
