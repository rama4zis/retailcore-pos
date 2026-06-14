# FRONTEND.md — RetailCore POS Frontend Build Guide

Conventions and agent instructions for `frontend/`. Read this with `docs/frontend-build-plan.md`, `README.md`, `docs/project-spec.md`, and `docs/phase-log.md` before starting frontend work.

The frontend is Phase 11. The backend is the source of truth. Do not invent DTOs, routes, roles, or response shapes when the answer can be read from OpenAPI or backend code.

---

## Product Goal

Build a clean, production-style POS frontend for a small retail store:

- Fast cashier checkout.
- Simple product/category management.
- Clear inventory and stock movement visibility.
- Sales history, receipt/refund flows, and reporting.
- Role-aware navigation for `ADMIN`, `MANAGER`, and `CASHIER`.

Primary UX: operations-first, data-dense, keyboard-friendly, boring in the best way. This is not a marketing site. It is a work tool.

---

## Required Preflight Before Coding

1. Read `docs/project-spec.md` and `docs/phase-log.md`.
2. Inspect backend controllers and DTOs under `backend/src/main/java/com/retailcore/pos`.
3. Prefer OpenAPI when the backend is running:
   - Swagger UI: `/swagger-ui.html`
   - Raw schema: `/v3/api-docs`
4. Confirm actual API response shapes before creating TypeScript types.
5. Build one vertical slice at a time. Do not scaffold ten fake screens with placeholder data.

If the backend and this file disagree, backend code/OpenAPI wins. Patch this file after verifying the mismatch.

---

## Stack

- React + Vite + TypeScript.
- Tailwind CSS.
- React Router.
- Axios through a centralized API client only.
- TanStack Query for server state once API-backed pages are added.
- Local component state for UI state.
- `AuthContext` for auth/session state.
- No Redux/Zustand unless the app has a real cross-cutting client-state problem.

Keep dependencies boring. A POS app does not need a dependency loot box.

---

## Recommended Frontend Build Order

1. Scaffold `frontend/` with Vite React TypeScript.
2. Configure Tailwind and base styles.
3. Add routing shell and app layout.
4. Add API client and auth token helpers.
5. Build login + session restore using `/api/auth/login` and `/api/auth/me`.
6. Add protected routes and role guards.
7. Add role-aware navigation.
8. Build dashboard shell with real report/inventory calls.
9. Build categories and products CRUD/list pages.
10. Build inventory stock and adjustment pages.
11. Build checkout flow: product search/select → cart → payment → receipt.
12. Build sales history + detail + refund flow.
13. Build reports page.
14. Build admin user management page.
15. Run lint/build and manually verify auth, role, error, empty, and CORS paths.

No fake green checkmarks. If a screen is done, it talks to the backend or clearly states why it cannot.

For copyable per-step prompts and acceptance criteria, use `docs/frontend-build-plan.md`.

---

## Folder Structure

Use feature-first organization with shared primitives kept boring and reusable.

```text
frontend/
└── src/
    ├── app/
    │   ├── App.tsx
    │   └── providers.tsx          # Router, QueryClient, AuthProvider, etc.
    ├── components/
    │   ├── ui/                    # Button, Input, Select, Badge, Modal, Table...
    │   ├── feedback/              # Spinner, Skeleton, ErrorBanner, EmptyState, Toast...
    │   └── layout/                # AppLayout, Sidebar, Header, PageHeader...
    ├── features/
    │   ├── auth/
    │   │   ├── components/
    │   │   ├── pages/
    │   │   ├── hooks.ts
    │   │   └── types.ts
    │   ├── dashboard/
    │   ├── categories/
    │   ├── products/
    │   ├── inventory/
    │   ├── sales/                 # checkout, sales history, refunds, receipts
    │   ├── reports/
    │   └── users/
    ├── lib/
    │   ├── api/
    │   │   ├── client.ts          # axios instance + interceptors
    │   │   ├── errors.ts          # ApiErrorResponse helpers
    │   │   ├── auth.ts
    │   │   ├── categories.ts
    │   │   ├── products.ts
    │   │   ├── inventory.ts
    │   │   ├── sales.ts
    │   │   ├── reports.ts
    │   │   └── users.ts
    │   ├── auth/
    │   │   └── tokenStorage.ts
    │   ├── constants/
    │   └── format/
    │       ├── currency.ts
    │       └── date.ts
    ├── routes/
    │   ├── navConfig.ts
    │   ├── ProtectedRoute.tsx
    │   ├── RoleGuard.tsx
    │   └── router.tsx
    ├── styles/
    │   └── globals.css
    └── main.tsx
```

