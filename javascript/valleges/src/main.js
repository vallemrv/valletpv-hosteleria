import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify'
import tools from '@/tools'

createApp(App)
  .use(router)
  .use(store)
  .use(vuetify)
  .use(tools)
  .mount('#app')
