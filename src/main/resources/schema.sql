-- ================================================
-- LUXURY CAR RENTAL SYSTEM - DATABASE SCHEMA
-- PostgreSQL / Neon Cloud Database
-- ================================================

-- Drop tables if they exist (in correct order due to foreign keys)
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS cars CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop types if they exist
DROP TYPE IF EXISTS account_status CASCADE;
DROP TYPE IF EXISTS car_status CASCADE;
DROP TYPE IF EXISTS car_category CASCADE;
DROP TYPE IF EXISTS booking_status CASCADE;

-- ================================================
-- CUSTOM ENUM TYPES
-- ================================================

-- User account status
CREATE TYPE account_status AS ENUM ('ACTIVE', 'PENDING_ACTIVATION', 'DISABLED', 'SUSPENDED');

-- Car availability status
CREATE TYPE car_status AS ENUM ('AVAILABLE', 'BOOKED', 'MAINTENANCE');

-- Car category/type
CREATE TYPE car_category AS ENUM ('SUV', 'SEDAN', 'LUXURY', 'SPORTS', 'CONVERTIBLE');

-- Booking lifecycle status
CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED');

-- ================================================
-- ROLES TABLE
-- ================================================

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(100),
    
    CONSTRAINT chk_role_name CHECK (name IN ('ROLE_ADMIN', 'ROLE_CUSTOMER'))
);

-- Index for role lookup by name
CREATE INDEX idx_role_name ON roles(name);

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
    ('ROLE_ADMIN', 'Administrator - Full system access'),
    ('ROLE_CUSTOMER', 'Customer - Can book cars and view own bookings');

-- ================================================
-- USERS TABLE
-- ================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    address VARCHAR(255),
    drivers_license VARCHAR(50),
    account_status VARCHAR(20) NOT NULL DEFAULT 'PENDING_ACTIVATION',
    activation_token VARCHAR(64),
    activation_token_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_account_status CHECK (account_status IN ('ACTIVE', 'PENDING_ACTIVATION', 'DISABLED', 'SUSPENDED'))
);

-- Indexes for users table
CREATE UNIQUE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_status ON users(account_status);
CREATE INDEX idx_user_created ON users(created_at);
CREATE INDEX idx_user_activation_token ON users(activation_token) WHERE activation_token IS NOT NULL;

-- ================================================
-- USER_ROLES (Many-to-Many Join Table)
-- ================================================

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    
    PRIMARY KEY (user_id, role_id),
    
    CONSTRAINT fk_user_roles_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_user_roles_role 
        FOREIGN KEY (role_id) 
        REFERENCES roles(id) 
        ON DELETE CASCADE
);

-- Indexes for user_roles
CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- ================================================
-- CARS TABLE
-- ================================================

CREATE TABLE cars (
    id BIGSERIAL PRIMARY KEY,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    color VARCHAR(30),
    category VARCHAR(20) NOT NULL,
    price_per_day DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    description TEXT,
    image_url VARCHAR(500),
    seats INTEGER DEFAULT 4,
    transmission VARCHAR(20) DEFAULT 'Automatic',
    fuel_type VARCHAR(20) DEFAULT 'Petrol',
    mileage INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_car_category CHECK (category IN ('SUV', 'SEDAN', 'LUXURY', 'SPORTS', 'CONVERTIBLE')),
    CONSTRAINT chk_car_status CHECK (status IN ('AVAILABLE', 'BOOKED', 'MAINTENANCE')),
    CONSTRAINT chk_price_positive CHECK (price_per_day > 0),
    CONSTRAINT chk_year_valid CHECK (year >= 1900 AND year <= 2100),
    CONSTRAINT chk_seats_valid CHECK (seats > 0 AND seats <= 20)
);

-- Indexes for cars table
CREATE UNIQUE INDEX idx_car_license_plate ON cars(license_plate);
CREATE INDEX idx_car_status ON cars(status);
CREATE INDEX idx_car_category ON cars(category);
CREATE INDEX idx_car_brand ON cars(brand);
CREATE INDEX idx_car_available ON cars(status, category);
CREATE INDEX idx_car_price ON cars(price_per_day);

-- ================================================
-- BOOKINGS TABLE
-- ================================================

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_reference VARCHAR(20) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    pickup_location VARCHAR(255),
    dropoff_location VARCHAR(255),
    price_per_day DECIMAL(10, 2) NOT NULL,
    total_days INTEGER NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_booking_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT fk_booking_car 
        FOREIGN KEY (car_id) 
        REFERENCES cars(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT chk_booking_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT chk_dates_valid CHECK (end_date >= start_date),
    CONSTRAINT chk_total_days_positive CHECK (total_days > 0),
    CONSTRAINT chk_prices_positive CHECK (price_per_day > 0 AND total_price > 0)
);

-- Indexes for bookings table
CREATE UNIQUE INDEX idx_booking_reference ON bookings(booking_reference);
CREATE INDEX idx_booking_user ON bookings(user_id);
CREATE INDEX idx_booking_car ON bookings(car_id);
CREATE INDEX idx_booking_status ON bookings(status);
CREATE INDEX idx_booking_dates ON bookings(start_date, end_date);
CREATE INDEX idx_booking_car_dates ON bookings(car_id, start_date, end_date);
CREATE INDEX idx_booking_created ON bookings(created_at);

-- ================================================
-- FUNCTIONS & TRIGGERS
-- ================================================
-- Note: Functions and triggers are created separately to avoid SQL parser issues
-- with dollar-quoted strings. They can be created manually or through a separate
-- initialization mechanism if needed.

