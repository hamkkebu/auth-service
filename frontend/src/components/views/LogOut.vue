<template>
  <div class="logout-container">
    <div class="logout-card">
      <div class="logout-spinner"></div>
      <p class="logout-text">로그아웃 중...</p>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted } from 'vue';
import { useAuth } from '@/composables/useAuth';

/**
 * 로그아웃 페이지
 *
 * 크로스 도메인 로그아웃을 지원합니다.
 * ledger-service 등 외부 서비스에서 로그아웃 시 이 페이지로 리다이렉트되면
 * auth-service의 localStorage도 함께 정리됩니다.
 */
export default defineComponent({
  name: 'LogOut',
  setup() {
    const { logout } = useAuth();

    onMounted(async () => {
      // 로그아웃 처리 (Keycloak 세션 종료 + localStorage 정리)
      await logout('/login');
    });

    return {};
  },
});
</script>

<style scoped>
.logout-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary, #0a0a0f);
}

.logout-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
}

.logout-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.logout-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  margin: 0;
}
</style>
