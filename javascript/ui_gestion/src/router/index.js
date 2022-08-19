import { createRouter, createWebHistory } from 'vue-router'
import { gestionRoutes  } from './gestion'
import { ventasRoutes } from "./ventas"
import HomeView from '@/views/HomeView'


const routes = [
  ... gestionRoutes, ...ventasRoutes,
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
]


const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