-- ================================================
-- SAMPLE DATA (Optional - for testing)
-- ================================================

-- Note: Passwords are BCrypt hashed. Default password: 'Password123!'
-- In production, create users through the API

-- Sample admin user (password: 'Admin@123')
INSERT INTO users (first_name, last_name, email, password, phone_number, account_status)
VALUES (
    'System',
    'Admin',
    'admin@luxuryrentals.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G4E1Md/T.qxCRe',
    '+1-555-0100',
    'ACTIVE'
);

-- Assign admin role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.email = 'admin@luxuryrentals.com' AND r.name = 'ROLE_ADMIN';

-- Sample luxury cars
INSERT INTO cars (brand, model, year, license_plate, color, category, price_per_day, status, description, seats, transmission, fuel_type) VALUES
    ('Mercedes-Benz', 'S-Class S580', 2024, 'LUX-001', 'Black', 'LUXURY', 599.99, 'AVAILABLE', 'The pinnacle of luxury sedans with cutting-edge technology and supreme comfort.', 5, 'Automatic', 'Hybrid'),
    ('BMW', '7 Series 760i', 2024, 'LUX-002', 'Alpine White', 'LUXURY', 549.99, 'AVAILABLE', 'Ultimate driving luxury with powerful V8 engine and executive features.', 5, 'Automatic', 'Petrol'),
    ('Rolls-Royce', 'Ghost', 2023, 'LUX-003', 'English White', 'LUXURY', 1299.99, 'AVAILABLE', 'The ultimate expression of luxury and refinement.', 5, 'Automatic', 'Petrol'),
    ('Bentley', 'Continental GT', 2024, 'LUX-004', 'Glacier White', 'SPORTS', 899.99, 'AVAILABLE', 'Grand touring excellence with handcrafted luxury.', 4, 'Automatic', 'Petrol'),
    ('Porsche', '911 Turbo S', 2024, 'SPT-001', 'Guards Red', 'SPORTS', 699.99, 'AVAILABLE', 'Legendary sports car performance with everyday usability.', 4, 'Automatic', 'Petrol'),
    ('Ferrari', 'Roma', 2024, 'SPT-002', 'Rosso Corsa', 'SPORTS', 999.99, 'AVAILABLE', 'Italian excellence in design and performance.', 2, 'Automatic', 'Petrol'),
    ('Lamborghini', 'Huracán EVO', 2024, 'SPT-003', 'Giallo Orion', 'SPORTS', 1199.99, 'AVAILABLE', 'Breathtaking V10 performance and striking design.', 2, 'Automatic', 'Petrol'),
    ('Range Rover', 'Autobiography', 2024, 'SUV-001', 'Santorini Black', 'SUV', 449.99, 'AVAILABLE', 'The ultimate luxury SUV for any terrain.', 5, 'Automatic', 'Diesel'),
    ('Mercedes-Benz', 'G-Class G63 AMG', 2024, 'SUV-002', 'Obsidian Black', 'SUV', 599.99, 'AVAILABLE', 'Iconic design meets AMG performance.', 5, 'Automatic', 'Petrol'),
    ('Porsche', 'Cayenne Turbo GT', 2024, 'SUV-003', 'Jet Black', 'SUV', 549.99, 'AVAILABLE', 'Sports car performance in an SUV package.', 5, 'Automatic', 'Petrol'),
    ('Audi', 'A8 L', 2024, 'SED-001', 'Mythos Black', 'SEDAN', 399.99, 'AVAILABLE', 'Progressive luxury with cutting-edge technology.', 5, 'Automatic', 'Hybrid'),
    ('Lexus', 'LS 500h', 2024, 'SED-002', 'Sonic White', 'SEDAN', 349.99, 'AVAILABLE', 'Japanese craftsmanship meets hybrid efficiency.', 5, 'Automatic', 'Hybrid'),
    ('Maserati', 'GranCabrio', 2024, 'CNV-001', 'Blu Sofisticato', 'CONVERTIBLE', 799.99, 'AVAILABLE', 'Italian open-top luxury with V6 power.', 4, 'Automatic', 'Petrol'),
    ('Mercedes-Benz', 'AMG SL 63', 2024, 'CNV-002', 'Patagonia Red', 'CONVERTIBLE', 749.99, 'AVAILABLE', 'Performance convertible with luxurious appointments.', 2, 'Automatic', 'Petrol'),
    ('BMW', 'M8 Competition', 2024, 'CNV-003', 'Marina Bay Blue', 'CONVERTIBLE', 699.99, 'MAINTENANCE', 'Ultimate M performance in convertible form.', 4, 'Automatic', 'Petrol');

-- ================================================
-- USEFUL QUERIES (for reference)
-- ================================================

-- Find available cars for a date range (no conflicting bookings)
-- SELECT c.* FROM cars c 
-- WHERE c.status = 'AVAILABLE' 
-- AND c.id NOT IN (
--     SELECT b.car_id FROM bookings b 
--     WHERE b.status IN ('PENDING', 'CONFIRMED') 
--     AND b.start_date <= '2024-12-31' 
--     AND b.end_date >= '2024-12-25'
-- );

-- Check for double booking
-- SELECT COUNT(*) > 0 FROM bookings b 
-- WHERE b.car_id = 1 
-- AND b.status IN ('PENDING', 'CONFIRMED') 
-- AND b.start_date <= '2024-12-31' 
-- AND b.end_date >= '2024-12-25';

-- Get booking statistics
-- SELECT 
--     status,
--     COUNT(*) as count,
--     SUM(total_price) as total_revenue
-- FROM bookings 
-- GROUP BY status;