Feature folders may contain `pages/`, `components/`, `hooks.ts`, `queries.ts`, `types.ts`, and feature-local helpers. Shared components must be genuinely shared. Do not dump feature-specific UI into `components/ui` just because the import path looks cute.

---

## Naming and TypeScript Rules

- Components: `PascalCase.tsx`.
- Hooks: `useSomething.ts` or feature-local `hooks.ts` exporting `useSomething`.
- API files: plural feature names, e.g. `products.ts`, `categories.ts`.
- Types mirror backend DTO names exactly: `ProductResponse`, `ProductCreateRequest`, `AuthResponse`, etc.
- Do not use `any`. Use `unknown` at boundaries and narrow it.
- Use `import type` for type-only imports.
- Keep functions small. Prefer early returns over nested condition pyramids.
- Never expose backend entities directly in frontend names. Use DTO names from the API.

---

## API Client Rules

Create exactly one Axios instance in `src/lib/api/client.ts`.

Requirements:

- Base URL comes from `VITE_API_BASE_URL`.
- Development fallback is allowed, but keep it in the client only.
- Recommended local `.env.local` when backend uses the alternate WSL port:

```env
VITE_API_BASE_URL=http://localhost:18080
```

- Request interceptor attaches the bearer token from storage to the `Authorization` header when a token exists.
- Response interceptor handles auth failures consistently:
  - `401`: clear token, reset auth state, redirect to `/login` without flashing protected UI.
  - `403`: show an authorization error. Do not redirect to login.
- Never log the JWT.
- Never call `fetch` or `axios` directly from components. Components call feature hooks/services.
- Keep API wrapper functions thin. They should not contain UI logic.

Recommended API layers:

```text
component/page → feature hook/query → lib/api/{feature}.ts → axios client
```

---

## Error Contract

Backend error responses use this shape:

```ts
export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  fieldErrors: FieldErrorResponse[]
}

export interface FieldErrorResponse {
  field: string
  message: string
}
```

Frontend rules:

- Display `message` for general API errors.
- Map `fieldErrors` to inline form errors when field names match.
- `400`: validation error.
- `401`: unauthenticated/expired token.
- `403`: authenticated but wrong role/inactive access.
- `404`: missing record.
- `409`: business conflict, e.g. duplicate SKU, negative stock, insufficient stock, invalid refund.
- Network error with no HTTP response is often CORS or backend down. Do not rewrite auth first.

---

## Current Backend API Map

Verify with OpenAPI before implementation, but this is the current Phase 10 map.

