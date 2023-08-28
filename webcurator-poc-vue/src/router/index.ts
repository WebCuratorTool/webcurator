import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import DashboardView from '@/views/DashboardView.vue'
import QueueView from '@/views/QueueView.vue'
import TargetInstanceView from '@/views/TargetInstanceView.vue'
import TargetsView from '@/views/TargetsView.vue'
import TargetView from '@/views/TargetView.vue'
import LoginView from '@/views/LoginView.vue'

import { useAuthStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue')
    },
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView
    },
    {
      path: '/queue',
      name: 'queue',
      component: QueueView
    },

    {
      path: '/harvest-authorisations',
      name: 'harvest-authorisations',
      component: QueueView
    },

    {
      path: '/target/:id',
      name: 'target',
      component: TargetView
    },

    {
      path: '/targets',
      name: 'targets',
      component: TargetsView
    },

    {
      path: '/groups',
      name: 'groups',
      component: QueueView
    },

    {
      path: '/management',
      name: 'management',
      component: QueueView
    },

    {
      path: '/target_instance',
      name: 'target_instance',
      component: TargetInstanceView
    },

    // {
    //   path: '/target/:oid',
    //   name: 'target-oid',
    //   component: TargetView
    // },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
  ]
})

router.beforeEach(async (to) => {
  const publicPages = ['/login']
  const authRequired = !publicPages.includes(to.path)
  const auth = useAuthStore()
  if (authRequired && !auth.isLoggedIn) {
    auth.returnUrl = to.fullPath
    return '/login'
  }
})

export default router
