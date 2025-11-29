-- Admin account for development environment
-- Password: admin123! (BCrypt hashed)
-- This admin account is automatically created on application startup

INSERT INTO users (username, email, password_hash, first_name, last_name, is_active, is_verified, role, created_at, updated_at)
SELECT 'admin', 'admin@hamkkebu.com', '$2a$12$v6Eiy2TaGS.KLTbhXuhcseUdrTlfcCgUOV8W6WI8fAmTMa3IvNKzi', 'Admin', 'User', TRUE, TRUE, 'ADMIN', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
