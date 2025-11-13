import './assets/main.css';

import { createApp } from 'vue';
import { createPinia } from 'pinia'; // Importa Pinia
import App from './App.vue';
import router from './router';
import vuetify from './plugins/vuetify'; // Importa Vuetify
import '@mdi/font/css/materialdesignicons.css'; // Importa los íconos aquí

const app = createApp(App);
const pinia = createPinia(); // Crea una instancia de Pinia

app.use(router);
app.use(pinia); // Usa Pinia
app.use(vuetify); // Usa Vuetify
app.mount('#app');