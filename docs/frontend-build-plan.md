# RetailCore POS Frontend Step-by-Step Build Plan

Use this file to run Phase 11 as small, copyable frontend checkpoints. Each step has a base prompt you can paste into a fresh agent session.

Primary rule: complete one step at a time. Do not let the agent speedrun the whole map and leave a pile of placeholder screens behind. That is how bugs spawn.

---

## Global Rules for Every Frontend Step

Before coding, every step must read:

- `FRONTEND.md`
- `docs/frontend-build-plan.md`
- `README.md`
- `docs/project-spec.md`
- `docs/phase-log.md`
- Relevant backend controllers and DTOs under `backend/src/main/java/com/retailcore/pos`

Backend/OpenAPI is the source of truth for:

- endpoint paths
- DTO names
- field names
- enum values
- error response shape
- auth and role behavior

Frontend conventions:

- React + Vite + TypeScript.
- Tailwind CSS.
- React Router.
- Axios through one centralized client.
- TanStack Query for API-backed server state.
- Feature-first folders under `frontend/src/features`.
- Shared UI only under `frontend/src/components`.
- No `any` for API responses.
- No fake data on screens marked complete.
- Every API-backed page handles loading, error, empty, forbidden, and mutation states.
- Verify each implementation with `npm run lint` and `npm run build` before final response.
- Run backend tests only if backend code changes.

Recommended local backend URL when testing manually from Vite:

```env
VITE_API_BASE_URL=http://localhost:18080
```

If browser API calls fail with Axios `Network Error`, check CORS/backend health before rewriting frontend logic:

```bash
curl -i http://localhost:18080/actuator/health
curl -i -X OPTIONS http://localhost:18080/api/auth/login \
  -H 'Origin: http://localhost:5173' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: content-type'
```

---

## Full Frontend Build Sequence

| Step | Name | Main Outcome |
|---|---|---|
| FE-00 | Contract audit | Confirm backend endpoints, DTOs, roles, and error shape |
| FE-01 | Scaffold frontend app | Vite React TypeScript app exists and builds |
| FE-02 | Styling and UI foundation | Tailwind, design tokens, layout primitives, shared feedback UI |
| FE-03 | API client and contracts | Axios client, token storage, typed DTOs, error helpers |
| FE-04 | Routing, providers, layout shell | Router, QueryClient, AuthProvider skeleton, protected shell, nav config |
| FE-05 | Login and session restore | Real login, `/api/auth/me`, logout, 401/403 behavior |
| FE-06 | Dashboard overview | Real dashboard cards from reports/inventory endpoints |
| FE-07 | Categories and products | CRUD/list/update flows for categories/products |
| FE-08 | Inventory | Stock list, low-stock view, adjustments, movement history |
| FE-09 | Checkout and receipt | Product picker, cart, cash/card payment, receipt response |
| FE-10 | Sales and refunds | Sales history, sale detail, refund flow |
| FE-11 | Reports | Daily/monthly/top-products/low-stock/cashier/payment reports |
| FE-12 | Admin users | Admin-only user list/create/role/active management |
| FE-13 | Final hardening | Accessibility, responsive pass, manual smoke, cleanup |

---

## Common Base Prompt Template

Use this as the prefix for every step. Replace `[STEP]` and `[STEP NAME]`.

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- relevant backend controllers/DTOs under backend/src/main/java/com/retailcore/pos

Continue with frontend [STEP] — [STEP NAME] only.

Follow frontend conventions:
- React + Vite + TypeScript under frontend/
- Tailwind CSS for styling
- React Router for routing
- Axios only through the centralized API client
- TanStack Query for API-backed server state
- feature-first folders under frontend/src/features
- shared reusable UI under frontend/src/components
- DTO/type names mirror backend DTO names exactly
- no any for API responses
- no fake/mock data on screens marked complete
- every API-backed page handles loading, error, empty, forbidden, and mutation states

