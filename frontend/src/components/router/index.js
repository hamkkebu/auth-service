import { createRouter, createWebHistory } from "vue-router";
import LogIn from '@/components/views/LogIn.vue';
import SignUp from "@/components/views/SignUp.vue";
import UserInfo from "@/components/views/UserInfo.vue";
import LeaveUser from "@/components/views/LeaveUser.vue";
import AdminDashboard from "@/components/views/AdminDashboard.vue";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'Home',
            redirect: () => {
                // 로그인 여부와 권한에 따라 리다이렉트
                const token = localStorage.getItem('authToken');
                if (!token) {
                    return '/login';
                }

                // 사용자 정보에서 권한 확인
                const userJson = localStorage.getItem('currentUser');
                if (userJson) {
                    try {
                        const user = JSON.parse(userJson);
                        const role = user.role;
                        // ADMIN 또는 DEVELOPER는 관리자 대시보드로
                        if (role === 'ADMIN' || role === 'DEVELOPER') {
                            return '/admin';
                        }
                    } catch (error) {
                        console.error('Failed to parse user:', error);
                    }
                }
                // 일반 사용자는 userinfo로
                return '/userinfo';
            }
        },
        {
            path: '/login',
            name: 'LogIn',
            component: LogIn,
        },
        {
            path: '/signup',
            name: 'SignUp',
            component: SignUp,
        },
        {
            path: '/admin',
            name: 'AdminDashboard',
            component: AdminDashboard,
            meta: { requiresAuth: true, requiresAdmin: true }
        },
        {
            path: '/userinfo',
            name: 'UserInfo',
            component: UserInfo,
            meta: { requiresAuth: true }
        },
        {
            path: '/leaveuser',
            name: 'LeaveUser',
            component: LeaveUser,
            meta: { requiresAuth: true }
        }
    ]
});

// Navigation guard to protect routes that require authentication
router.beforeEach(async (to, from, next) => {
    const token = localStorage.getItem('authToken');
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);

    if (requiresAuth) {
        if (!token) {
            // No token, redirect to login
            next('/login');
            return;
        }

        // 관리자 권한이 필요한 페이지인 경우
        if (requiresAdmin) {
            const userJson = localStorage.getItem('currentUser');
            if (!userJson) {
                // 사용자 정보 없음
                alert('접근 권한이 없습니다.');
                next('/userinfo');
                return;
            }

            try {
                const user = JSON.parse(userJson);
                const role = user.role;

                // ADMIN 또는 DEVELOPER만 접근 가능
                if (role !== 'ADMIN' && role !== 'DEVELOPER') {
                    alert('관리자 권한이 필요합니다.');
                    next('/userinfo');
                    return;
                }
            } catch (error) {
                console.error('Failed to parse user:', error);
                alert('사용자 정보를 확인할 수 없습니다.');
                next('/login');
                return;
            }
        }

        // Token exists and role check passed - API interceptor will handle validation and auto-refresh if needed
        next();
    } else {
        next();
    }
});

export default router;