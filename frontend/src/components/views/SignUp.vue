<template>
  <div class="signup-container">
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <div class="signup-card">
      <div class="signup-brand">
        <div class="brand-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="8.5" cy="7" r="4"></circle>
            <line x1="20" y1="8" x2="20" y2="14"></line>
            <line x1="23" y1="11" x2="17" y2="11"></line>
          </svg>
        </div>
      </div>

      <div class="signup-header">
        <h1 class="signup-title">Create account</h1>
        <p class="signup-subtitle">새로운 계정을 만들어 시작하세요</p>
      </div>

      <form class="signup-form" @submit.prevent="signUpSubmit">
        <div class="form-section">
          <h3 class="section-title">기본 정보</h3>

          <div class="input-group">
            <label for="username" class="input-label">아이디</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
              </div>
              <input
                id="username"
                type="text"
                :class="['input-field', { 'error': idChecked && idDuplicate, 'success': idChecked && !idDuplicate }]"
                placeholder="사용할 아이디를 입력하세요"
                v-model="dict_columns.username"
                @input="resetIdCheck"
                @blur="checkIdDuplicate"
                required
              />
              <div v-if="idChecking" class="input-status checking">
                <span class="spinner"></span>
              </div>
              <div v-else-if="idChecked && !idDuplicate" class="input-status success">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
              </div>
              <div v-else-if="idChecked && idDuplicate" class="input-status error">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </div>
            </div>
            <p v-if="idChecked && idDuplicate" class="field-message error">이미 사용 중인 아이디입니다</p>
            <p v-if="idChecked && !idDuplicate" class="field-message success">사용 가능한 아이디입니다</p>
          </div>

          <div class="input-row">
            <div class="input-group">
              <label for="firstName" class="input-label">이름</label>
              <input id="firstName" type="text" class="input-field" placeholder="이름" v-model="dict_columns.firstName" required />
            </div>
            <div class="input-group">
              <label for="lastName" class="input-label">성</label>
              <input id="lastName" type="text" class="input-field" placeholder="성" v-model="dict_columns.lastName" required />
            </div>
          </div>

          <div class="input-group">
            <label for="nickname" class="input-label">닉네임</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <circle cx="12" cy="10" r="3"></circle>
                  <path d="M5.52 19c.64-2.2 1.84-3 3.22-3h6.52c1.38 0 2.58.8 3.22 3"></path>
                </svg>
              </div>
              <input
                id="nickname"
                type="text"
                :class="['input-field', { 'error': nicknameChecked && nicknameDuplicate, 'success': nicknameChecked && !nicknameDuplicate }]"
                placeholder="닉네임을 입력하세요"
                v-model="dict_columns.nickname"
                @input="resetNicknameCheck"
                @blur="checkNicknameDuplicate"
                required
              />
              <div v-if="nicknameChecking" class="input-status checking">
                <span class="spinner"></span>
              </div>
              <div v-else-if="nicknameChecked && !nicknameDuplicate" class="input-status success">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
              </div>
              <div v-else-if="nicknameChecked && nicknameDuplicate" class="input-status error">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </div>
            </div>
            <p v-if="nicknameChecked && nicknameDuplicate" class="field-message error">이미 사용 중인 닉네임입니다</p>
            <p v-if="nicknameChecked && !nicknameDuplicate" class="field-message success">사용 가능한 닉네임입니다</p>
          </div>

          <div class="input-group">
            <label for="password" class="input-label">비밀번호</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </div>
              <input
                id="password"
                :type="showPassword ? 'text' : 'password'"
                class="input-field"
                placeholder="비밀번호 (8자 이상)"
                v-model="dict_columns.password"
                required
              />
              <button type="button" class="password-toggle" @click="showPassword = !showPassword">
                <svg v-if="!showPassword" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
              </button>
            </div>
            <div v-if="dict_columns.password" class="password-strength">
              <div class="strength-bar"><div class="strength-fill" :class="passwordStrength.level" :style="{ width: `${passwordStrength.score * 25}%` }"></div></div>
              <span class="strength-text" :class="passwordStrength.level">{{ passwordStrength.text }}</span>
            </div>
            <div v-if="dict_columns.password" class="password-requirements">
              <div class="requirement" :class="{ met: passwordValidation.hasMinLength }"><span>8자 이상</span></div>
              <div class="requirement" :class="{ met: passwordValidation.hasLetter }"><span>영문자</span></div>
              <div class="requirement" :class="{ met: passwordValidation.hasNumber }"><span>숫자</span></div>
              <div class="requirement" :class="{ met: passwordValidation.hasSpecial }"><span>특수문자</span></div>
            </div>
          </div>

          <div class="input-group">
            <label for="passwordConfirm" class="input-label">비밀번호 확인</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </div>
              <input
                id="passwordConfirm"
                type="password"
                :class="['input-field', { 'error': passwordConfirm && passwordConfirm !== dict_columns.password }]"
                placeholder="비밀번호를 다시 입력하세요"
                v-model="passwordConfirm"
                required
              />
            </div>
            <p v-if="passwordConfirm && passwordConfirm !== dict_columns.password" class="field-message error">비밀번호가 일치하지 않습니다</p>
          </div>
        </div>

        <div class="form-section">
          <h3 class="section-title">연락처 정보</h3>

          <div class="input-group">
            <label for="email" class="input-label">이메일</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                  <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
              </div>
              <input id="email" type="email" class="input-field" placeholder="example@email.com" v-model="dict_columns.email" required />
            </div>
          </div>

          <div class="input-group">
            <label for="phone" class="input-label">전화번호</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72"></path>
                </svg>
              </div>
              <input id="phone" type="tel" class="input-field" placeholder="010-1234-5678" :value="dict_columns.phone" @input="formatPhoneNumber" required />
            </div>
          </div>
        </div>

        <div class="form-section">
          <h3 class="section-title">주소 정보 <span class="optional">(선택)</span></h3>

          <div class="input-group">
            <label for="country" class="input-label">국가</label>
            <input id="country" type="text" class="input-field" placeholder="국가" v-model="dict_columns.country" />
          </div>

          <div class="input-row">
            <div class="input-group">
              <label for="city" class="input-label">도시</label>
              <input id="city" type="text" class="input-field" placeholder="도시" v-model="dict_columns.city" />
            </div>
            <div class="input-group">
              <label for="zip" class="input-label">우편번호</label>
              <input id="zip" type="text" class="input-field" placeholder="우편번호" v-model="dict_columns.zip" />
            </div>
          </div>
        </div>

        <button type="submit" :class="['btn-signup', { loading: loading }]" :disabled="loading">
          <span v-if="!loading">가입하기</span>
          <span v-else class="loading-spinner"></span>
        </button>

        <div class="login-link">
          이미 계정이 있으신가요?
          <a href="#" @click.prevent="goToLogin">로그인</a>
        </div>
      </form>
    </div>

    <!-- Success Overlay -->
    <Transition name="fade">
      <div v-if="showSuccessOverlay" class="success-overlay">
        <div class="success-content">
          <div class="success-check">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
          </div>
          <h2 class="success-title">가입 완료!</h2>
          <p class="success-message">로그인 화면으로 이동합니다...</p>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import apiClient from '@/api/client';
