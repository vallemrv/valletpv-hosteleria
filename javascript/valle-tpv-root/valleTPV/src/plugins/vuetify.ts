// Vuetify
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { mdi } from 'vuetify/iconsets/mdi'
import '@mdi/font/css/materialdesignicons.css'

import * as temas from 'valle-tpv-lib';

const { miTemaModerno, valleDark, valleLight, pastelElegance } = temas as any;

export default createVuetify({
  components,
  directives,
  icons: {
    defaultSet: 'mdi',
    sets: {
      mdi,
    },
  },
  theme: {
    defaultTheme: 'pastelElegance',
    themes: {
      miTemaModerno,
      valleDark,
      valleLight,
      pastelElegance
    }
  },
})
