import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'CamarerosHome',
    component: () => import('../views/CamarerosView.vue'),
  },
  {
    path: '/empresas',
    name: 'Empresas',
    component: () => import('../views/EmpresasView.vue'),
  },
  {
    path: '/camareros',
    name: 'CamarerosManager',
    component: () => import('../views/CamarerosManagerView.vue'),
  },
  {
    path: '/mesas',
    name: 'Mesas',
    component: () => import('../views/MesasView.vue'),
  },
  {
    path: '/cuenta',
    name: 'Cuenta',
    component: () => import('../views/CuentaView.vue'),
  },
  {
    path: '/arqueo',
    name: 'Arqueo',
    component: () => import('../views/ArqueoView.vue'),
  }
  
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

export default router;

// Add this declaration to extend ImportMeta type
declare global {
  interface ImportMeta {
    env: {
      BASE_URL: string;
      [key: string]: any;
    };
  }
}
