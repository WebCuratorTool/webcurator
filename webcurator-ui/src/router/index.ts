import { createRouter, createWebHistory } from 'vue-router';
import Dashboard from '../views/Dashboard.vue';

export const routes={
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: Dashboard
        },
        {
          path: 'targets',
          name: 'target-list',
          // route level code-splitting
          // this generates a separate chunk (About.[hash].js) for this route
          // which is lazy-loaded when the route is visited.
          component: () => import('@/views/target/TargetList.vue')
        },
        {
          path: 'targets/:id',
          name: 'target',
          component: () => import('@/views/target/Target.vue')
        },
        {
          path: 'targets/new',
          name: 'target-new',
          component: () => import('@/views/target/TargetNew.vue')
        }
      ]
    }
  ]
};

const router = createRouter(routes);

export default router;