import { createRouter, createWebHistory } from 'vue-router'
import MainView from '@/views/dashBoard/MainView.vue';
import CamarerosView from '@/views/gestion/CamarerosView.vue';
import FamiliasView from '@/views/gestion/FamiliasView.vue';
import TecladosView from '@/views/gestion/TecladosView.vue';
import MesasView from '@/views/gestion/MesasView.vue';
import ModificarTeclas from '@/views/gestion/ModificarTeclas.vue';
import Profile from '@/views/Profile.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainView,
      name: 'dashBoard',
    },
    {
      path: '/camareros',
      component: CamarerosView,
      name: 'camareros',
    },
    {
      path: '/familias',
      component: FamiliasView,
      name: 'familias',
    },
    { 
      path: '/modificar/:tipo',
      component: ModificarTeclas,
      name: 'modificar_tipo',
      props: true
    },
    { 
      path: '/modificar/',
      component: ModificarTeclas,
      name: 'modificar',
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
    },
    {
      path: '/mesas/:zona_id/:mesa_id',
      name: 'mesas_detalle', // nombre único
      component: MesasView,
      props: true // Añadido props: true
    },
    {
      path: '/mesas/:zona_id',
      name: 'mesas_zona', // nombre único
      component: MesasView,
      props: true 
    },
    {
      path: '/mesas',
      name: 'MesasView',
      component: MesasView,
    },
    {
      path: '/profile',
      name: 'Profile',
      component: Profile,

    }
    
  ]
})

export default router
