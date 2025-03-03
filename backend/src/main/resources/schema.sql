DROP DATABASE IF EXISTS taste_account_db;
CREATE DATABASE taste_account_db;
USE taste_account_db;
DROP TABLE IF EXISTS tbl_account_taste;
CREATE TABLE tbl_account_taste (
    taste_account_num BIGINT PRIMARY KEY AUTO_INCREMENT,
    taste_account_id VARCHAR(20) UNIQUE NOT NULL,
    taste_account_fname VARCHAR(20),
    taste_account_lname VARCHAR(20),
    taste_account_nickname VARCHAR(20),
    taste_account_country VARCHAR(20),
    taste_account_city VARCHAR(20),
    taste_account_state VARCHAR(20),
    taste_account_street1 VARCHAR(100),
    taste_account_street2 VARCHAR(100),
    taste_account_zip VARCHAR(10),
    taste_account_email VARCHAR(100),
    taste_account_phone VARCHAR(20),
    taste_account_id_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    taste_account_id_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);