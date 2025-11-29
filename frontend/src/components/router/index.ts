import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import LogIn from '@/components/views/LogIn.vue';
import SignUp from '@/components/views/SignUp.vue';
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
        // 로그인되어 있으면 권한에 따라 리다이렉트
        if (userRole === 'ADMIN' || userRole === 'DEVELOPER') {
          return ROUTES.ADMIN_DASHBOARD;
        } else {
          return ROUTES.USER_INFO;
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
 * 라우터 가드: Keycloak 기반 인증 및 권한 확인
 */
router.beforeEach(async (to, from, next) => {
  const { isAuthenticated, currentUser, hasRole } = useAuth();

  const requiresAuth = to.meta.requiresAuth;
  const requiredRoles = to.meta.requiredRoles as UserRole[] | undefined;

  // 인증이 필요한 페이지인 경우
  if (requiresAuth) {
    if (!isAuthenticated.value) {
      // 로그인되지 않았으면 로그인 페이지로
      next(ROUTES.LOGIN);
      return;
    }
  }

  // 특정 권한이 필요한 페이지인 경우
  if (requiredRoles && requiredRoles.length > 0) {
    const hasRequiredRole = requiredRoles.some(role => hasRole(role));
    if (!hasRequiredRole) {
      alert('접근 권한이 없습니다.');
      // 일반 사용자는 사용자 정보 페이지로
      next(ROUTES.USER_INFO);
      return;
    }
  }

  // 로그인한 상태에서 로그인/회원가입 페이지 접근 시 권한에 따라 리다이렉트
  if (isAuthenticated.value && (to.path === ROUTES.LOGIN || to.path === ROUTES.SIGNUP)) {
    const userRole = currentUser.value?.role;
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
