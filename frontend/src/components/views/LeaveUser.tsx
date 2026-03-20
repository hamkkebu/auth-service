import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/composables/useAuth';
import { useApi } from '@/composables/useApi';
import apiClient from '@/api/client';
import { API_ENDPOINTS, ROUTES } from '@/constants';
import styles from './LeaveUser.module.css';

export default function LeaveUser() {
  const navigate = useNavigate();
  const { currentUser, logout, isAuthenticated } = useAuth();
  const { loading, execute } = useApi();
  const [password, setPassword] = useState('');
  const [confirmed, setConfirmed] = useState(false);

  // App.tsx에서 이미 인증 가드를 처리하므로 별도 체크 불필요

  const confirmAndLeave = () => {
    if (!confirmed) {
      alert('탈퇴 동의에 체크해주세요.');
      return;
    }

    if (!password) {
      alert('비밀번호를 입력해주세요.');
      return;
    }

    if (!currentUser) {
      alert('로그인 정보를 찾을 수 없습니다.');
      navigate('../profile');
      return;
    }

    if (
      confirm(
        `정말로 "${currentUser.username}" 계정을 탈퇴하시겠습니까?\n이 작업은 되돌릴 수 없습니다.`
      )
    ) {
      leaveSubmit();
    }
  };

  const leaveSubmit = async () => {
    if (!currentUser) {
      return;
    }

    await execute(
      () =>
        apiClient.delete(API_ENDPOINTS.USER_BY_USERNAME(currentUser.username), {
          data: {
            password,
          },
          headers: {
            'Refresh-Token': localStorage.getItem('refreshToken') || '',
          },
        }),
      {
        onSuccess: async () => {
          alert('회원 탈퇴가 완료되었습니다.');
          await logout();
        },
      }
    );
  };

  const goBack = () => {
    navigate('../profile');
  };

  return (
    <div className={styles.leaveContainer}>
      <div className={styles.leaveCard}>
        <div className={styles.warningIcon}>
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
        </div>

        <div className={styles.leaveHeader}>
          <h1 className={styles.leaveTitle}>회원탈퇴</h1>
          <p className={styles.leaveSubtitle}>정말로 탈퇴하시겠습니까?</p>
        </div>

        <div className={styles.warningMessage}>
          <div className={styles.warningContent}>
            <h3>
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
              탈퇴 시 유의사항
            </h3>
            <ul>
              <li>계정의 모든 정보가 영구적으로 삭제됩니다</li>
              <li>삭제된 데이터는 복구할 수 없습니다</li>
              <li>탈퇴 후에도 동일한 아이디 및 닉네임으로 재가입할 수 없습니다</li>
            </ul>
          </div>
        </div>

        <form className={styles.leaveForm} onSubmit={(e) => { e.preventDefault(); confirmAndLeave(); }}>
          <div className={styles.inputGroup}>
            <label htmlFor="password" className={styles.inputLabel}>비밀번호 확인</label>
            <div className={styles.inputWrapper}>
              <div className={styles.inputIcon}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </div>
              <input
                id="password"
                type="password"
                className={styles.inputField}
                placeholder="비밀번호를 입력하세요"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          <div className={styles.confirmationCheckbox}>
            <label className={styles.checkboxLabel} onClick={() => setConfirmed(!confirmed)}>
              <div className={`${styles.customCheckbox} ${confirmed ? styles.checked : ''}`}>
                {confirmed && (
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                )}
              </div>
              <span>위 유의사항을 모두 확인했으며, 탈퇴에 동의합니다</span>
            </label>
          </div>

          <div className={styles.buttonGroup}>
            <button type="button" onClick={goBack} className={styles.btnCancel}>
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="15 18 9 12 15 6"></polyline>
              </svg>
              <span>취소</span>
            </button>
            <button type="submit" className={styles.btnLeave} disabled={!confirmed || !password || loading}>
              {!loading ? (
                <>
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                  </svg>
                  <span>탈퇴하기</span>
                </>
              ) : (
                <span className={styles.loadingSpinner}></span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
