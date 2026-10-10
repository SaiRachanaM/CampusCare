-- Campus Maintenance & Accountability System Database Schema
-- Database Target: MySQL 8.0+

CREATE DATABASE IF NOT EXISTS campus_maintenance_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_maintenance_db;

-- 1. App User Table
CREATE TABLE IF NOT EXISTS app_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'MAINTENANCE_STAFF'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Location Table
CREATE TABLE IF NOT EXISTS location (
    location_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    building VARCHAR(100) NOT NULL,
    floor VARCHAR(50) NOT NULL,
    room VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_location_building_floor_room UNIQUE (building, floor, room)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Asset Table
CREATE TABLE IF NOT EXISTS asset (
    asset_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_name VARCHAR(150) NOT NULL,
    asset_type VARCHAR(100) NOT NULL,
    location_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'FUNCTIONAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asset_location FOREIGN KEY (location_id) REFERENCES location (location_id) ON DELETE RESTRICT,
    CONSTRAINT chk_asset_status CHECK (status IN ('FUNCTIONAL', 'UNDER_MAINTENANCE', 'FAULTY', 'DECOMMISSIONED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Complaint Table
CREATE TABLE IF NOT EXISTS complaint (
    complaint_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reported_by BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    asset_id BIGINT NULL,
    title VARCHAR(200) NOT NULL,
    issue TEXT NOT NULL,
    priority VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REPORTED',
    assigned_to BIGINT NULL,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    closed_at TIMESTAMP NULL,
    due_at TIMESTAMP NULL,
    CONSTRAINT fk_complaint_user_reporter FOREIGN KEY (reported_by) REFERENCES app_user (user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_complaint_location FOREIGN KEY (location_id) REFERENCES location (location_id) ON DELETE RESTRICT,
    CONSTRAINT fk_complaint_asset FOREIGN KEY (asset_id) REFERENCES asset (asset_id) ON DELETE RESTRICT,
    CONSTRAINT fk_complaint_user_assigned FOREIGN KEY (assigned_to) REFERENCES app_user (user_id) ON DELETE RESTRICT,
    CONSTRAINT chk_complaint_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT chk_complaint_status CHECK (status IN ('REPORTED', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Repair Table (Enforces 1:1 relationship with complaint via UNIQUE constraint on complaint_id)
CREATE TABLE IF NOT EXISTS repair (
    repair_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL UNIQUE,
    repair_date DATETIME NOT NULL,
    description TEXT NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    recorded_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_repair_complaint FOREIGN KEY (complaint_id) REFERENCES complaint (complaint_id) ON DELETE RESTRICT,
    CONSTRAINT fk_repair_user_recorded FOREIGN KEY (recorded_by) REFERENCES app_user (user_id) ON DELETE RESTRICT,
    CONSTRAINT chk_repair_cost CHECK (cost >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Status History Table (Preserves historical lifecycle logs)
CREATE TABLE IF NOT EXISTS status_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remarks TEXT NULL,
    CONSTRAINT fk_status_history_complaint FOREIGN KEY (complaint_id) REFERENCES complaint (complaint_id) ON DELETE RESTRICT,
    CONSTRAINT fk_status_history_user_changed FOREIGN KEY (changed_by) REFERENCES app_user (user_id) ON DELETE RESTRICT,
    CONSTRAINT chk_history_status CHECK (status IN ('REPORTED', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Data (Default test passwords are BCrypt hashes of 'Password@123')
INSERT INTO app_user (name, email, password, role) VALUES
('John Student', 'john.student@campus.edu', '$2a$10$e7xX3YhG.00pM7K2e8Nn0.Kx1X2Y3Z4A5B6C7D8E9F0G1H2I3J4K5', 'USER'),
('Alice Faculty', 'alice.faculty@campus.edu', '$2a$10$e7xX3YhG.00pM7K2e8Nn0.Kx1X2Y3Z4A5B6C7D8E9F0G1H2I3J4K5', 'USER'),
('Bob Technician', 'bob.tech@campus.edu', '$2a$10$e7xX3YhG.00pM7K2e8Nn0.Kx1X2Y3Z4A5B6C7D8E9F0G1H2I3J4K5', 'MAINTENANCE_STAFF'),
('Sarah Supervisor', 'sarah.staff@campus.edu', '$2a$10$e7xX3YhG.00pM7K2e8Nn0.Kx1X2Y3Z4A5B6C7D8E9F0G1H2I3J4K5', 'MAINTENANCE_STAFF')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO location (building, floor, room) VALUES
('Science Block', '2nd Floor', 'Lab 204'),
('Main Library', '1st Floor', 'Reading Room A'),
('Student Hostel A', '3rd Floor', 'Room 312'),
('Engineering Building', 'Ground Floor', 'Auditorium')
ON DUPLICATE KEY UPDATE building=VALUES(building);

INSERT INTO asset (asset_name, asset_type, location_id, status) VALUES
('Lab Projector Sony HD', 'Electronics', 1, 'UNDER_MAINTENANCE'),
('AC Split Unit 2.0 Ton', 'HVAC', 1, 'FAULTY'),
('Library Ceiling Fan #3', 'Electrical', 2, 'FUNCTIONAL'),
('Hostel Room 312 Door Lock', 'Hardware', 3, 'FAULTY')
ON DUPLICATE KEY UPDATE asset_name=VALUES(asset_name);

INSERT INTO complaint (reported_by, location_id, asset_id, title, issue, priority, status, assigned_to, reported_at, due_at) VALUES
(1, 1, 1, 'Projector Display Flicker', 'The projector display flickers violently after 10 minutes of usage in Lab 204.', 'HIGH', 'IN_PROGRESS', 3, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY)),
(2, 2, 3, 'Water Leakage Near Window', 'Minor water seepage near window sill during heavy rainfall in Reading Room A.', 'LOW', 'REPORTED', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY))
ON DUPLICATE KEY UPDATE title=VALUES(title);

INSERT INTO status_history (complaint_id, status, changed_by, remarks) VALUES
(1, 'REPORTED', 1, 'Complaint submitted by John Student.'),
(1, 'ASSIGNED', 4, 'Assigned to Bob Technician by Sarah Supervisor.'),
(1, 'IN_PROGRESS', 3, 'Technician inspected projector power supply and ordered replacement lamp.')
ON DUPLICATE KEY UPDATE status=VALUES(status);
