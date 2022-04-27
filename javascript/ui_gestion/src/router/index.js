import { createRouter, createWebHistory } from 'vue-router'
import { gestionRoutes  } from './gestion'
import HomeView from '@/views/HomeView'
import GestionView from '@/views/gestion/GestionView'
import VentasView from '@/views/ventas/VentasView'


const routes = [
  ... gestionRoutes,
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/gestion',
    name: 'gestion',
    component: GestionView
  },
  {
    path: '/ventas',
    name: 'ventas',
    component: VentasView
  },
]


const router = createRouter({
  base: "/",
  history: createWebHistory("/"),
  routes
})

export default router
