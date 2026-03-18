import React, { useState, useEffect, useMemo } from 'react';
import { useAuth } from '@/composables/useAuth';
import apiClient from '@/api/client';
import { API_ENDPOINTS } from '@/constants';
import type { UserResponse, UserStatsResponse, UserRole } from '@/types/domain.types';
import styles from './AdminDashboard.module.css';

export default function AdminDashboard() {
  const { currentUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<'users' | 'deleted'>('users');

  const [users, setUsers] = useState<UserResponse[]>([]);
  const [deletedUsers, setDeletedUsers] = useState<UserResponse[]>([]);
  const [stats, setStats] = useState<UserStatsResponse | null>(null);

  const [showRoleModal, setShowRoleModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserResponse | null>(null);
  const [newRole, setNewRole] = useState<UserRole>('USER');

  const currentUsers = useMemo(() => {
    return activeTab === 'users' ? users : deletedUsers;
  }, [activeTab, users, deletedUsers]);

  const loadStats = async () => {
    try {
      const response = await apiClient.get(API_ENDPOINTS.ADMIN.STATS);
      setStats(response.data.data);
    } catch (err: any) {
      console.error('Failed to load stats:', err);
    }
  };

  const loadUsers = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await apiClient.get(API_ENDPOINTS.ADMIN.USERS);
      setUsers(response.data.data);
    } catch (err: any) {
      setError(err.response?.data?.error?.message || '사용자 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const loadDeletedUsers = async () => {
    if (deletedUsers.length > 0) return;

    setLoading(true);
    setError('');
    try {
      const response = await apiClient.get(API_ENDPOINTS.ADMIN.DELETED_USERS);
      setDeletedUsers(response.data.data);
    } catch (err: any) {
      setError(err.response?.data?.error?.message || '탈퇴 사용자 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const openRoleModal = (user: UserResponse) => {
    setSelectedUser(user);
    setNewRole(user.role);
    setShowRoleModal(true);
  };

  const closeRoleModal = () => {
    setShowRoleModal(false);
    setSelectedUser(null);
    setNewRole('USER');
  };

  const updateUserRole = async () => {
    if (!selectedUser) return;

    try {
      await apiClient.put(
        API_ENDPOINTS.ADMIN.USER_ROLE(selectedUser.username),
        null,
        {
          params: { role: newRole }
        }
      );

      alert('권한이 변경되었습니다.');
      closeRoleModal();
      await loadUsers();
      await loadStats();
    } catch (err: any) {
      alert(err.response?.data?.error?.message || '권한 변경에 실패했습니다.');
    }
  };

  const toggleUserStatus = async (user: UserResponse) => {
    const action = user.isActive ? '비활성화' : '활성화';
    if (!confirm(`${user.username} 사용자를 ${action}하시겠습니까?`)) return;

    try {
      await apiClient.put(
        API_ENDPOINTS.ADMIN.USER_ACTIVE(user.username),
        null,
        {
          params: { isActive: !user.isActive }
        }
      );

      alert(`사용자가 ${action}되었습니다.`);
      await loadUsers();
      await loadStats();
    } catch (err: any) {
      alert(err.response?.data?.error?.message || `${action}에 실패했습니다.`);
    }
  };

  const getRoleLabel = (role: UserRole): string => {
    const labels: Record<UserRole, string> = {
      USER: '사용자',
      ADMIN: '관리자',
      DEVELOPER: '개발자',
    };
    return labels[role] || role;
  };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  useEffect(() => {
    loadStats();
    loadUsers();
  }, []);

  return (
    <div className={styles.adminContainer}>
      <div className={styles.dashboardContent}>
        <div className={styles.dashboardHeader}>
          <div className={styles.headerIcon}>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <rect x="3" y="3" width="7" height="7"></rect>
              <rect x="14" y="3" width="7" height="7"></rect>
              <rect x="14" y="14" width="7" height="7"></rect>
              <rect x="3" y="14" width="7" height="7"></rect>
            </svg>
          </div>
          <div className={styles.headerText}>
            <h1 className={styles.dashboardTitle}>관리자 대시보드</h1>
            <p className={styles.dashboardSubtitle}>사용자 및 시스템 관리</p>
          </div>
        </div>

        {stats && (
          <div className={styles.statsGrid}>
            <div className={styles.statCard}>
              <div className={`${styles.statIcon} ${styles.users}`}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
              </div>
              <div className={styles.statContent}>
                <div className={styles.statValue}>{stats.totalUsers}</div>
                <div className={styles.statLabel}>전체 사용자</div>
              </div>
            </div>

            <div className={styles.statCard}>
              <div className={`${styles.statIcon} ${styles.active}`}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                  <polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
              </div>
              <div className={styles.statContent}>
                <div className={styles.statValue}>{stats.activeUsers}</div>
                <div className={styles.statLabel}>활성 사용자</div>
              </div>
            </div>

            <div className={styles.statCard}>
              <div className={`${styles.statIcon} ${styles.admin}`}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
                  <path d="M2 17l10 5 10-5"></path>
                  <path d="M2 12l10 5 10-5"></path>
                </svg>
              </div>
              <div className={styles.statContent}>
                <div className={styles.statValue}>{stats.adminUsers + stats.developerUsers}</div>
                <div className={styles.statLabel}>관리자</div>
              </div>
            </div>

            <div className={styles.statCard}>
              <div className={`${styles.statIcon} ${styles.deleted}`}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </div>
              <div className={styles.statContent}>
                <div className={styles.statValue}>{stats.deletedUsers}</div>
                <div className={styles.statLabel}>탈퇴 사용자</div>
              </div>
            </div>
          </div>
        )}

        <div className={styles.tabs}>
          <button
            className={`${styles.tab} ${activeTab === 'users' ? styles.active : ''}`}
            onClick={() => setActiveTab('users')}
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
            </svg>
            활성 사용자
          </button>
          <button
            className={`${styles.tab} ${activeTab === 'deleted' ? styles.active : ''}`}
            onClick={() => {
              setActiveTab('deleted');
              loadDeletedUsers();
            }}
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
            탈퇴 사용자
          </button>
        </div>

        <div className={styles.tableCard}>
          {loading ? (
            <div className={styles.loading}>
              <div className={styles.spinner}></div>
              <p>로딩 중...</p>
            </div>
          ) : error ? (
            <div className={styles.errorMessage}>
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              {error}
            </div>
          ) : (
            <div className={styles.tableWrapper}>
              <table className={styles.userTable}>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>아이디</th>
                    <th>이름</th>
                    <th>이메일</th>
                    <th>권한</th>
                    <th>상태</th>
                    <th>가입일</th>
                    {activeTab === 'users' && <th>작업</th>}
                  </tr>
                </thead>
                <tbody>
                  {currentUsers.map((user) => (
                    <tr key={user.userId}>
                      <td className={styles.tdId}>{user.userId}</td>
                      <td className={styles.tdUsername}>{user.username}</td>
                      <td>{user.firstName} {user.lastName}</td>
                      <td className={styles.tdEmail}>{user.email}</td>
                      <td>
                        <span className={`${styles.roleBadge} ${styles[user.role.toLowerCase()]}`}>
                          {getRoleLabel(user.role)}
                        </span>
                      </td>
                      <td>
                        <span className={`${styles.statusBadge} ${user.isActive ? styles.active : styles.inactive}`}>
                          {user.isActive ? '활성' : '비활성'}
                        </span>
                      </td>
                      <td className={styles.tdDate}>{formatDate(user.createdAt)}</td>
                      {activeTab === 'users' && (
                        <td className={styles.tdActions}>
                          <button
                            className={`${styles.btnAction} ${styles.role}`}
                            onClick={() => openRoleModal(user)}
                            disabled={user.username === currentUser?.username}
                            title="권한 변경"
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
                              <path d="M2 17l10 5 10-5"></path>
                              <path d="M2 12l10 5 10-5"></path>
                            </svg>
                          </button>
                          <button
                            className={`${styles.btnAction} ${user.isActive ? styles.danger : styles.success}`}
                            onClick={() => toggleUserStatus(user)}
                            disabled={user.username === currentUser?.username}
                            title={user.isActive ? '비활성화' : '활성화'}
                          >
                            {user.isActive ? (
                              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"></line>
                              </svg>
                            ) : (
                              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                <polyline points="22 4 12 14.01 9 11.01"></polyline>
                              </svg>
                            )}
                          </button>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {showRoleModal && (
        <div className={styles.modalOverlay} onClick={closeRoleModal}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2>권한 변경</h2>
              <button className={styles.modalClose} onClick={closeRoleModal}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>
            <div className={styles.modalBody}>
              <p className={styles.modalDesc}>
                <strong>{selectedUser?.username}</strong> 사용자의 권한을 변경합니다.
              </p>
              <div className={styles.roleOptions}>
                <label className={`${styles.roleOption} ${newRole === 'USER' ? styles.selected : ''}`}>
                  <input type="radio" value="USER" checked={newRole === 'USER'} onChange={(e) => setNewRole(e.target.value as UserRole)} />
                  <div className={styles.roleOptionContent}>
                    <div className={`${styles.roleOptionIcon} ${styles.user}`}>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                      </svg>
                    </div>
                    <div>
                      <div className={styles.roleOptionTitle}>일반 사용자</div>
                      <div className={styles.roleOptionDesc}>기본 사용자 권한</div>
                    </div>
                  </div>
                </label>
                <label className={`${styles.roleOption} ${newRole === 'DEVELOPER' ? styles.selected : ''}`}>
                  <input type="radio" value="DEVELOPER" checked={newRole === 'DEVELOPER'} onChange={(e) => setNewRole(e.target.value as UserRole)} />
                  <div className={styles.roleOptionContent}>
                    <div className={`${styles.roleOptionIcon} ${styles.developer}`}>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <polyline points="16 18 22 12 16 6"></polyline>
                        <polyline points="8 6 2 12 8 18"></polyline>
                      </svg>
                    </div>
                    <div>
                      <div className={styles.roleOptionTitle}>개발자</div>
                      <div className={styles.roleOptionDesc}>개발자 전용 기능 접근</div>
                    </div>
                  </div>
                </label>
                <label className={`${styles.roleOption} ${newRole === 'ADMIN' ? styles.selected : ''}`}>
                  <input type="radio" value="ADMIN" checked={newRole === 'ADMIN'} onChange={(e) => setNewRole(e.target.value as UserRole)} />
                  <div className={styles.roleOptionContent}>
                    <div className={`${styles.roleOptionIcon} ${styles.admin}`}>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                      </svg>
                    </div>
                    <div>
                      <div className={styles.roleOptionTitle}>관리자</div>
                      <div className={styles.roleOptionDesc}>전체 시스템 관리 권한</div>
                    </div>
                  </div>
                </label>
              </div>
            </div>
            <div className={styles.modalFooter}>
              <button className={styles.btnSecondary} onClick={closeRoleModal}>취소</button>
              <button className={styles.btnPrimary} onClick={updateUserRole}>변경</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
