import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import DashboardView from '@/views/DashboardView.vue'
import QueueView from '@/views/QueueView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue')
    },
    {
      path: '/dashboard',
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
      path: '/targets',
      name: 'targets',
      component: QueueView
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

  ]
})

export default router
