/* eslint-disable no-console */

import { registerSW } from 'virtual:pwa-register'

const updateSW = registerSW({
  onNeedRefresh() {
    console.log('New content is available; please refresh.')
  },
  onOfflineReady() {
    console.log('App ready to work offline')
  },
  onRegistered(registration) {
    console.log('Service worker has been registered.')
  },
  onRegisterError(error) {
    console.error('Error during service worker registration:', error)
  }
})
