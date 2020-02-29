-- -----------------------------------------
-- Sams Teach Yourself SQL in 10 Minutes
-- http://forta.com/books/0672336073/
-- Example table creation scripts for MySQL.
-- -----------------------------------------

CREATE DATABASE IF NOT EXISTS db_tutorial;
USE db_tutorial;

-- ----------------------
-- Create Customers table
-- ----------------------
CREATE TABLE customers (
    cust_id      CHAR(10)  NOT NULL,
    cust_name    CHAR(50)  NOT NULL,
    cust_address CHAR(50)  NULL,
    cust_city    CHAR(50)  NULL,
    cust_state   CHAR(5)   NULL,
    cust_zip     CHAR(10)  NULL,
    cust_country CHAR(50)  NULL,
    cust_contact CHAR(50)  NULL,
    cust_email   CHAR(255) NULL
);

-- -----------------------
-- Create OrderItems table
-- -----------------------
CREATE TABLE orderitems (
    order_num  INT           NOT NULL,
    order_item INT           NOT NULL,
    prod_id    CHAR(10)      NOT NULL,
    quantity   INT           NOT NULL,
    item_price DECIMAL(8, 2) NOT NULL
);


-- -------------------
-- Create Orders table
-- -------------------
CREATE TABLE orders (
    order_num  INT      NOT NULL,
    order_date DATETIME NOT NULL,
    cust_id    CHAR(10) NOT NULL
);

-- ---------------------
-- Create Products table
-- ---------------------
CREATE TABLE products (
    prod_id    CHAR(10)      NOT NULL,
    vend_id    CHAR(10)      NOT NULL,
    prod_name  CHAR(255)     NOT NULL,
    prod_price DECIMAL(8, 2) NOT NULL,
    prod_desc  TEXT          NULL
);

-- --------------------
-- Create Vendors table
-- --------------------
CREATE TABLE vendors (
    vend_id      CHAR(10) NOT NULL,
    vend_name    CHAR(50) NOT NULL,
    vend_address CHAR(50) NULL,
    vend_city    CHAR(50) NULL,
    vend_state   CHAR(5)  NULL,
    vend_zip     CHAR(10) NULL,
    vend_country CHAR(50) NULL
);


-- -------------------
-- Define primary keys
-- -------------------
ALTER TABLE customers
    ADD PRIMARY KEY (cust_id);
ALTER TABLE orderitems
    ADD PRIMARY KEY (order_num, order_item);
ALTER TABLE orders
    ADD PRIMARY KEY (order_num);
ALTER TABLE products
    ADD PRIMARY KEY (prod_id);
ALTER TABLE vendors
    ADD PRIMARY KEY (vend_id);


-- -------------------
-- Define foreign keys
-- -------------------
ALTER TABLE orderitems
    ADD CONSTRAINT fk_orderitems_orders FOREIGN KEY (order_num) REFERENCES orders(order_num);
ALTER TABLE orderitems
    ADD CONSTRAINT fk_orderitems_products FOREIGN KEY (prod_id) REFERENCES products(prod_id);
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customers FOREIGN KEY (cust_id) REFERENCES customers(cust_id);
ALTER TABLE products
    ADD CONSTRAINT fk_products_vendors FOREIGN KEY (vend_id) REFERENCES vendors(vend_id);
