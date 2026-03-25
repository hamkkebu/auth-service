-- Admin 초기 시드 데이터
-- Keycloak realm-export.json에 admin 계정이 포함되어 있으므로
-- auth-service 기동 시 tbl_users에 admin을 미리 적재하고
-- Outbox 이벤트를 통해 다른 서비스에도 전파하여 모든 서비스에서 user_id=1을 보장한다.
-- 이후 admin이 첫 로그인하면 KeycloakUserSyncService가 username으로 매칭하여 keycloak_user_id를 연결한다.

INSERT IGNORE INTO tbl_users (user_id, username, email, first_name, last_name, password_hash, is_active, is_verified, user_role, created_by, updated_by)
VALUES (1, 'admin', 'admin@hamkkebu.com', 'Admin', 'User', 'KEYCLOAK_MANAGED', TRUE, TRUE, 'ADMIN', 'system', 'system');

-- Outbox 이벤트: USER_REGISTERED → 다른 서비스가 Kafka로 수신하여 admin 사용자 생성
INSERT IGNORE INTO tbl_outbox_event (event_id, event_type, topic, resource_id, payload, event_status)
VALUES (
    'admin-seed-user-registered-event',
    'USER_REGISTERED',
    'user.events',
    '1',
    JSON_OBJECT(
        'eventId', 'admin-seed-user-registered-event',
        'eventType', 'USER_REGISTERED',
        'eventVersion', '1.0',
        'resourceId', '1',
        'userPk', 1,
        'occurredAt', DATE_FORMAT(NOW(), '%Y-%m-%dT%H:%i:%s'),
        'userId', '1',
        'metadata', NULL
    ),
    'PENDING'
);
