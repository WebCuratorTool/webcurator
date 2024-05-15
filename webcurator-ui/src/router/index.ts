import { createRouter, createWebHistory } from 'vue-router';
import Dashboard from '../views/Dashboard.vue';

export const routes={
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/wct',
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
          component: () => import('@/views/target/Target.vue')
        },
        {
          path: 'targets/:mode/:id',
          name: 'target-tabview-exist',
          component: () => import('@/views/target/Target.vue')
        },
        {
          path: 'targets/new',
          name: 'target-tabview-new',
          component: () => import('@/views/target/Target.vue')
        }
      ]
    }
  ]
};

const router = createRouter(routes);

export default router;