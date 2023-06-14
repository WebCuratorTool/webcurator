import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import DashboardView from '@/views/DashboardView.vue'
import QueueView from '@/views/QueueView.vue'
import TargetInstanceView from '@/views/TargetInstanceView.vue'
import TargetView from '@/views/TargetView.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
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
      path: '/target',
      name: 'target',
      component: TargetView
    },
  ]
})

export default router
