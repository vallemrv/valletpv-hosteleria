import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView'
import CamarerosView from '@/views/CamarerosView'
import PreciosView from '@/views/PreciosView'
import TecladosView from '@/views/TecladosView'
import CajasView from '@/views/CajasView'

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
    path: '/teclados',
    component: TecladosView,
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
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
