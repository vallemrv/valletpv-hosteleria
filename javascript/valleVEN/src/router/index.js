import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ArqueosView from '@/views/ArqueosView'
import MensajesView from '@/views/MensajesView'
import MesasAbiertasView from '@/views/MesasAbiertasView'
import NulosView from '@/views/NulosView'
import ListadoMesasView from '@/views/ListadoMesasView'

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
    path :'/listado_mesas',
    name: 'listado_mesas',
    component: ListadoMesasView
  },
  {
    path :'/nulos',
    name: 'nulos',
    component: NulosView
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
