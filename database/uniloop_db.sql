-- Create the UniLoop database
CREATE DATABASE IF NOT EXISTS uniloop_db;

-- Select the database
USE uniloop_db;


-- =========================
-- USERS TABLE
-- =========================

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================
-- CATEGORIES TABLE
-- =========================

CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE
);


-- Add initial categories
INSERT INTO categories (category_name)
VALUES
('Books'),
('Electronics'),
('Stationery'),
('Clothing'),
('Hostel Items'),
('Sports & Fitness'),
('Other');


-- =========================
-- ITEMS TABLE
-- =========================

CREATE TABLE items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,

    seller_id INT NOT NULL,
    category_id INT NOT NULL,

    item_name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    image_path VARCHAR(255),

    item_condition VARCHAR(50),
    listing_type VARCHAR(20) NOT NULL,
    item_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',

    date_posted TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (seller_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);


-- =========================
-- REQUESTS TABLE
-- =========================

CREATE TABLE requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,

    item_id INT NOT NULL,
    buyer_id INT NOT NULL,

    request_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (item_id) REFERENCES items(item_id),
    FOREIGN KEY (buyer_id) REFERENCES users(user_id)
);


-- =========================
-- TRANSACTIONS TABLE
-- =========================

CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,

    request_id INT NOT NULL,
    item_id INT NOT NULL,
    buyer_id INT NOT NULL,
    seller_id INT NOT NULL,

    transaction_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    completion_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (request_id) REFERENCES requests(request_id),
    FOREIGN KEY (item_id) REFERENCES items(item_id),
    FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    FOREIGN KEY (seller_id) REFERENCES users(user_id)
);