| Area | Method + Path | Frontend use |
|---|---|---|
| Auth | `POST /api/auth/login` | Login |
| Auth | `GET /api/auth/me` | Restore current session |
| Categories | `POST /api/categories` | Create category |
| Categories | `GET /api/categories` | Category list/select options |
| Categories | `GET /api/categories/{id}` | Category detail/edit preload |
| Categories | `PUT /api/categories/{id}` | Update category |
| Categories | `DELETE /api/categories/{id}` | Delete category |
| Products | `POST /api/products` | Create product |
| Products | `GET /api/products` | Product list/search/checkout picker |
| Products | `GET /api/products/{id}` | Product detail/edit preload |
| Products | `PUT /api/products/{id}` | Update product |
| Products | `PATCH /api/products/{id}/active` | Enable/disable product |
| Inventory | `GET /api/inventory` | Stock list |
| Inventory | `GET /api/inventory/low-stock` | Low-stock alert/report |
| Inventory | `GET /api/inventory/{productId}` | Product stock detail |
| Inventory | `POST /api/inventory/{productId}/adjust` | Stock adjustment |
| Inventory | `GET /api/inventory/{productId}/movements` | Stock movement history |
| Sales | `POST /api/sales/checkout` | Checkout + receipt response |
| Sales | `GET /api/sales` | Sales history |
| Sales | `GET /api/sales/{id}` | Sale detail |
| Sales | `POST /api/sales/{id}/refunds` | Refund sale items |
| Payments | `POST /api/payments` | Standalone payment creation if needed |
| Payments | `GET /api/payments` | Payment list/admin view if needed |
| Payments | `GET /api/payments/{id}` | Payment detail if needed |
| Reports | `GET /api/reports/daily-sales?date=YYYY-MM-DD` | Daily total card |
| Reports | `GET /api/reports/monthly-sales?year=YYYY&month=M` | Monthly total card |
| Reports | `GET /api/reports/top-products` | Top products table/chart |
| Reports | `GET /api/reports/low-stock` | Low-stock report |
| Reports | `GET /api/reports/sales-by-cashier` | Cashier performance |
| Reports | `GET /api/reports/payment-summary` | Payment method summary |
| Users | `POST /api/users` | Admin creates user |
| Users | `GET /api/users` | Admin user list |
| Users | `PATCH /api/users/{id}/role` | Admin changes role |
| Users | `PATCH /api/users/{id}/active` | Admin activates/deactivates user |

Current list endpoints mostly return plain arrays, not Spring `Page<T>`. If backend later adds pagination, handle the actual OpenAPI shape. Do not blindly assume every list is `Page<T>`.

---

## Auth and Role Rules

Roles:

```ts
export type UserRole = 'ADMIN' | 'MANAGER' | 'CASHIER'
```

Rules:

- `AuthProvider` owns `user`, `token`, `status`, `login`, `logout`, and `refreshMe`.
- Auth boot has three states: `loading`, `authenticated`, `anonymous`.
- Protected routes render a loading/skeleton state while auth is booting.
- No protected content may render before auth is known.
- After login, redirect to the originally requested route if present.
- On logout, clear token and cached server state.
- Keep token storage isolated in `lib/auth/tokenStorage.ts`.

Suggested route access:

| Route | Roles |
|---|---|
| `/login` | public |
| `/` or `/dashboard` | `ADMIN`, `MANAGER`, `CASHIER` |
| `/categories` | `ADMIN`, `MANAGER` |
| `/products` | `ADMIN`, `MANAGER` |
| `/inventory` | `ADMIN`, `MANAGER` |
| `/checkout` | `ADMIN`, `MANAGER`, `CASHIER` |
| `/sales` | `ADMIN`, `MANAGER`, `CASHIER` |
| `/reports` | `ADMIN`, `MANAGER` |
| `/users` | `ADMIN` |

Define nav entries once in `routes/navConfig.ts`. Filter by role there, not inside every component like an amateur side quest.

---

## Server State with TanStack Query

Use TanStack Query for API-backed lists/details/mutations.

Rules:

- Set up `QueryClientProvider` in `app/providers.tsx` once API pages exist.
- Query keys are stable arrays:
  - `['products']`
  - `['products', productId]`
  - `['inventory']`
  - `['inventory', productId, 'movements']`
- Mutations invalidate the affected queries.
- Do not duplicate fetched server data into `useState` unless the user is editing a form draft.
- Keep query hooks feature-local: `features/products/queries.ts`, etc.
- Use `enabled` for queries that require an ID or authenticated user.
- Treat checkout/refund as mutations and invalidate sales, inventory, reports, and product stock queries afterward.

