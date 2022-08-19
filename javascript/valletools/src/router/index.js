import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ArqueosView from '@/views/ArqueosView'
import MensajesView from '@/views/MensajesView'
import MesasAbiertasView from '@/views/MesasAbiertasView'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path :'/arqueos',
    name: 'arqueos',
    component: ArqueosView
  },
  {
    path :'/mesas_abiertas',
    name: 'mesas_abiertas',
    component: MesasAbiertasView
  },
  {
    path :'/mensajes',
    name: 'Mensajes',
    component: MensajesView
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
