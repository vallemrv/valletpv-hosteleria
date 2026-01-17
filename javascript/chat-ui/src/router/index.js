import { createRouter, createWebHistory } from 'vue-router';
import ChatView from '../views/ChatView.vue';

const routes = [
  {
    path: '/',
    name: 'Chat',
    component: ChatView,
  },
  {
    path: '/tools',
    name: 'Tools',
    component: () => import('../views/ToolsView.vue'),
  },
  {
    path: '/tools/waiters',
    name: 'WaitersTool',
    component: () => import('../views/tools/WaitersTool.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;