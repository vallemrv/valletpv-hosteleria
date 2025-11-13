import './registerServiceWorker'
import notification from "@/plugins/notificacion"
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify'
import { loadFonts } from './plugins/webfontloader'

loadFonts()

const app = createApp(App)
const pinia = createPinia()

app
  .use(pinia)
  .use(router)
  .use(vuetify)
  .use(notification)
  .mount('#app')