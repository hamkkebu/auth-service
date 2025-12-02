-- Create database if not exists
CREATE DATABASE IF NOT EXISTS hamkkebu_auth;
USE hamkkebu_auth;

-- Drop existing tables
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS tbl_outbox_event;

-- Create users table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    keycloak_user_id VARCHAR(36) UNIQUE,          -- Keycloak SSO 연동용 (sub claim)
    password_hash VARCHAR(255),                    -- Keycloak 사용자는 NULL 가능
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    nickname VARCHAR(50),
    phone_number VARCHAR(20),
    country VARCHAR(50),
    city VARCHAR(50),
    state VARCHAR(50),
    street_address VARCHAR(200),
    street_address2 VARCHAR(200),
    postal_code VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    -- BaseEntity fields (auditing and soft delete)
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_keycloak_user_id (keycloak_user_id),
    INDEX idx_is_active (is_active),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Transactional Outbox Event table
CREATE TABLE tbl_outbox_event (
    -- Primary Key
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Event Information
    event_id VARCHAR(36) NOT NULL UNIQUE,      -- UUID 형태의 이벤트 고유 ID
    event_type VARCHAR(100) NOT NULL,          -- 이벤트 타입 (예: USER_CREATED)
    topic VARCHAR(100) NOT NULL,               -- Kafka 토픽명
    resource_id VARCHAR(100) NOT NULL,         -- 리소스 ID (Kafka 파티션 키)

    -- Event Payload (JSON)
    payload JSON NOT NULL,                     -- 전체 이벤트 객체를 JSON으로 직렬화

    -- Status Management
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, PUBLISHED, FAILED
    retry_count INT NOT NULL DEFAULT 0,        -- 재시도 횟수
    max_retry INT NOT NULL DEFAULT 3,          -- 최대 재시도 횟수

    -- Error Tracking
    error_message TEXT,                        -- 실패 시 에러 메시지

    -- Timestamps
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME,                     -- 발행 완료 시각
    last_retry_at DATETIME,                    -- 마지막 재시도 시각

    -- Optimistic Locking
    version BIGINT DEFAULT 0,                  -- 낙관적 락을 위한 버전 필드

    -- Indexes
    INDEX idx_status_created (status, created_at),
    INDEX idx_event_id (event_id),
    INDEX idx_topic (topic)
) COMMENT='Transactional Outbox 이벤트 테이블 - DB 트랜잭션과 이벤트 발행의 원자성 보장';
