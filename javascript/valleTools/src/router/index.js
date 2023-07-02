import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '@/views/dashboard/HomeView.vue';
import LoginView from '@/views/formlogin/LoginView.vue';
import RegistroView from '@/views/formlogin/RegistroView.vue';
import TecladosView from "@/views/gestion/TecladosView.vue";
import ChatView from '@/views/ia/ChatView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/signup',
      name: 'RegistroView',
      component: RegistroView,
    },
    {
      path: '/login',
      name: 'Login',
      component: LoginView,
    },
    {
      path: '/',
      component: HomeView,
      name: 'HomeView',
    },
    {
      path: '/:view',
      component: HomeView,
      name: 'view',
      props: true
    },
    {
      path: '/chat',
      component: ChatView,
      name: 'ChatView',
    },
    {
      path: '/teclados/:seccion_id/:tecla_id/:nivel/',
      name: 'teclados_child', // nombre único
      component: TecladosView,
      props: true // Esto pasará los parámetros de ruta al componente como props
    },
    {
      path: '/teclados/:seccion_id/:tecla_id',
      name: 'teclados_teclas', // nombre único
      component: TecladosView,
      props: true // Añadido props: true
    },
    {
      path: '/teclados/:seccion_id',
      name: 'teclados_seccion', // nombre único
      component: TecladosView,
      props: true 
    },
    {
      path: '/teclados',
      name: 'TecladosView',
      component: TecladosView,
    }
  
  ]
});


export default router
