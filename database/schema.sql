-- ========================================================
-- Student Management System Database Schema
-- Database: student_management_db
-- ========================================================

CREATE DATABASE IF NOT EXISTS student_management_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_management_db;

-- Drop table if exists for fresh setup (optional)
DROP TABLE IF EXISTS students;

-- Create students table
CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(30) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    course VARCHAR(80) NOT NULL,
    year_level INT NOT NULL,
    gpa DECIMAL(3, 2) NOT NULL DEFAULT 3.00,
    city VARCHAR(60) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course (course),
    INDEX idx_status (status),
    INDEX idx_roll (roll_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed initial student records
INSERT INTO students (roll_number, first_name, last_name, email, phone, gender, course, year_level, gpa, city, status) VALUES
('SMS2026-001', 'Sri', 'Pallav', 'sripallav@example.com', '9876543210', 'Male', 'Computer Science', 3, 3.92, 'Hyderabad', 'Active'),
('SMS2026-002', 'Aarav', 'Sharma', 'aarav.sharma@example.com', '9123456780', 'Male', 'Artificial Intelligence', 2, 3.85, 'Bengaluru', 'Active'),
('SMS2026-003', 'Ananya', 'Reddy', 'ananya.reddy@example.com', '9234567891', 'Female', 'Data Science', 4, 3.98, 'Hyderabad', 'Active'),
('SMS2026-004', 'Rohan', 'Verma', 'rohan.verma@example.com', '9345678912', 'Male', 'Information Technology', 1, 3.40, 'Pune', 'Active'),
('SMS2026-005', 'Priya', 'Nair', 'priya.nair@example.com', '9456789123', 'Female', 'Computer Science', 3, 3.75, 'Chennai', 'Active'),
('SMS2026-006', 'Karthik', 'Iyer', 'karthik.iyer@example.com', '9567891234', 'Male', 'Cyber Security', 2, 3.60, 'Mumbai', 'Active'),
('SMS2026-007', 'Sneha', 'Patel', 'sneha.patel@example.com', '9678912345', 'Female', 'Electronics', 4, 3.88, 'Ahmedabad', 'Active'),
('SMS2026-008', 'Vikram', 'Rao', 'vikram.rao@example.com', '9789012345', 'Male', 'Data Science', 2, 3.15, 'Delhi', 'Inactive'),
('SMS2026-009', 'Meera', 'Joshi', 'meera.joshi@example.com', '9890123456', 'Female', 'Artificial Intelligence', 1, 3.95, 'Pune', 'Active'),
('SMS2026-010', 'Arjun', 'Das', 'arjun.das@example.com', '9901234567', 'Male', 'Computer Science', 4, 3.50, 'Kolkata', 'Active');