Do not rewrite completed frontend steps unless required by failing lint/build or broken integration.
Do not change backend code unless this step explicitly requires it; if backend changes are unavoidable, explain why and run backend tests.
Before final answer, run:
- npm run lint
- npm run build
Report what changed and the real command results.
```

---

## FE-00 — Contract Audit and Frontend Planning

### Goal

Create the frontend contract checklist before implementation. No React code yet unless a `frontend/` app already exists and must be inspected.

### Scope

- Read project docs.
- Inspect controllers, DTO records, enums, security config, and global error handler.
- Confirm API endpoints and role access.
- Confirm error response shape.
- Note which list endpoints return arrays vs paginated responses.
- Update frontend docs if any mismatch is found.

### Acceptance Criteria

- `FRONTEND.md` and this plan agree with backend reality.
- The next implementation step is clear.
- No guessed DTO shapes remain in the plan.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/pom.xml
- backend/src/main/java/com/retailcore/pos/config/SecurityConfig.java
- backend/src/main/java/com/retailcore/pos/common/exception/GlobalExceptionHandler.java
- all backend controllers and DTOs under backend/src/main/java/com/retailcore/pos

Continue with frontend FE-00 — Contract Audit and Frontend Planning only.

Do not build UI yet. Verify the frontend plan against backend code/OpenAPI:
- endpoint paths
- DTO names and fields
- enum values
- auth routes
- role restrictions
- error response shape
- whether list endpoints return arrays or Page<T>

Patch FRONTEND.md or docs/frontend-build-plan.md if anything is stale or guessed.
Before final answer, report the verified API/role/error contract and any doc changes.
```

---

## FE-01 — Scaffold Frontend App

### Goal

Create the `frontend/` Vite React TypeScript app with a clean baseline that passes lint/build.

### Scope

- Scaffold Vite React TypeScript under `frontend/`.
- Install core dependencies:
  - `axios`
  - `react-router-dom`
  - `@tanstack/react-query`
- Install/configure Tailwind dependencies according to the installed Tailwind major version.
- Keep the app minimal. No business screens yet.
- Add basic project scripts if missing.

### Acceptance Criteria

- `frontend/package.json` exists.
- `frontend/src/main.tsx` exists.
- `npm run lint` passes.
- `npm run build` passes.
- No backend code changed.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md

Continue with frontend FE-01 — Scaffold Frontend App only.

Create the frontend app under frontend/ using React + Vite + TypeScript.
Install the minimum required dependencies for the frontend baseline:
- axios
- react-router-dom
- @tanstack/react-query
- Tailwind CSS dependencies appropriate for the installed Tailwind version

Keep this step to scaffolding and build configuration only. Do not implement auth, routes, or feature pages yet.
Follow the project conventions in FRONTEND.md.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
Report the real command results.
```

---

## FE-02 — Styling, Design Tokens, and Shared UI Foundation

### Goal

Set the visual foundation: Tailwind, global styles, design tokens, and reusable UI/feedback primitives.

### Scope

- Configure global CSS.
- Add RetailCore POS design tokens from `FRONTEND.md`.
- Create initial shared components:
  - `Button`
  - `Input`
  - `Select`
  - `Badge` or `StatusBadge`
  - `Card`
  - `Spinner`
  - `Skeleton`
  - `ErrorBanner`
  - `EmptyState`
  - `PageHeader`
- Ensure focus states and accessible labels are supported.
- Do not create API-backed pages yet.

### Acceptance Criteria

- Shared UI components are typed and reusable.
- Components use accessible defaults.
- No raw API work in this step.
- Lint/build pass.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md

Continue with frontend FE-02 — Styling, Design Tokens, and Shared UI Foundation only.

Implement Tailwind/global styling and the base reusable UI components needed by later pages:
- Button
- Input
- Select
- Badge or StatusBadge
- Card
- Spinner
- Skeleton
- ErrorBanner
- EmptyState
- PageHeader

Use the POS dashboard visual direction from FRONTEND.md: slate/neutral surfaces, emerald accent, clear amber/red statuses, keyboard-friendly and data-dense.
Do not build real feature pages yet.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
Report the real command results.
```

---

## FE-03 — API Client, DTO Types, and Error Helpers

### Goal

Create the frontend API foundation without tying it to page UI yet.

### Scope

- Create `src/lib/api/client.ts` with one Axios instance.
- Read `VITE_API_BASE_URL`; use only one dev fallback in this file.
- Create token storage helpers under `src/lib/auth/tokenStorage.ts`.
- Create `src/lib/api/errors.ts` for `ApiErrorResponse`, `FieldErrorResponse`, and extraction helpers.
- Create typed API modules for current backend areas:
  - `auth.ts`
  - `categories.ts`
  - `products.ts`
  - `inventory.ts`
  - `sales.ts`
  - `reports.ts`
  - `users.ts`
