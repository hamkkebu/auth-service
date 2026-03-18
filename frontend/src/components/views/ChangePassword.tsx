import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/composables/useAuth';
import apiClient from '@/api/client';
import { API_ENDPOINTS } from '@/constants';
import type { PasswordChangeRequest } from '@/types/domain.types';
import styles from './ChangePassword.module.css';

export default function ChangePassword() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [form, setForm] = useState<PasswordChangeRequest>({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [saving, setSaving] = useState(false);
  const [showCurrentPw, setShowCurrentPw] = useState(false);
  const [showNewPw, setShowNewPw] = useState(false);
  const [showConfirmPw, setShowConfirmPw] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      alert('로그인이 필요합니다.');
      navigate('../profile');
    }
  }, [isAuthenticated, navigate]);

  const handleChange = (field: keyof PasswordChangeRequest, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const validateForm = (): string | null => {
    if (!form.currentPassword) return '현재 비밀번호를 입력해주세요.';
    if (!form.newPassword) return '새 비밀번호를 입력해주세요.';
    if (!form.confirmPassword) return '새 비밀번호 확인을 입력해주세요.';
    if (form.newPassword.length < 8) return '새 비밀번호는 8자 이상이어야 합니다.';
    if (form.newPassword !== form.confirmPassword) return '새 비밀번호가 일치하지 않습니다.';
    if (form.currentPassword === form.newPassword) return '현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.';

    // 영문자, 숫자, 특수문자 포함 검증
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (!passwordRegex.test(form.newPassword)) {
      return '비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다.';
    }

    return null;
  };

  const handleSubmit = async () => {
    const error = validateForm();
    if (error) {
      alert(error);
      return;
    }

    setSaving(true);
    try {
      await apiClient.put(API_ENDPOINTS.USER_ME_PASSWORD, form);
      alert('비밀번호가 변경되었습니다.');
      navigate('../profile');
    } catch (error: any) {
      // apiClient 인터셉터에서 이미 alert 처리함
      console.error('비밀번호 변경 실패:', error);
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate('../profile');
  };

  const EyeIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
      <circle cx="12" cy="12" r="3"></circle>
    </svg>
  );

  const EyeOffIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
      <line x1="1" y1="1" x2="23" y2="23"></line>
    </svg>
  );

  return (
    <div className={styles.changePasswordContainer}>
      <div className={styles.changePasswordCard}>
        <div className={styles.lockIcon}>
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
          </svg>
        </div>

        <div className={styles.changePasswordHeader}>
          <h1 className={styles.changePasswordTitle}>비밀번호 변경</h1>
          <p className={styles.changePasswordSubtitle}>안전한 비밀번호로 계정을 보호하세요</p>
        </div>

        <div className={styles.passwordRules}>
          <h3>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="16" x2="12" y2="12"></line>
              <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
            비밀번호 규칙
          </h3>
          <ul>
            <li>8자 이상</li>
            <li>영문자(대소문자) 포함</li>
            <li>숫자 포함</li>
            <li>특수문자(@$!%*#?&) 포함</li>
          </ul>
        </div>

        <div className={styles.changePasswordForm}>
          <div className={styles.inputGroup}>
            <label className={styles.inputLabel}>현재 비밀번호</label>
            <div className={styles.inputWrapper}>
              <div className={styles.inputIcon}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </div>
              <input
                type={showCurrentPw ? 'text' : 'password'}
                className={styles.inputField}
                value={form.currentPassword}
                onChange={(e) => handleChange('currentPassword', e.target.value)}
                placeholder="현재 비밀번호를 입력하세요"
              />
              <button
                type="button"
                className={styles.togglePw}
                onClick={() => setShowCurrentPw(!showCurrentPw)}
              >
                {showCurrentPw ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.inputLabel}>새 비밀번호</label>
            <div className={styles.inputWrapper}>
              <div className={styles.inputIcon}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"></path>
                </svg>
              </div>
              <input
                type={showNewPw ? 'text' : 'password'}
                className={styles.inputField}
                value={form.newPassword}
                onChange={(e) => handleChange('newPassword', e.target.value)}
                placeholder="새 비밀번호를 입력하세요"
              />
              <button
                type="button"
                className={styles.togglePw}
                onClick={() => setShowNewPw(!showNewPw)}
              >
                {showNewPw ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.inputLabel}>새 비밀번호 확인</label>
            <div className={styles.inputWrapper}>
              <div className={styles.inputIcon}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                  <polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
              </div>
              <input
                type={showConfirmPw ? 'text' : 'password'}
                className={styles.inputField}
                value={form.confirmPassword}
                onChange={(e) => handleChange('confirmPassword', e.target.value)}
                placeholder="새 비밀번호를 다시 입력하세요"
              />
              <button
                type="button"
                className={styles.togglePw}
                onClick={() => setShowConfirmPw(!showConfirmPw)}
              >
                {showConfirmPw ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            {form.confirmPassword && form.newPassword !== form.confirmPassword && (
              <span className={styles.errorText}>비밀번호가 일치하지 않습니다</span>
            )}
            {form.confirmPassword && form.newPassword === form.confirmPassword && (
              <span className={styles.successText}>비밀번호가 일치합니다</span>
            )}
          </div>

          <div className={styles.buttonGroup}>
            <button onClick={handleCancel} className={styles.btnCancel} disabled={saving}>
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="19" y1="12" x2="5" y2="12"></line>
                <polyline points="12 19 5 12 12 5"></polyline>
              </svg>
              <span>취소</span>
            </button>
            <button onClick={handleSubmit} className={styles.btnSubmit} disabled={saving}>
              {saving ? (
                <div className={styles.loadingSpinner}></div>
              ) : (
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                  <polyline points="17 21 17 13 7 13 7 21"></polyline>
                  <polyline points="7 3 7 8 15 8"></polyline>
                </svg>
              )}
              <span>{saving ? '변경 중...' : '비밀번호 변경'}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
