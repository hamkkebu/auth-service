import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/composables/useAuth';
import { useKeycloak } from '@/composables/useKeycloak';
import { useApi } from '@/composables/useApi';
import apiClient from '@/api/client';
import { API_ENDPOINTS } from '@/constants';
import type { Sample, ProfileUpdateRequest } from '@/types/domain.types';
import styles from './UserInfo.module.css';

export default function UserInfo() {
  const navigate = useNavigate();
  const keycloak = useKeycloak();
  const { currentUser, logout, login } = useAuth();
  const { loading, execute } = useApi<Sample>();
  const [result, setResult] = useState<Sample[]>([]);
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState<ProfileUpdateRequest>({
    email: '',
    firstName: '',
    lastName: '',
    nickname: '',
    phone: '',
    country: '',
    city: '',
    state: '',
    street1: '',
    street2: '',
    zip: '',
  });
  const [saving, setSaving] = useState(false);
  const loginAttempted = React.useRef(false);

  const getUserInfo = async () => {
    if (!keycloak.isInitialized) {
      return;
    }

    if (!keycloak.isAuthenticated) {
      if (!loginAttempted.current) {
        loginAttempted.current = true;
        login();
      }
      return;
    }

    loginAttempted.current = false;

    if (!currentUser?.username) {
      return;
    }

    try {
      const data = await execute(() =>
        apiClient.get(API_ENDPOINTS.USER_BY_USERNAME(currentUser.username))
      );

      if (data) {
        setResult([data]);
      }
    } catch (error: any) {
      const errorCode = error?.response?.data?.error?.code;
      const errorMessage = error?.response?.data?.error?.message || '';

      if (errorCode === 'USER-101' || errorMessage.includes('사용자를 찾을 수 없습니다')) {
        alert('사용자를 찾을 수 없습니다. 로그인 화면으로 되돌아갑니다.');
        logout();
      }
    }
  };

  const handleEditClick = () => {
    if (result.length > 0) {
      const user = result[0];
      setEditForm({
        email: user.email || '',
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        nickname: user.nickname || '',
        phone: user.phone || '',
        country: user.country || '',
        city: user.city || '',
        state: user.state || '',
        street1: user.street1 || '',
        street2: user.street2 || '',
        zip: user.zip || '',
      });
      setIsEditing(true);
    }
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
  };

  const handleFormChange = (field: keyof ProfileUpdateRequest, value: string) => {
    setEditForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSaveProfile = async () => {
    if (!editForm.email) {
      alert('이메일은 필수입니다.');
      return;
    }

    setSaving(true);
    try {
      const response = await apiClient.put(API_ENDPOINTS.USER_ME, editForm);
      const updatedData = response.data?.data;

      if (updatedData) {
        const updatedUser: Sample = {
          id: updatedData.userId,
          username: updatedData.username,
          firstName: updatedData.firstName,
          lastName: updatedData.lastName,
          nickname: updatedData.nickname,
          email: updatedData.email,
          phone: updatedData.phoneNumber,
          country: updatedData.country,
          city: updatedData.city,
          state: updatedData.state,
          street1: updatedData.streetAddress,
          street2: updatedData.streetAddress2,
          zip: updatedData.postalCode,
        };
        setResult([updatedUser]);
      }

      setIsEditing(false);
      alert('프로필이 수정되었습니다.');
    } catch (error: any) {
      console.error('프로필 수정 실패:', error);
    } finally {
      setSaving(false);
    }
  };

  const goToLogout = async () => {
    console.log('[UserInfo] goToLogout called');
    try {
      await logout();
      console.log('[UserInfo] logout completed');
    } catch (error) {
      console.error('[UserInfo] logout error:', error);
    }
  };

  const goToLeave = () => {
    navigate('../leave');
  };

  useEffect(() => {
    if (keycloak.isInitialized) {
      getUserInfo();
    }
  }, [keycloak.isInitialized, keycloak.isAuthenticated, currentUser]);

  return (
    <div className={styles.userinfoContainer}>
      <div className={styles.userinfoCard}>
        <div className={styles.userinfoBrand}>
          <div className={styles.brandIcon}>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
          </div>
        </div>

        <div className={styles.userinfoHeader}>
          <h1 className={styles.userinfoTitle}>My Profile</h1>
          <p className={styles.userinfoSubtitle}>
            {isEditing ? '프로필 정보를 수정하세요' : '내 정보를 확인하세요'}
          </p>
        </div>

        <div className={styles.actionBar}>
          <div className={styles.userCount}>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span>회원 정보</span>
          </div>
          {!isEditing && result.length > 0 && (
            <button onClick={handleEditClick} className={styles.btnEdit}>
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
              </svg>
              <span>수정</span>
            </button>
          )}
        </div>

        {isEditing ? (
          <div className={styles.editFormContainer}>
            <div className={styles.editFormGrid}>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>이름 (First Name)</label>
                <input
                  type="text"
                  className={styles.formInput}
                  value={editForm.firstName}
                  onChange={(e) => handleFormChange('firstName', e.target.value)}
                  placeholder="이름"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>성 (Last Name)</label>
                <input
                  type="text"
                  className={styles.formInput}
                  value={editForm.lastName}
                  onChange={(e) => handleFormChange('lastName', e.target.value)}
                  placeholder="성"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>닉네임</label>
                <input
                  type="text"
                  className={styles.formInput}
                  value={editForm.nickname}
                  onChange={(e) => handleFormChange('nickname', e.target.value)}
                  placeholder="닉네임"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>
                  이메일 <span className={styles.required}>*</span>
                </label>
                <input
                  type="email"
                  className={styles.formInput}
                  value={editForm.email}
                  onChange={(e) => handleFormChange('email', e.target.value)}
                  placeholder="이메일"
                  required
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>전화번호</label>
                <input
                  type="tel"
                  className={styles.formInput}
                  value={editForm.phone}
                  onChange={(e) => handleFormChange('phone', e.target.value)}
                  placeholder="전화번호"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>국가</label>
                <input
                  type="text"
                  className={styles.formInput}
                  value={editForm.country}
                  onChange={(e) => handleFormChange('country', e.target.value)}
                  placeholder="국가"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>시/도</label>
                <input
                  type="text"
                  className={styles.formInput}
                  value={editForm.state}
                  onChange={(e) => handleFormChange('state', e.target.value)}
                  placeholder="시/도"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>시/군/구</label>
                <input
                  type="text"
                  className={styles.formInput}
                  value={editForm.city}
                  onChange={(e) => handleFormChange('city', e.target.value)}
                  placeholder="시/군/구"
                />
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label className={styles.formLabel}>주소</label>
                <input
                  type="text"
                  className={styles.formInput}
                  value={editForm.street1}
                  onChange={(e) => handleFormChange('street1', e.target.value)}
                  placeholder="주소"
                />
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label className={styles.formLabel}>상세 주소</label>
                <input
                  type="text"
                  className={styles.formInput}
                  value={editForm.street2}
                  onChange={(e) => handleFormChange('street2', e.target.value)}
                  placeholder="상세 주소"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>우편번호</label>
                <input
                  type="text"
                  className={styles.formInput}
                  value={editForm.zip}
                  onChange={(e) => handleFormChange('zip', e.target.value)}
                  placeholder="우편번호"
                />
              </div>
            </div>
            <div className={styles.editActions}>
              <button
                onClick={handleCancelEdit}
                className={styles.btnCancel}
                disabled={saving}
              >
                취소
              </button>
              <button
                onClick={handleSaveProfile}
                className={styles.btnSave}
                disabled={saving}
              >
                {saving ? '저장 중...' : '저장'}
              </button>
            </div>
          </div>
        ) : result.length > 0 ? (
          <div className={styles.tableContainer}>
            {result.map((user, index) => (
              <div key={index} className={styles.userCard}>
                <div className={styles.userCardHeader}>
                  <div className={styles.userAvatar}>
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                  </div>
                  <div className={styles.userBasicInfo}>
                    <h3>{user.firstName} {user.lastName}</h3>
                    <p>@{user.nickname}</p>
                  </div>
                  <div className={styles.userIdBadge}>
                    ID: {user.username}
                  </div>
                </div>

                <div className={styles.userCardBody}>
                  <div className={styles.infoGrid}>
                    <div className={styles.infoItem}>
                      <svg className={styles.infoIcon} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                        <polyline points="22,6 12,13 2,6"></polyline>
                      </svg>
                      <div>
                        <span className={styles.infoLabel}>이메일</span>
                        <span className={styles.infoValue}>{user.email || 'N/A'}</span>
                      </div>
                    </div>

                    <div className={styles.infoItem}>
                      <svg className={styles.infoIcon} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                      </svg>
                      <div>
                        <span className={styles.infoLabel}>전화번호</span>
                        <span className={styles.infoValue}>{user.phone || 'N/A'}</span>
                      </div>
                    </div>

                    <div className={`${styles.infoItem} ${styles.fullWidth}`}>
                      <svg className={styles.infoIcon} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                        <circle cx="12" cy="10" r="3"></circle>
                      </svg>
                      <div>
                        <span className={styles.infoLabel}>주소</span>
                        <span className={styles.infoValue}>
                          {[user.street1, user.street2, user.city, user.state, user.country, user.zip].filter(Boolean).join(', ') || 'N/A'}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className={styles.emptyState}>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
            <h3>등록된 회원이 없습니다</h3>
            <p>새로운 회원을 등록해주세요.</p>
          </div>
        )}

        <div className={styles.navigationButtons}>
          <button onClick={() => navigate('../password')} className={styles.btnPassword}>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
            <span>비밀번호 변경</span>
          </button>
          <button onClick={goToLogout} className={styles.btnSecondary}>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
            <span>로그아웃</span>
          </button>
          <button onClick={goToLeave} className={styles.btnDanger}>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
            <span>회원탈퇴</span>
          </button>
        </div>
      </div>
    </div>
  );
}