- DTO names must mirror backend DTO names.
- Do not invent fields. Inspect backend DTOs/OpenAPI.

### Acceptance Criteria

- No component imports Axios directly.
- API modules are thin wrappers.
- Errors can be converted to display messages and field-error maps.
- Lint/build pass.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/src/main/java/com/retailcore/pos/common/dto/ApiErrorResponse.java
- backend/src/main/java/com/retailcore/pos/common/dto/FieldErrorResponse.java
- backend/src/main/java/com/retailcore/pos/config/SecurityConfig.java
- relevant backend controllers and DTOs under backend/src/main/java/com/retailcore/pos

Continue with frontend FE-03 — API Client, DTO Types, and Error Helpers only.

Implement the frontend API foundation:
- one Axios client in frontend/src/lib/api/client.ts
- token storage helpers in frontend/src/lib/auth/tokenStorage.ts
- API error types/helpers in frontend/src/lib/api/errors.ts
- typed API wrapper files for auth, categories, products, inventory, sales, reports, and users

DTO/type names must mirror backend DTO names exactly. Do not use any for API responses. Do not build feature pages yet.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
Report the real command results.
```

---

## FE-04 — Providers, Routing, Protected Shell, and Navigation Config

### Goal

Create the app skeleton: providers, route definitions, protected route components, role guard, and layout shell.

### Scope

- Add `QueryClientProvider`.
- Add `AuthProvider` skeleton if not already implemented.
- Add React Router setup.
- Add `ProtectedRoute`.
- Add `RoleGuard`.
- Add `navConfig.ts` as the only source of nav role mapping.
- Add `AppLayout` with sidebar/topbar/mobile-friendly structure.
- Create minimal route placeholder components only where necessary, clearly marked as incomplete.

### Acceptance Criteria

- Routes exist for login, dashboard, categories, products, inventory, checkout, sales, reports, users.
- Protected shell does not flash protected content while auth state is loading.
- Nav visibility is role-driven from one config file.
- Lint/build pass.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/src/main/java/com/retailcore/pos/user/UserRole.java
- backend/src/main/java/com/retailcore/pos/config/SecurityConfig.java

Continue with frontend FE-04 — Providers, Routing, Protected Shell, and Navigation Config only.

Implement the frontend app shell:
- app providers including QueryClientProvider and AuthProvider skeleton
- React Router route definitions
- ProtectedRoute
- RoleGuard
- route/nav config with role-based nav in one file
- AppLayout with responsive navigation structure

Create only minimal placeholders for feature pages that are not implemented yet, and label them as incomplete. Do not implement login/API-backed features in this step beyond what is needed for the shell.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
Report the real command results.
```

---

## FE-05 — Login, Session Restore, Logout, and Auth Failure Handling

### Goal

Make authentication real.

### Scope

- Implement login page and form.
- Call `POST /api/auth/login`.
- Store JWT through token storage helper.
- Restore current user with `GET /api/auth/me`.
- Implement logout.
- Implement `401` handling: clear token/session and redirect to login.
- Implement `403` handling: show forbidden UI/message; do not redirect to login.
- Redirect to originally requested route after login.

### Acceptance Criteria

- Login uses real backend API wrapper.
- Invalid login displays backend error message.
- Refresh restores session from token.
- Protected routes wait for auth boot before rendering content.
- Lint/build pass.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/src/main/java/com/retailcore/pos/auth/AuthController.java
- backend/src/main/java/com/retailcore/pos/auth/dto/LoginRequest.java
- backend/src/main/java/com/retailcore/pos/auth/dto/AuthResponse.java
- backend/src/main/java/com/retailcore/pos/user/dto/UserResponse.java
- backend/src/main/java/com/retailcore/pos/config/SecurityConfig.java

Continue with frontend FE-05 — Login, Session Restore, Logout, and Auth Failure Handling only.

Implement real auth:
- login page and controlled form
- POST /api/auth/login through the auth API wrapper
- token storage through tokenStorage helper
- GET /api/auth/me session restore
- logout that clears token and query cache
- 401 clears auth and redirects to /login
- 403 shows not-authorized UI and does not redirect
- redirect back to the originally requested route after login

