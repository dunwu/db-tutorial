/*
 sql 约束的 Mysql 示例
 */

-- --------------------------------------------------------------
-- DDL BEGIN
-- --------------------------------------------------------------

-- NOT NULL 约束
DROP TABLE IF EXISTS person;
CREATE TABLE person (
    person_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT,
    address VARCHAR(255)
);

-- UNIQUE 约束
DROP TABLE IF EXISTS person;
CREATE TABLE person (
    person_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT,
    address VARCHAR(255),
    UNIQUE (person_id)
);

-- PRIMARY KEY 约束
DROP TABLE IF EXISTS person;
CREATE TABLE person (
    person_id INT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT,
    address VARCHAR(255)
);

-- FOREIGN KEY 约束
DROP TABLE IF EXISTS bill;
CREATE TABLE bill
(
	bill_id INT NOT NULL,
	bill_no INT NOT NULL,
	person_id INT,
	PRIMARY KEY (bill_id),
	FOREIGN KEY (person_id) REFERENCES person(person_id)
);

-- CHECK 约束
DROP TABLE IF EXISTS person;
CREATE TABLE person (
    person_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT CHECK (age >= 18),
    address VARCHAR(255)
);

-- DEFAULT 约束
DROP TABLE IF EXISTS person;
CREATE TABLE person (
    person_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT DEFAULT 18,
    address VARCHAR(255)
);

-- --------------------------------------------------------------
-- DDL END
-- --------------------------------------------------------------