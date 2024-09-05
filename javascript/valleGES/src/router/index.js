import { createRouter, createWebHistory } from 'vue-router'

import HomeView from "@/views/HomeView.vue"
import CamarerosView from "@/views/gestion/camareros/CamarerosView.vue"

const routes = [
  {
    path: '/camareros',
    name: 'camareros',
    component: CamarerosView
  },
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
]


const router = createRouter({
  history: createWebHistory("/"),
  routes
})

export default router