Do not implement business feature pages yet.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
If a backend is available, manually smoke-test login/session restore and report the result.
```

---

## FE-06 — Dashboard Overview

### Goal

Build the first protected API-backed page using real dashboard data.

### Scope

- Dashboard page visible to `ADMIN`, `MANAGER`, `CASHIER`.
- Use report/inventory endpoints appropriate for the current role.
- Suggested cards:
  - daily sales total
  - monthly sales total
  - low-stock count/list
  - top products preview for manager/admin
- Use loading/error/empty states.
- Use `Intl.NumberFormat` and `Intl.DateTimeFormat` helpers.

### Acceptance Criteria

- Dashboard uses real API calls, not fake stats.
- Cashier does not see manager/admin-only report calls if backend forbids them.
- Query hooks and query keys are feature-local/stable.
- Lint/build pass.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/src/main/java/com/retailcore/pos/report/ReportController.java
- backend/src/main/java/com/retailcore/pos/report/dto
- backend/src/main/java/com/retailcore/pos/inventory/InventoryController.java
- backend/src/main/java/com/retailcore/pos/inventory/dto

Continue with frontend FE-06 — Dashboard Overview only.

Build the protected dashboard page with real backend data and role-aware behavior.
Use TanStack Query for server state. Handle loading, error, empty, and forbidden states.
Do not fake KPI values. If a role cannot access an endpoint, do not call it for that role.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
Report the real command results and which endpoints the dashboard uses.
```

---

## FE-07 — Categories and Products Management

### Goal

Build manager/admin catalog management.

### Scope

Categories:

- List categories.
- Create category.
- Edit category.
- Delete category with confirmation.
- Show conflict errors when category is in use.

Products:

- List products.
- Create product.
- Edit product.
- Enable/disable product.
- Category select uses real categories.
- Show duplicate SKU/barcode errors.

### Acceptance Criteria

- Routes restricted to `ADMIN` and `MANAGER`.
- Forms map backend validation errors inline.
- Mutations invalidate affected queries.
- No direct Axios calls from components.
- Lint/build pass.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/src/main/java/com/retailcore/pos/category/CategoryController.java
- backend/src/main/java/com/retailcore/pos/category/dto
- backend/src/main/java/com/retailcore/pos/product/ProductController.java
- backend/src/main/java/com/retailcore/pos/product/dto

Continue with frontend FE-07 — Categories and Products Management only.

Build category and product management for ADMIN and MANAGER:
- category list/create/edit/delete with confirmation and conflict handling
- product list/create/edit/active toggle
- category select populated from real categories API
- inline field errors from backend validation
- loading/error/empty/forbidden/mutation states
- TanStack Query hooks with proper invalidation

Do not implement inventory, checkout, reports, or users in this step.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
Report the real command results and the endpoints wired.
```

---

## FE-08 — Inventory Stock, Adjustments, and Movement History

### Goal

Build manager/admin inventory workflows.

### Scope

- Inventory list from `GET /api/inventory`.
- Low-stock view from `GET /api/inventory/low-stock`.
- Product inventory detail from `GET /api/inventory/{productId}`.
- Stock adjustment form using `POST /api/inventory/{productId}/adjust`.
- Movement history using `GET /api/inventory/{productId}/movements`.
- Clear positive/negative adjustment UI.

### Acceptance Criteria

- Routes restricted to `ADMIN` and `MANAGER`.
- Negative-stock/business conflict errors display clearly.
- Movement history has loading/error/empty states.
- Mutations invalidate inventory and reports where relevant.
- Lint/build pass.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/src/main/java/com/retailcore/pos/inventory/InventoryController.java
- backend/src/main/java/com/retailcore/pos/inventory/dto
- backend/src/main/java/com/retailcore/pos/inventory/StockMovementType.java

Continue with frontend FE-08 — Inventory Stock, Adjustments, and Movement History only.

Build inventory pages for ADMIN and MANAGER:
- stock list
- low-stock view
- product stock detail
- stock adjustment form
- stock movement history

Use real inventory API wrappers and TanStack Query. Handle loading, error, empty, forbidden, and mutation states. Show positive/negative stock adjustments clearly and display backend conflict messages.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
Report the real command results and the endpoints wired.
```

