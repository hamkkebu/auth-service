import { createRouter, createWebHistory } from "vue-router";
import LogIn from '@/components/views/LogIn.vue';
import SignUp from "@/components/views/SignUp.vue";
import UserInfo from "@/components/views/UserInfo.vue";
import LeaveUser from "@/components/views/LeaveUser.vue";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/login'
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
    const refreshToken = localStorage.getItem('refreshToken');
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

    if (requiresAuth) {
        if (!token) {
            // No token, redirect to login
            next('/login');
            return;
        }

        // Token exists - API interceptor will handle validation and auto-refresh if needed
        // Just proceed to the route
        next();
    } else {
        next();
    }
});

export default router;