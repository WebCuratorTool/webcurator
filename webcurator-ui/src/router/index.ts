import { createRouter, createWebHistory } from 'vue-router';
import Dashboard from '../views/Dashboard.vue';
import { useAuthStore, LoginPagePath } from '@/utils/rest.api';
import { progressVisible } from '@/utils/progress';

export const routes = {
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      children: [
        {
          path: 'login',
          name: 'login',
          component: () => import('@/views/login/LoginView.vue')
        },
        {
          path: 'index.html',
          name: 'index',
          component: () => import('@/layout/MainLayoutView.vue')
        },
        {
          path: '',
          name: 'main-layout',
          component: () => import('@/layout/MainLayoutView.vue'),
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
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
};

const router = createRouter(routes);
router.beforeEach(async (to: any) => {
  progressVisible.value = false;
  const auth = useAuthStore();
  const loggedIn = await auth.isAuthenticated();
  if (to.path !== LoginPagePath && !loggedIn) {
    auth.setRedirectPath(to.fullPath);
    return { path: LoginPagePath };
  }
});

export default router;