---

## FE-09 — Checkout and Receipt Flow

### Goal

Build the core cashier workflow.

### Scope

- Checkout route visible to `ADMIN`, `MANAGER`, `CASHIER`.
- Product picker/search from real products endpoint.
- Cart state with quantity controls.
- Payment method `CASH`/`CARD`.
- Cash tendered and change calculation UI.
- Submit checkout to `POST /api/sales/checkout`.
- Display receipt response immediately.
- Invalidate products/inventory/sales/reports after successful checkout.

### Acceptance Criteria

- No fake products.
- Cart prevents quantity below 1.
- Stock/active product errors from backend are displayed clearly.
- Receipt uses backend response fields, not client recomputation.
- Lint/build pass.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/src/main/java/com/retailcore/pos/sale/SaleController.java
- backend/src/main/java/com/retailcore/pos/sale/dto
- backend/src/main/java/com/retailcore/pos/payment/PaymentMethod.java
- backend/src/main/java/com/retailcore/pos/receipt/dto
- backend/src/main/java/com/retailcore/pos/product/ProductController.java
- backend/src/main/java/com/retailcore/pos/product/dto

Continue with frontend FE-09 — Checkout and Receipt Flow only.

Build the checkout workflow for ADMIN, MANAGER, and CASHIER:
- product picker/search using real products API
- cart with quantity controls and line totals
- CASH/CARD payment UI
- cash tendered/change display for cash payments
- POST /api/sales/checkout mutation
- receipt display from backend ReceiptResponse
- loading/error/conflict/success states
- query invalidation for inventory, sales, reports, and products after checkout

Do not implement sales history/refunds beyond what checkout needs.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
Report the real command results and checkout endpoint wiring.
```

---

## FE-10 — Sales History, Sale Detail, and Refunds

### Goal

Build sales review and refund workflows.

### Scope

- Sales history from `GET /api/sales`.
- Sale detail from `GET /api/sales/{id}`.
- Refund form launched from sale detail.
- Refund mutation to `POST /api/sales/{id}/refunds`.
- Show refundable item quantities clearly.
- Display over-refund/conflict backend errors.
- Invalidate sales, inventory, reports after refund.

### Acceptance Criteria

- Routes visible to `ADMIN`, `MANAGER`, `CASHIER` unless backend says otherwise.
- Refund UI cannot submit empty/invalid quantities.
- Backend conflict errors are visible and useful.
- Lint/build pass.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/src/main/java/com/retailcore/pos/sale/SaleController.java
- backend/src/main/java/com/retailcore/pos/sale/dto
- backend/src/main/java/com/retailcore/pos/refund/dto

Continue with frontend FE-10 — Sales History, Sale Detail, and Refunds only.

Build sales and refund workflows:
- sales history list from real API
- sale detail page/dialog from real API
- refund form launched from sale detail
- POST /api/sales/{id}/refunds mutation
- display refundable quantities and backend conflict errors
- loading/error/empty/forbidden/mutation states
- query invalidation for sales, inventory, and reports after refund

Do not implement reports/admin users in this step.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
Report the real command results and endpoints wired.
```

---

## FE-11 — Reports Page

### Goal

Build manager/admin reporting views.

### Scope

- Daily sales total with date selector.
- Monthly sales total with year/month selector.
- Top-selling products.
- Low-stock report.
- Sales by cashier.
- Payment method summary.
- Tables/cards first; charts optional only if they clarify.

### Acceptance Criteria

- Route restricted to `ADMIN` and `MANAGER`.
- Money/date formatting is consistent.
- No-data states render clearly.
- Charts, if added, have table/text alternatives.
- Lint/build pass.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/src/main/java/com/retailcore/pos/report/ReportController.java
- backend/src/main/java/com/retailcore/pos/report/dto
- backend/src/main/java/com/retailcore/pos/inventory/dto/InventoryStockResponse.java

Continue with frontend FE-11 — Reports Page only.

Build the reports page for ADMIN and MANAGER:
- daily sales total with date input
- monthly sales total with year/month input
- top-selling products
- low-stock report
- sales by cashier
- payment method summary