import { useApi } from '@/composables/useApi';
import { API_ENDPOINTS, ROUTES, SUCCESS_MESSAGES } from '@/constants';
import type { CreateSampleRequest, Sample } from '@/types/domain.types';

export default defineComponent({
  name: 'SignUp',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const { loading, execute } = useApi<Sample>();
    const passwordConfirm = ref('');
    const idChecked = ref(false);
    const idDuplicate = ref(false);
    const idChecking = ref(false);
    const nicknameChecked = ref(false);
    const nicknameDuplicate = ref(false);
    const nicknameChecking = ref(false);
    const showPassword = ref(false);
    const showSuccessOverlay = ref(false);
    const registeredUsername = ref('');

    const prefilledUsername = (route.query.username as string) || '';
    const prefilledPassword = (route.query.password as string) || '';

    const formData = reactive<CreateSampleRequest>({
      username: prefilledUsername,
      firstName: '',
      lastName: '',
      password: prefilledPassword,
      nickname: '',
      email: '',
      phone: '',
      country: '',
      city: '',
      state: '',
      street1: '',
      street2: '',
      zip: '',
    });

    const passwordValidation = computed(() => ({
      hasMinLength: formData.password.length >= 8,
      hasLetter: /[a-zA-Z]/.test(formData.password),
      hasNumber: /\d/.test(formData.password),
      hasSpecial: /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(formData.password),
    }));

    const passwordStrength = computed(() => {
      if (!formData.password) return { level: 'none', text: '', score: 0 };
      const v = passwordValidation.value;
      const score = [v.hasMinLength, v.hasLetter, v.hasNumber, v.hasSpecial].filter(Boolean).length;
      if (score < 2) return { level: 'weak', text: '취약', score };
      if (score < 4) return { level: 'medium', text: '보통', score };
      return { level: 'strong', text: '강력', score };
    });

    const checkIdDuplicate = async () => {
      if (!formData.username || formData.username.trim().length < 3) { idChecked.value = false; return; }
      idChecking.value = true;
      try {
        const response = await apiClient.get(API_ENDPOINTS.USER_CHECK_DUPLICATE(formData.username));
        idDuplicate.value = response.data.data.exists;
        idChecked.value = true;
      } catch { idChecked.value = false; }
      finally { idChecking.value = false; }
    };

    const resetIdCheck = () => { idChecked.value = false; idDuplicate.value = false; };

    const checkNicknameDuplicate = async () => {
      if (!formData.nickname || formData.nickname.trim().length < 2) { nicknameChecked.value = false; return; }
      nicknameChecking.value = true;
      try {
        const response = await apiClient.get(API_ENDPOINTS.USER_CHECK_NICKNAME_DUPLICATE(formData.nickname));
        nicknameDuplicate.value = response.data.data.exists;
        nicknameChecked.value = true;
      } catch { nicknameChecked.value = false; }
      finally { nicknameChecking.value = false; }
    };

    const resetNicknameCheck = () => { nicknameChecked.value = false; nicknameDuplicate.value = false; };

    const formatPhoneNumber = (event: Event) => {
      const input = event.target as HTMLInputElement;
      let value = input.value.replace(/\D/g, '');
      if (value.length > 11) value = value.slice(0, 11);
      if (value.length <= 3) {
        formData.phone = value;
      } else if (value.length <= 7) {
        formData.phone = `${value.slice(0, 3)}-${value.slice(3)}`;
      } else {
        formData.phone = `${value.slice(0, 3)}-${value.slice(3, 7)}-${value.slice(7)}`;
      }
    };

    const signUpSubmit = async () => {
      if (!idChecked.value) { alert('아이디 중복 확인이 필요합니다.'); return; }
      if (idDuplicate.value) { alert('이미 사용 중인 아이디입니다.'); return; }
      if (!nicknameChecked.value) { alert('닉네임 중복 확인이 필요합니다.'); return; }
      if (nicknameDuplicate.value) { alert('이미 사용 중인 닉네임입니다.'); return; }
      if (formData.password !== passwordConfirm.value) { alert('비밀번호가 일치하지 않습니다.'); return; }
      if (passwordStrength.value.score < 4) { alert('비밀번호가 안전하지 않습니다.'); return; }

      await execute(() => apiClient.post(API_ENDPOINTS.USERS, formData), {
        onSuccess: () => {
          registeredUsername.value = formData.username;
          showSuccessOverlay.value = true;
          setTimeout(() => {
            router.push({ path: ROUTES.LOGIN, query: { username: registeredUsername.value } });
          }, 1500);
        },
      });
    };

    const goToLogin = () => router.push(ROUTES.LOGIN);

    onMounted(() => { if (prefilledUsername && prefilledUsername.trim().length >= 3) checkIdDuplicate(); });

    return {
      dict_columns: formData, passwordConfirm, idChecked, idDuplicate, idChecking, nicknameChecked, nicknameDuplicate, nicknameChecking,
      showPassword, passwordValidation, passwordStrength, loading, showSuccessOverlay,
      checkIdDuplicate, resetIdCheck, checkNicknameDuplicate, resetNicknameCheck, formatPhoneNumber, signUpSubmit, goToLogin,
    };
  },
});
</script>

