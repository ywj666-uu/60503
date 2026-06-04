import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    component: () => import('@/views/HomePage.vue')
  },
  {
    path: '/login',
    component: () => import('@/views/LoginPage.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    component: () => import('@/views/RegisterPage.vue'),
    meta: { guest: true }
  },
  {
    path: '/teacher',
    component: () => import('@/views/teacher/TeacherDashboard.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
    children: [
      {
        path: '',
        component: () => import('@/views/teacher/CourseManage.vue')
      },
      {
        path: 'courses/:courseId/segments',
        component: () => import('@/views/teacher/SegmentManage.vue')
      }
    ]
  },
  {
    path: '/student',
    component: () => import('@/views/student/StudentDashboard.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
      {
        path: '',
        component: () => import('@/views/student/CourseDetail.vue')
      },
      {
        path: 'practice/:segmentId',
        component: () => import('@/views/student/PracticePage.vue')
      },
      {
        path: 'progress',
        component: () => import('@/views/student/MyProgress.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.guest && authStore.isAuthenticated) {
    next(authStore.user?.role === 'TEACHER' ? '/teacher' : '/student')
  } else if (to.meta.role && authStore.user?.role !== to.meta.role) {
    next('/')
  } else {
    next()
  }
})

export default router
