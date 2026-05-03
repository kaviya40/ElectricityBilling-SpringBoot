-- ============================================================
-- EBMS – Electricity Bill Management System
-- SQL Setup Script
-- Run this in MySQL Workbench before starting the application
-- ============================================================

CREATE DATABASE IF NOT EXISTS electricity_bill_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE electricity_bill_db;

-- Tables are auto-created by Spring Boot (ddl-auto=update)
-- This script inserts sample data AFTER first run

-- ── Sample Admin (password = admin123, BCrypt hashed) ──────────
-- Spring Boot DataInitializer creates this automatically.

-- ── Sample Customer User ─────────────────────────────────────
-- Register via UI at http://localhost:8080/auth/register

-- ── Verify Tables Created ────────────────────────────────────
-- After running the app once, verify:
-- SHOW TABLES;
-- Expected: users, customers, bills, payments

-- ── Useful Queries ───────────────────────────────────────────

-- View all users
-- SELECT id, username, full_name, email, role, enabled, created_at FROM users;

-- View all customers
-- SELECT c.id, c.name, c.meter_number, c.address, c.connection_type, u.username
-- FROM customers c LEFT JOIN users u ON c.user_id = u.id;

-- View all bills with customer info
-- SELECT b.id, b.bill_number, c.name, c.meter_number, b.units_consumed,
--        b.total_amount, b.billing_month, b.billing_year, b.status, b.due_date
-- FROM bills b JOIN customers c ON b.customer_id = c.id
-- ORDER BY b.created_at DESC;

-- Total Revenue
-- SELECT SUM(total_amount) FROM bills WHERE status = 'PAID';

-- Pending Bills
-- SELECT COUNT(*) FROM bills WHERE status = 'PENDING';

-- Overdue Bills (due date passed but not paid)
-- SELECT * FROM bills WHERE status = 'PENDING' AND due_date < CURDATE();
