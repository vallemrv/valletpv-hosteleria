import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ArqueosView from '@/views/ArqueosView'
import MensajesView from '@/views/MensajesView'

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
