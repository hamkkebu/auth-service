import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import UserInfo from '@/components/views/UserInfo.vue';
import LeaveUser from '@/components/views/LeaveUser.vue';
import AdminDashboard from '@/components/views/AdminDashboard.vue';
import { ROUTES } from '@/constants';
import type { UserRole } from '@/types/domain.types';
import { useAuth } from '@/composables/useAuth';

const routes: RouteRecordRaw[] = [
  {
    path: ROUTES.HOME,
    name: 'Home',
    redirect: () => {
      const { isAuthenticated, currentUser } = useAuth();

      if (isAuthenticated.value && currentUser.value) {
        const userRole = currentUser.value.role;
        if (userRole === 'ADMIN' || userRole === 'DEVELOPER') {
          return ROUTES.ADMIN_DASHBOARD;
        } else {
          return ROUTES.USER_INFO;
        }
      }

      // 로그인 안되어 있으면 사용자 정보 페이지로 (initAuth에서 로그인 리다이렉트)
      return ROUTES.USER_INFO;
    },
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
  // 기존 로그인/회원가입 경로 -> 홈으로 리다이렉트 (Keycloak이 처리)
  {
    path: '/login',
    redirect: '/',
  },
  {
    path: '/signup',
    redirect: '/',
  },
  {
    path: '/logout',
    redirect: '/',
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

/**
 * 라우터 가드: Keycloak SSO 기반 인증 및 권한 확인
 */
router.beforeEach(async (to, from, next) => {
  const { isAuthenticated, currentUser, hasRole, login, initAuth, isInitialized } = useAuth();

  // 초기화가 안 되어 있으면 초기화
  if (!isInitialized.value) {
    await initAuth();
  }

  const requiresAuth = to.meta.requiresAuth;
  const requiredRoles = to.meta.requiredRoles as UserRole[] | undefined;

  // 인증이 필요한 페이지인 경우
  if (requiresAuth) {
    if (!isAuthenticated.value) {
      // Keycloak 로그인 페이지로 리다이렉트
      login(window.location.origin + to.fullPath);
      return;
    }
  }

  // 특정 권한이 필요한 페이지인 경우
  if (requiredRoles && requiredRoles.length > 0) {
    const hasRequiredRole = requiredRoles.some(role => hasRole(role));
    if (!hasRequiredRole) {
      alert('접근 권한이 없습니다.');
      next(ROUTES.USER_INFO);
      return;
    }
  }

  next();
});

export default router;
