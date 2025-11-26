<template>
  <div class="login-container">
    <!-- Floating Orbs Background -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <div class="login-card">
      <!-- Logo/Brand -->
      <div class="login-brand">
        <div class="brand-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
            <path d="M2 17l10 5 10-5"></path>
            <path d="M2 12l10 5 10-5"></path>
          </svg>
        </div>
      </div>

      <div class="login-header">
        <h1 class="login-title">Welcome back</h1>
        <p class="login-subtitle">계정에 로그인하여 계속하세요</p>
      </div>

      <form class="login-form" @submit.prevent="logInSubmit">
        <div class="input-group">
          <label for="userId" class="input-label">아이디</label>
          <div class="input-wrapper">
            <div class="input-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </div>
            <input
              id="userId"
              type="text"
              class="input-field"
              placeholder="아이디를 입력하세요"
              v-model="user_id"
              required
            />
            <div class="input-glow"></div>
          </div>
        </div>

        <div class="input-group">
          <label for="userPassword" class="input-label">비밀번호</label>
          <div class="input-wrapper">
            <div class="input-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </div>
            <input
              id="userPassword"
              :type="showPassword ? 'text' : 'password'"
              class="input-field"
              placeholder="비밀번호를 입력하세요"
              v-model="user_pw"
              required
            />
            <button type="button" class="password-toggle" @click="showPassword = !showPassword">
              <svg v-if="!showPassword" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                <line x1="1" y1="1" x2="23" y2="23"></line>
              </svg>
            </button>
            <div class="input-glow"></div>
          </div>
        </div>

        <button type="submit" class="btn-login" :disabled="loading">
          <span v-if="!loading">로그인</span>
          <span v-else class="loading-spinner"></span>
        </button>

        <div class="divider">
          <span>또는</span>
        </div>

        <div class="signup-link">
          계정이 없으신가요?
          <a href="#" @click.prevent="goToSignup">회원가입</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuth } from '@/composables/useAuth';
import apiClient from '@/api/client';
import { useApi } from '@/composables/useApi';
import { API_ENDPOINTS, ROUTES } from '@/constants';
import type { LoginResponse } from '@/types/domain.types';

export default defineComponent({
  name: 'LogIn',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const { login } = useAuth();
    const { loading, execute } = useApi<LoginResponse>();

    const prefilledUsername = (route.query.username as string) || '';
    const user_id = ref(prefilledUsername);
    const user_pw = ref('');
    const showPassword = ref(false);

    const logInSubmit = async () => {
      if (!user_id.value) {
        alert('아이디를 입력해주세요.');
        return;
      }

      if (!user_pw.value) {
        alert('비밀번호를 입력해주세요.');
        return;
      }

      try {
        const response = await apiClient.post(API_ENDPOINTS.AUTH.LOGIN, {
          username: user_id.value,
          password: user_pw.value,
        });

        const data = response.data.data;

        login(
          {
            username: data.username,
            firstName: data.firstName,
            lastName: data.lastName,
            email: data.email ?? undefined,
          },
          data.token.accessToken,
          data.token.refreshToken
        );

        router.push(ROUTES.USER_INFO);
      } catch (error: any) {
        if (error.response?.data?.error?.code === 'USER-101') {
          if (confirm('등록되지 않은 아이디입니다. 회원가입을 진행하시겠습니까?')) {
            router.push({
              path: ROUTES.SIGNUP,
              query: {
                username: user_id.value,
                password: user_pw.value,
              },
            });
          }
        } else {
          const errorMessage = error.response?.data?.error?.message || '로그인에 실패했습니다.';
          alert(errorMessage);
        }
      }
    };

    const goToSignup = () => {
      router.push(ROUTES.SIGNUP);
    };

    return {
      user_id,
      user_pw,
      showPassword,
      loading,
      logInSubmit,
      goToSignup,
    };
  },
});
</script>

<style scoped>
.login-container {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

/* Floating Orbs */
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: -50px;
  right: -50px;
  animation-delay: -5s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(30px, -30px) scale(1.05);
  }
  50% {
    transform: translate(-20px, 20px) scale(0.95);
  }
  75% {
    transform: translate(-30px, -20px) scale(1.02);
  }
}

.login-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 48px 40px;
  width: 100%;
  max-width: 420px;
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;
}

.login-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 24px;
  padding: 1px;
  background: linear-gradient(135deg, rgba(255,255,255,0.1), rgba(255,255,255,0.02));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-brand {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.brand-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.brand-icon svg {
  width: 28px;
  height: 28px;
  color: white;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #fff 0%, rgba(255,255,255,0.7) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
  letter-spacing: -0.02em;
}

.login-subtitle {
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  margin: 0;
  font-weight: 400;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  font-size: 13px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.02em;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  width: 20px;
  height: 20px;
  color: rgba(255, 255, 255, 0.3);
  pointer-events: none;
  transition: color 0.3s;
  z-index: 2;
}

.input-icon svg {
  width: 20px;
  height: 20px;
}

.input-field {
  width: 100%;
  padding: 14px 48px 14px 48px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.95);
  transition: all 0.3s ease;
  outline: none;
}

.input-field::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.input-field:hover {
  border-color: rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.05);
}

.input-field:focus {
  border-color: rgba(102, 126, 234, 0.5);
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-wrapper:focus-within .input-icon {
  color: #667eea;
}

.input-glow {
  position: absolute;
  inset: -1px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  opacity: 0;
  transition: opacity 0.3s;
  z-index: -1;
  filter: blur(8px);
}

.input-wrapper:focus-within .input-glow {
  opacity: 0.15;
}

.password-toggle {
  position: absolute;
  right: 16px;
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.3);
  transition: color 0.2s;
  z-index: 2;
}

.password-toggle:hover {
  color: rgba(255, 255, 255, 0.6);
}

.password-toggle svg {
  width: 20px;
  height: 20px;
}

.btn-login {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  margin-top: 8px;
}

.btn-login::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.2), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.btn-login:hover::before {
  opacity: 1;
}

.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.4);
}

.btn-login:active {
  transform: translateY(0);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.divider {
  display: flex;
  align-items: center;
  gap: 16px;
  color: rgba(255, 255, 255, 0.3);
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
}

.signup-link {
  text-align: center;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
}

.signup-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: all 0.2s;
}

.signup-link a:hover {
  color: #a855f7;
  text-shadow: 0 0 20px rgba(168, 85, 247, 0.5);
}

@media (max-width: 640px) {
  .login-card {
    padding: 32px 24px;
    margin: 16px;
  }

  .login-title {
    font-size: 28px;
  }

  .orb-1 { width: 250px; height: 250px; }
  .orb-2 { width: 200px; height: 200px; }
  .orb-3 { width: 150px; height: 150px; }
}
</style>
