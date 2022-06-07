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
  base: "/",
  history: createWebHistory("/"),
  routes
})

export default router
