import notification from "@/plugins/notificacion"
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify'
import { loadFonts } from './plugins/webfontloader'

loadFonts()

// Registrar Service Worker para habilitar instalación PWA
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js', { scope: '/' })
      .then(registration => {
        console.log('✅ Service Worker registrado correctamente');
      })
      .catch(error => {
        console.error('❌ Error al registrar Service Worker:', error);
      });
  });
}

const app = createApp(App)
const pinia = createPinia()

app
  .use(pinia)
  .use(router)
  .use(vuetify)
  .use(notification)
  .mount('#app')