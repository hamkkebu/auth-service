import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import axios from 'axios';
import LogIn from '@/components/views/LogIn.vue';
import SignUp from '@/components/views/SignUp.vue';
import UserInfo from '@/components/views/UserInfo.vue';
import LeaveUser from '@/components/views/LeaveUser.vue';
import AdminDashboard from '@/components/views/AdminDashboard.vue';
import { ROUTES } from '@/constants';
import type { UserRole, TokenResponse } from '@/types/domain.types';
import type { ApiResponse } from '@/types/api.types';

const routes: RouteRecordRaw[] = [
  {
    path: ROUTES.HOME,
    name: 'Home',
    // redirect는 라우터 가드에서 처리
    redirect: () => {
      const currentUserJson = localStorage.getItem('currentUser');
      const authToken = localStorage.getItem('authToken');

      if (currentUserJson && authToken) {
        try {
          const currentUser = JSON.parse(currentUserJson);
          const userRole = currentUser.role;

          // 로그인되어 있으면 권한에 따라 리다이렉트
          if (userRole === 'ADMIN' || userRole === 'DEVELOPER') {
            return ROUTES.ADMIN_DASHBOARD;
          } else {
            return ROUTES.USER_INFO;
          }
        } catch (e) {
          return ROUTES.LOGIN;
        }
      }

      // 로그인 안되어 있으면 로그인 페이지로
      return ROUTES.LOGIN;
    },
  },
  {
    path: ROUTES.LOGIN,
    name: 'LogIn',
    component: LogIn,
    meta: { requiresAuth: false },
  },
  {
    path: ROUTES.SIGNUP,
    name: 'SignUp',
    component: SignUp,
    meta: { requiresAuth: false },
  },
  {
    path: ROUTES.USER_INFO,
    name: 'UserInfo',
    component: UserInfo,
    meta: { requiresAuth: true },
  },
  {
    path: ROUTES.LEAVE_USER,
    name: 'LeaveUser',
    component: LeaveUser,
    meta: { requiresAuth: true },
  },
  {
    path: ROUTES.ADMIN_DASHBOARD,
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: {
      requiresAuth: true,
      requiredRoles: ['ADMIN', 'DEVELOPER'] as UserRole[]
    },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

/**
 * 세션 유효성 검증을 위해 refresh token으로 토큰 갱신 시도
 * Redis에 refresh token이 없으면 실패하여 로그인 필요
 */
async function validateSession(): Promise<boolean> {
  const refreshToken = localStorage.getItem('refreshToken');

  if (!refreshToken) {
    return false;
  }

  try {
    const response = await axios.post<ApiResponse<TokenResponse>>(
      `${process.env.VUE_APP_baseApiURL || ''}/api/v1/auth/refresh`,
      null,
      {
        headers: {
          'Refresh-Token': refreshToken,
        },
      }
    );

    const tokenData = response.data.data;

    if (tokenData) {
      // 새로운 토큰 저장
      localStorage.setItem('authToken', tokenData.accessToken);
      localStorage.setItem('refreshToken', tokenData.refreshToken);
      return true;
    }

    return false;
  } catch (error) {
    console.error('[Session Validation Failed]', error);
    // 세션 무효화
    localStorage.removeItem('authToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('currentUser');
    return false;
  }
}

/**
 * 라우터 가드: 인증 및 권한 확인
 */
router.beforeEach(async (to, from, next) => {
  const currentUserJson = localStorage.getItem('currentUser');
  const authToken = localStorage.getItem('authToken');
  const refreshToken = localStorage.getItem('refreshToken');

  let currentUser: { username: string; role?: UserRole } | null = null;

  if (currentUserJson) {
    try {
      currentUser = JSON.parse(currentUserJson);
    } catch (e) {
      console.error('Failed to parse currentUser:', e);
    }
  }

  const hasLocalAuth = !!(currentUser && authToken && refreshToken);
  const requiresAuth = to.meta.requiresAuth;
  const requiredRoles = to.meta.requiredRoles as UserRole[] | undefined;

  // 인증이 필요한 페이지인 경우 세션 유효성 검증
  if (requiresAuth) {
    if (!hasLocalAuth) {
      // 로컬에 인증 정보가 없으면 바로 로그인 페이지로
      next(ROUTES.LOGIN);
      return;
    }

    // 서버에서 세션 유효성 검증 (Redis에 refresh token이 있는지 확인)
    const isSessionValid = await validateSession();

    if (!isSessionValid) {
      alert('세션이 만료되었습니다. 다시 로그인해주세요.');
      next(ROUTES.LOGIN);
      return;
    }
  }

  // 특정 권한이 필요한 페이지인데 권한이 없는 경우
  if (requiredRoles && currentUser?.role) {
    if (!requiredRoles.includes(currentUser.role)) {
      alert('접근 권한이 없습니다.');
      // 일반 사용자는 사용자 정보 페이지로
      next(ROUTES.USER_INFO);
      return;
    }
  }

  // 로그인한 상태에서 로그인/회원가입 페이지 접근 시 권한에 따라 리다이렉트
  if (hasLocalAuth && (to.path === ROUTES.LOGIN || to.path === ROUTES.SIGNUP)) {
    const userRole = currentUser?.role;
    if (userRole === 'ADMIN' || userRole === 'DEVELOPER') {
      // 관리자/개발자는 관리자 대시보드로
      next(ROUTES.ADMIN_DASHBOARD);
    } else {
      // 일반 사용자는 사용자 정보 페이지로
      next(ROUTES.USER_INFO);
    }
    return;
  }

  next();
});

export default router;
