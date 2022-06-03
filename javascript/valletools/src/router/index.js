import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ArqueosView from '@/views/ArqueosView'

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
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