---

## UI/UX Direction

Use a professional operations dashboard style:

- Layout: sidebar on desktop, compact top/bottom navigation on small screens.
- Density: data-dense but readable. Avoid huge decorative whitespace.
- Colors: slate/neutral surfaces with emerald success/accent and clear amber/red status states.
- Icons: use one SVG icon family, preferably Lucide or Heroicons. No emojis as structural icons.
- Typography: system font is acceptable. If adding fonts, use one body font and one optional numeric/mono font for data.
- Data: tables/cards first; charts only where they clarify reports.

Suggested design tokens:

```css
:root {
  --color-primary: #334155;
  --color-on-primary: #ffffff;
  --color-secondary: #475569;
  --color-accent: #059669;
  --color-background: #f8fafc;
  --color-foreground: #0f172a;
  --color-muted: #f2f3f4;
  --color-border: #e6e8ea;
  --color-destructive: #dc2626;
  --color-ring: #334155;
}
```

Do not scatter raw hex values across components. Use Tailwind tokens/classes consistently.

---

## Shared Component Standards

Create shared UI only when at least two features need it or it is clearly foundational.

Good shared components:

- `Button`
- `Input`
- `Select`
- `Textarea`
- `Badge`
- `StatusBadge`
- `Card`
- `DataTable`
- `PageHeader`
- `Spinner`
- `Skeleton`
- `ErrorBanner`
- `EmptyState`
- `ConfirmDialog`
- `MoneyText`
- `DateTimeText`
- `RoleBadge`

Component rules:

- One component per file unless a tiny helper is private to that file.
- Props are typed with named interfaces.
- Buttons show loading state and disable duplicate submit during mutations.
- Destructive actions require confirmation.
- Every interactive element has visible focus state.
- Do not hide important actions behind hover-only UI.

---

## Page State Requirements

Every API-backed page must handle:

- Initial loading.
- Refetching/loading while stale data exists.
- Empty state with a useful next action.
- Error state with retry when possible.
- Unauthorized/forbidden state.
- Mutation pending state.
- Mutation success/failure feedback.

No silent blank screens. Blank screens are bugs wearing invisibility cloaks.

---

## Forms

- Use controlled inputs for small forms.
- Introduce React Hook Form + Zod only when forms become repetitive or validation logic gets noisy.
- Labels are always visible. Placeholder-only labels are banned.
- Show validation errors inline near the field.
- On submit failure with field errors, focus the first invalid field.
- Preserve form drafts while mutation is pending.
- Disable submit while request is in flight.
- For backend field errors, map by `fieldErrors[].field`.

Frontend validation should mirror backend constraints, not replace them.

---

## POS-Specific UX Rules

Checkout:

- Product search/select must be fast and keyboard-friendly.
- Cart line items show product name, SKU, unit price, quantity, and line total.
- Quantity cannot be less than 1.
- Show stock warnings before checkout when available.
- Payment method supports `CASH` and `CARD`.
- Cash payment shows amount tendered and calculated change.
- Successful checkout shows receipt response immediately.
- Do not mutate product prices client-side. Backend owns sale-time price snapshots.

Inventory:

- Low stock should be visually distinct with text + color/icon, not color alone.
- Stock adjustment must clearly show whether the quantity change is positive or negative.
- Show movement history for the selected product.

Reports:

- Format money consistently with `Intl.NumberFormat`.
- Format dates consistently with `Intl.DateTimeFormat`.
- Charts are optional. Tables/cards are acceptable and often clearer.
- Report pages must handle no-data states.

Refunds:

- Refund screen should be launched from sale detail/history.
- User selects refundable items and quantities.
- Explain conflict errors from backend, especially over-refund attempts.

---

## Accessibility and Responsive Rules

