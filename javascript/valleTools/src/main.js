import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import '@mdi/font/css/materialdesignicons.css'; // Importa Material Design Icons

// Vuetify
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import * as labsComponents from 'vuetify/labs/components'

const vuetify = createVuetify({
  components:{
    ...components,
    ...labsComponents,
  },
  directives,
})


const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(vuetify)


app.mount('#app')
