-- Keycloak 전환 이후 초기 유저 시드 데이터는 불필요
-- 유저는 Keycloak 가입/로그인 시 JIT Provisioning(KeycloakJitProvisioningFilter)으로 자동 생성됨
-- Keycloak Admin 콘솔: admin / Hamkkebu2024! (boilerplate/docker-compose.yml 참조)
SELECT 1;
