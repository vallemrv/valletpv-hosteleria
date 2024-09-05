import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import tools from '@/tools'
import vuetify from './plugins/vuetify';

createApp(App)
  .use(router)
  .use(store)
  .use(vuetify)
  .use(tools)
  .mount('#app')
