import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView'
import CamarerosView from '@/views/CamarerosView'
import PreciosView from '@/views/PreciosView'
import TeclasView from '@/views/TeclasView'
import CajasView from '@/views/CajasView'
import CamarerosPaseView from '@/views/CamarerosPaseView'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/camareros',
    component: CamarerosView,
    name: 'camareros'
  },
  {
    path: '/camareros_pase',
    component: CamarerosPaseView,
    name: 'camareros_pase'
  },
  {
    path: '/teclados',
    component: TeclasView,
    name: 'teclados'
  },
  {
    path: '/precios',
    component: PreciosView,
    name: 'precios'
  },
  {
    path: '/cierres_caja',
    component: CajasView,
    name: 'cajas'
  },
]

const router = createRouter({
  base: "/",
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
