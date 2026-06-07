import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    meta: { requiresAuth: true },
    redirect: '/',
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'instruments',
        name: 'Instruments',
        component: () => import('../views/Instruments.vue')
      },
      {
        path: 'models',
        name: 'Models',
        component: () => import('../views/Models.vue')
      },
      {
        path: 'reservations',
        name: 'Reservations',
        component: () => import('../views/Reservations.vue')
      },
      {
        path: 'revenue',
        name: 'Revenue',
        component: () => import('../views/Revenue.vue')
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/Users.vue')
      },
      {
        path: 'maintenance',
        name: 'Maintenance',
        component: () => import('../views/Maintenance.vue')
      },
      {
        path: 'scan',
        name: 'Scan',
        component: () => import('../views/Scan.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router