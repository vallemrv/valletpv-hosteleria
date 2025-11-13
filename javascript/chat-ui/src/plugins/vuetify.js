import 'vuetify/styles'; // Estilos base de Vuetify
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';
import '@mdi/font/css/materialdesignicons.css'; // Íconos

const vuetify = createVuetify({
  components,
  directives,
});

export default vuetify;