<style scoped>
.signup-container {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: float 20s ease-in-out infinite;
}

.orb-1 { width: 500px; height: 500px; background: linear-gradient(135deg, #667eea, #764ba2); top: -150px; right: -150px; }
.orb-2 { width: 400px; height: 400px; background: linear-gradient(135deg, #11998e, #38ef7d); bottom: -100px; left: -100px; animation-delay: -7s; }
.orb-3 { width: 300px; height: 300px; background: linear-gradient(135deg, #f093fb, #f5576c); top: 40%; left: 30%; animation-delay: -14s; }

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-30px, 30px) scale(0.95); }
}

.signup-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 40px;
  width: 100%;
  max-width: 520px;
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.signup-brand { display: flex; justify-content: center; margin-bottom: 20px; }

.brand-icon {
  width: 52px; height: 52px;
  background: linear-gradient(135deg, #11998e, #38ef7d);
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 32px rgba(17, 153, 142, 0.3);
}

.brand-icon svg { width: 26px; height: 26px; color: white; }

.signup-header { text-align: center; margin-bottom: 28px; }

.signup-title {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 28px; font-weight: 700;
  background: linear-gradient(135deg, #fff, rgba(255,255,255,0.7));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text; margin: 0 0 8px 0;
}

.signup-subtitle { color: rgba(255, 255, 255, 0.5); font-size: 14px; margin: 0; }

.signup-form { display: flex; flex-direction: column; gap: 24px; }

.form-section { display: flex; flex-direction: column; gap: 16px; }

.section-title {
  font-size: 14px; font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin: 0;
}

.section-title .optional { color: rgba(255, 255, 255, 0.4); font-weight: 400; }

.input-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

.input-group { display: flex; flex-direction: column; gap: 6px; }

.input-label { font-size: 13px; font-weight: 500; color: rgba(255, 255, 255, 0.7); }

.input-wrapper { position: relative; display: flex; align-items: center; }

.input-icon {
  position: absolute; left: 14px; width: 18px; height: 18px;
  color: rgba(255, 255, 255, 0.3);
  pointer-events: none; transition: color 0.3s; z-index: 2;
}

.input-icon svg { width: 18px; height: 18px; }

.input-field {
  width: 100%; padding: 12px 14px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px; font-size: 14px;
  color: rgba(255, 255, 255, 0.95);
  transition: all 0.3s ease; outline: none;
}

.input-wrapper .input-field { padding-left: 42px; padding-right: 42px; }

.input-field::placeholder { color: rgba(255, 255, 255, 0.25); }
.input-field:hover { border-color: rgba(255, 255, 255, 0.15); background: rgba(255, 255, 255, 0.05); }
.input-field:focus { border-color: rgba(102, 126, 234, 0.5); box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1); }
.input-field.error { border-color: rgba(239, 68, 68, 0.5); }
.input-field.success { border-color: rgba(16, 185, 129, 0.5); }

.input-wrapper:focus-within .input-icon { color: #667eea; }

.input-status {
  position: absolute;
  right: 14px;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

.input-status svg { width: 18px; height: 18px; }

.input-status.success svg { color: #10b981; }
.input-status.error svg { color: #ef4444; }

.input-status.checking .spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.input-with-button {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.input-with-button .input-wrapper { flex: 1; }

.flex-grow { flex: 1; }

.btn-check {
  flex-shrink: 0;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.btn-check:hover:not(:disabled) {
  background: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.3);
  color: #667eea;
}

.btn-check:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-check.success {
  background: rgba(16, 185, 129, 0.1);
  border-color: rgba(16, 185, 129, 0.3);
  color: #10b981;
}

.btn-check.error {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
  color: #ef4444;
}

.password-toggle {
  position: absolute; right: 14px; background: none; border: none;
  padding: 4px; cursor: pointer; color: rgba(255, 255, 255, 0.3);
  transition: color 0.2s; z-index: 2;
}

.password-toggle:hover { color: rgba(255, 255, 255, 0.6); }
.password-toggle svg { width: 18px; height: 18px; }

.field-message { font-size: 12px; margin: 0; }
.field-message.error { color: #ef4444; }
.field-message.success { color: #10b981; }

.password-strength { display: flex; align-items: center; gap: 12px; margin-top: 8px; }

.strength-bar {
  flex: 1; height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px; overflow: hidden;
}

.strength-fill { height: 100%; transition: all 0.3s; border-radius: 2px; }
.strength-fill.weak { background: linear-gradient(90deg, #ef4444, #f87171); }
.strength-fill.medium { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.strength-fill.strong { background: linear-gradient(90deg, #10b981, #34d399); }

.strength-text { font-size: 12px; font-weight: 600; min-width: 36px; }
.strength-text.weak { color: #ef4444; }
.strength-text.medium { color: #f59e0b; }
.strength-text.strong { color: #10b981; }

.password-requirements { display: flex; gap: 8px; margin-top: 8px; flex-wrap: wrap; }

.requirement {
  font-size: 11px; padding: 4px 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px; color: rgba(255, 255, 255, 0.4);
  transition: all 0.2s;
}

.requirement.met { color: #10b981; border-color: rgba(16, 185, 129, 0.3); background: rgba(16, 185, 129, 0.1); }

.btn-signup {
  width: 100%; padding: 14px;
  background: linear-gradient(135deg, #11998e, #38ef7d);
  color: white; border: none; border-radius: 12px;
  font-size: 15px; font-weight: 600; cursor: pointer;
  transition: all 0.3s; margin-top: 8px;
}

.btn-signup:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(17, 153, 142, 0.4); }
.btn-signup:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

.loading-spinner {
  display: inline-block; width: 20px; height: 20px;
  border: 2px solid rgba(255,255,255,0.3); border-top-color: white;
  border-radius: 50%; animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.login-link { text-align: center; font-size: 13px; color: rgba(255, 255, 255, 0.5); }
.login-link a { color: #667eea; text-decoration: none; font-weight: 600; margin-left: 4px; transition: all 0.2s; }
.login-link a:hover { color: #a855f7; text-shadow: 0 0 20px rgba(168, 85, 247, 0.5); }

@media (max-width: 640px) {
  .signup-card { padding: 28px 20px; margin: 16px; }
  .signup-title { font-size: 24px; }
  .input-row { grid-template-columns: 1fr; }
  .orb-1 { width: 300px; height: 300px; }
  .orb-2 { width: 250px; height: 250px; }
  .orb-3 { width: 180px; height: 180px; }
}

/* Success Overlay Styles */
.success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 15, 26, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: successPop 0.4s ease-out;
}

@keyframes successPop {
  0% { opacity: 0; transform: scale(0.8); }
  50% { transform: scale(1.05); }
  100% { opacity: 1; transform: scale(1); }
}

.success-check {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 40px rgba(34, 197, 94, 0.4);
  margin-bottom: 24px;
}

.success-check svg {
  width: 40px;
  height: 40px;
  color: white;
  stroke-dasharray: 50;
  stroke-dashoffset: 50;
  animation: checkDraw 0.5s ease-out 0.2s forwards;
}

@keyframes checkDraw {
  0% { stroke-dashoffset: 50; }
  100% { stroke-dashoffset: 0; }
}

.success-overlay .success-title {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 8px 0;
}

.success-overlay .success-message {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

/* Input hover effect */
.input-group {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.input-group:hover {
  transform: translateY(-2px);
}

/* Button shimmer effect when loading */
.btn-signup.loading {
  background: linear-gradient(90deg, #11998e 0%, #38ef7d 50%, #11998e 100%);
  background-size: 200% auto;
  animation: shimmer 1.5s linear infinite;
}

@keyframes shimmer {
  0% { background-position: -200% center; }
  100% { background-position: 200% center; }
}

/* Fade Transition */
.fade-enter-active { transition: opacity 0.3s ease-out; }
.fade-leave-active { transition: opacity 0.3s ease-in; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