Use real report API wrappers and TanStack Query. Prefer readable cards/tables; add charts only if useful and accessible. Handle loading, error, empty, and forbidden states.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
Report the real command results and endpoints wired.
```

---

## FE-12 — Admin User Management

### Goal

Build admin-only user management.

### Scope

- User list from `GET /api/users`.
- Create user with `POST /api/users`.
- Change role with `PATCH /api/users/{id}/role`.
- Activate/deactivate with `PATCH /api/users/{id}/active`.
- Role badges and active status badges.
- Inline validation errors.

### Acceptance Criteria

- Route restricted to `ADMIN`.
- Manager/cashier see forbidden UI if routed there.
- Mutations invalidate user list.
- User forms never display/log passwords after submit.
- Lint/build pass.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/src/main/java/com/retailcore/pos/user/UserController.java
- backend/src/main/java/com/retailcore/pos/user/UserRole.java
- backend/src/main/java/com/retailcore/pos/user/dto

Continue with frontend FE-12 — Admin User Management only.

Build admin-only user management:
- user list
- create user form
- change role action
- activate/deactivate action
- role and active status badges
- inline field errors from backend validation
- loading/error/empty/forbidden/mutation states

Do not log passwords or JWTs. Do not implement unrelated pages in this step.
Before final answer, from frontend/ run:
- npm run lint
- npm run build
Report the real command results and endpoints wired.
```

---

## FE-13 — Final Hardening, Accessibility, Responsive, and Smoke Test

### Goal

Turn the frontend from “works on my machine” into something that survives contact with reality. Barely a boss fight.

### Scope

- Run full lint/build.
- Review all routes for protected-content flash.
- Verify role navigation for `ADMIN`, `MANAGER`, `CASHIER`.
- Verify loading/error/empty/forbidden states.
- Check mobile widths: 375px, 768px, 1024px, desktop.
- Check keyboard navigation and focus states.
- Check form labels and inline errors.
- Check CORS/backend health if browser network errors appear.
- Remove unused placeholder code and dead imports.
- Update docs/phase-log.md with frontend checkpoint summary if requested.

### Acceptance Criteria

- `npm run lint` passes.
- `npm run build` passes.
- Manual smoke test results are documented.
- No fake complete screens remain.
- No JWT/password logging.

### Base Prompt

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md

Continue with frontend FE-13 — Final Hardening, Accessibility, Responsive, and Smoke Test only.

Do a final frontend hardening pass:
- run lint/build and fix issues
- review protected route/auth behavior for content flash
- verify role-aware navigation for ADMIN, MANAGER, and CASHIER
- verify loading/error/empty/forbidden states on API-backed pages
- check responsive layout at 375px, 768px, 1024px, and desktop widths
- check keyboard navigation, focus states, labels, and form errors
- remove unused placeholder code/dead imports
- check CORS/backend health if API calls fail with Network Error

Do not add new features unless needed to finish an incomplete promised frontend requirement.
Before final answer, run from frontend/:
- npm run lint
- npm run build
If backend is available, run a manual smoke test and report what passed/failed.
```

---

## One-Shot Prompt for the Whole Frontend Phase

Use this only when you want an agent to attempt the entire frontend phase in one long run. Risky. High variance. Fun, if you enjoy debugging chaos.

```text
We are working on RetailCore POS.

Before coding, read:
- FRONTEND.md
- docs/frontend-build-plan.md
- README.md
- docs/project-spec.md
- docs/phase-log.md
- backend/pom.xml
- all relevant backend controllers/DTOs under backend/src/main/java/com/retailcore/pos

Build Phase 11 frontend under frontend/ following docs/frontend-build-plan.md from FE-01 through FE-13.

Use React + Vite + TypeScript, Tailwind CSS, React Router, Axios through a centralized API client, AuthContext, and TanStack Query for API-backed server state.

Build vertical slices, not fake placeholders. Every API-backed page must handle loading, error, empty, forbidden, and mutation states. DTO/type names must mirror backend DTO names exactly. Do not assume paginated responses unless backend/OpenAPI says so.

Do not rewrite backend code unless unavoidable; if changed, explain why and run backend tests.
Before final answer, run from frontend/:
- npm run lint
- npm run build
Report the real command outputs and any manual smoke-test results.
```
