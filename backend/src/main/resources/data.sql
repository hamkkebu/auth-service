-- Admin account for development environment
-- Password: admin123! (BCrypt hashed)
-- This admin account is automatically created on application startup

INSERT INTO users (username, email, password_hash, first_name, last_name, is_active, is_verified, role, created_at, updated_at)
SELECT 'admin', 'admin@hamkkebu.com', '$2a$12$v6Eiy2TaGS.KLTbhXuhcseUdrTlfcCgUOV8W6WI8fAmTMa3IvNKzi', 'Admin', 'User', TRUE, TRUE, 'ADMIN', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Outbox event for admin user registration
-- This event will be published to Kafka and consumed by ledger-service
-- ledger-service will sync the admin user via gRPC
INSERT INTO tbl_outbox_event (event_id, event_type, topic, resource_id, payload, status, retry_count, max_retry, created_at)
SELECT
    'admin-user-registered-event-001',
    'USER_REGISTERED',
    'user.events',
    '1',
    JSON_OBJECT(
        'eventId', 'admin-user-registered-event-001',
        'eventType', 'USER_REGISTERED',
        'eventVersion', '1.0',
        'resourceId', '1',
        'occurredAt', DATE_FORMAT(NOW(), '%Y-%m-%dT%H:%i:%s'),
        'userId', '1',
        'metadata', NULL,
        'userPk', 1
    ),
    'PENDING',
    0,
    3,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM tbl_outbox_event WHERE event_id = 'admin-user-registered-event-001');