- Body text minimum: 16px.
- Normal text contrast: at least WCAG AA 4.5:1.
- Interactive target size: at least 44px high/wide where practical.
- Keyboard navigation must work for login, nav, forms, tables, checkout, and dialogs.
- Modal/dialog focus is trapped and returned to trigger on close.
- Use `aria-live="polite"` or equivalent for toast/status updates.
- Support `prefers-reduced-motion`.
- Test at 375px, 768px, 1024px, and desktop width.
- No horizontal scroll on mobile.
- Never disable browser zoom.

---

## Tailwind Rules

- If using Tailwind v4 with Vite, use the v4 setup:
  - add `tailwindcss()` in `vite.config.ts`
  - use `@import "tailwindcss";` in global CSS
- If using Tailwind v3, use the v3 config files. Do not mix versions.
- Prefer utility classes for layout and spacing.
- Extract component classes only when repetition is real.
- Use a consistent spacing rhythm: `2`, `3`, `4`, `6`, `8`, `12`.
- Do not use arbitrary values everywhere. Arbitrary values are escape hatches, not architecture.

---

## Hard Constraints

- No hardcoded API URLs outside `lib/api/client.ts`.
- No API calls from components.
- No JWT logs.
- No fake/mock data on a screen marked complete.
- No protected-content flash before redirect.
- No role checks scattered randomly through page components.
- No `any` for API responses.
- No snake_case assumptions. Backend uses Jackson camelCase unless OpenAPI proves otherwise.
- Do not assume paginated responses unless OpenAPI/backend code says so.
- Do not swallow API errors. Show useful feedback.

---

## Verification Checklist

Before calling frontend work complete:

```bash
npm run lint
npm run build
```

Also manually verify:

- Login succeeds with valid credentials.
- Invalid login shows backend error message.
- Refresh keeps/restores session with `/api/auth/me`.
- Logout clears token and cached data.
- `401` redirects to login.
- `403` shows not-authorized UI.
- Role nav differs correctly for `ADMIN`, `MANAGER`, and `CASHIER`.
- CRUD mutations update lists without manual refresh.
- Empty states render for empty lists.
- Loading and error states render for slow/failing calls.
- Checkout creates a receipt from real backend response.
- Inventory changes after checkout/adjustment/refund.
- Responsive layout works at mobile and desktop widths.

If backend is running and browser requests fail with Axios `Network Error`, check CORS before rewriting frontend logic:

```bash
curl -i http://localhost:18080/actuator/health
curl -i -X OPTIONS http://localhost:18080/api/auth/login \
  -H 'Origin: http://localhost:5173' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: content-type'
```

`403 Invalid CORS request` means fix backend CORS/restart backend. It is not a React bug. Shocking, I know.

---

## Agent Prompt for Building Frontend

When asked to build the frontend, follow this instruction block:

```text
Build Phase 11 frontend for RetailCore POS under frontend/.

Before coding, read FRONTEND.md, docs/frontend-build-plan.md, README.md, docs/project-spec.md, docs/phase-log.md, and inspect backend controllers/DTOs/OpenAPI.

Use React + Vite + TypeScript, Tailwind, React Router, Axios centralized API client, AuthContext, and TanStack Query for API-backed server state.

Follow docs/frontend-build-plan.md step by step:
1. FE-00 contract audit
2. FE-01 scaffold/configure app
3. FE-02 styling/shared UI foundation
4. FE-03 API client/error handling/auth storage/types
5. FE-04 providers/routing/protected shell/nav
6. FE-05 login/session restore/logout
7. FE-06 dashboard
8. FE-07 products/categories
9. FE-08 inventory
10. FE-09 checkout/receipt
11. FE-10 sales/refunds
12. FE-11 reports
13. FE-12 admin users
14. FE-13 final hardening

Build vertical slices, not placeholders. For every API-backed page, implement loading, error, empty, forbidden, and mutation states. Types must mirror backend DTOs. Verify with npm run lint and npm run build before final response.
```
