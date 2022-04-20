import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView'
import CamarerosView from '@/views/CamarerosView'

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
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
