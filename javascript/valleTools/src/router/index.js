import { createRouter, createWebHistory } from 'vue-router'
import MainView from '@/views/dashBoard/MainView.vue';
import ChatView from "@/views/ia/ChatView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainView,
    },
    {
      path: "/chat/:titulo/:tipo/:opciones",
      name: "ChatView",
      component: ChatView,
      props: true
    }
  ]
})

export default router
