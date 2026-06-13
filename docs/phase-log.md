# RetailCore POS Phase Log

This file is the compact handoff state for future sessions. Treat `README.md` and `docs/project-spec.md` as the broader source of truth; treat this file as the implementation checkpoint log.

## Current Checkpoint

Next recommended work: tighten Phase 2 product tests, then move to Phase 3 inventory.

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

Status: Mostly done, needs checkpoint cleanup

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
- Product service tests and partial controller tests

Endpoints implemented:

```http
POST   /api/products
GET    /api/products
GET    /api/products/{id}
PUT    /api/products/{id}
PATCH  /api/products/{id}/active
```

Known cleanup before marking fully done:
- Add/verify controller tests for:
  - `GET /api/products`
  - `GET /api/products/{id}`
  - `PUT /api/products/{id}`
- Add/verify service tests for:
  - update duplicate SKU
  - update duplicate barcode
  - product not found paths
- Run manual API checks for all product endpoints.
- Update `README.md` Phase 2 checklist only after tests and manual checks pass.

## Phase 3 — Inventory Module

Status: Not started

Planned next after Phase 2 cleanup:
- `InventoryStockEntity`
- `StockMovementEntity`
- inventory migrations
- inventory repositories
- inventory DTOs
- `InventoryService`
- `InventoryController`
- negative-stock protection
- stock movement creation for every adjustment
- stock adjustment endpoint
- low-stock query
- tests

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
