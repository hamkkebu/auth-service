-- Insert sample users (for development only)
-- Note: In production, passwords should be properly hashed using BCrypt or similar

INSERT INTO users (username, email, password_hash, first_name, last_name, phone_number, country, city, state, is_active, is_verified) VALUES
('k1m743hyun', 'k1m743hyun@hamkkebu.com', 'temp_password_123', 'Taehyun', 'Kim', '+82 10-1234-5678', '대한민국', '수원시', '경기도', TRUE, TRUE),
('admin', 'admin@hamkkebu.com', 'admin123', 'Admin', 'User', '010-1234-5678', 'South Korea', 'Seoul', 'Seoul', TRUE, TRUE),
('testuser', 'test@hamkkebu.com', 'test123', 'Test', 'User', '010-9876-5432', 'South Korea', 'Busan', 'Busan', TRUE, FALSE),
('demo', 'demo@hamkkebu.com', 'demo123', 'Demo', 'User', '010-5555-6666', 'South Korea', 'Incheon', 'Incheon', TRUE, TRUE);

-- Insert sample roles
INSERT INTO user_roles (user_id, role_name) VALUES
(1, 'ADMIN'),
(1, 'USER'),
(2, 'ADMIN'),
(2, 'USER'),
(3, 'USER'),
(4, 'USER');
