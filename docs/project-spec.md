# RetailCore POS Project Specification

## Goal

Build a mini POS system for small retail stores with product management, inventory tracking, sales checkout, receipts, and reporting.

## Tech Stack

- Java 25
- Spring Boot 4
- Spring Security
- JWT authentication
- Spring Data JPA
- PostgreSQL
- React + Tailwind CSS
- Docker Compose

## Roles

- Admin: full system access, user management, reports
- Manager: product, inventory, sales, reports
- Cashier: checkout and receipt operations

## Main Modules

1. Authentication and authorization
2. Product management
3. Category management
4. Inventory management
5. Sales and checkout
6. Payment management
7. Receipt generation
8. Reporting dashboard

## Important Business Rules

- SKU/barcode must be unique.
- Stock cannot become negative.
- Every stock change must create a stock movement record.
- Sale item price must be copied at sale time.
- Completed sales should not be edited.
- Refunds must not exceed the original sale amount.
- Passwords must be hashed with BCrypt.
