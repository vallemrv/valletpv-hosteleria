import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import vuetify from './plugins/vuetify'
import router from './router'

import ValleTpvLib from 'valle-tpv-lib'
import { useInstruccionesStore, initCashKeeperWatcher } from 'valle-tpv-lib'
import { registerCustomServiceWorker } from './lib/utils/registerSW'

import './assets/css/global.css'
import './assets/css/touch-scroll.css'

// Prevenir zoom mediante eventos táctiles
let lastTouchEnd = 0;
document.addEventListener('touchend', (event) => {
  const now = Date.now();
  if (now - lastTouchEnd <= 300) {
    event.preventDefault();
  }
  lastTouchEnd = now;
}, { passive: false });

// Prevenir zoom mediante gestos de pinza
document.addEventListener('gesturestart', (e) => {
  e.preventDefault();
}, { passive: false });

document.addEventListener('gesturechange', (e) => {
  e.preventDefault();
}, { passive: false });

document.addEventListener('gestureend', (e) => {
  e.preventDefault();
}, { passive: false });

// Prevenir zoom con rueda del ratón + Ctrl
document.addEventListener('wheel', (e) => {
  if (e.ctrlKey) {
    e.preventDefault();
  }
}, { passive: false });

// Prevenir zoom con teclado (Ctrl + / Ctrl -)
document.addEventListener('keydown', (e) => {
  if ((e.ctrlKey || e.metaKey) && (e.key === '+' || e.key === '-' || e.key === '=')) {
    e.preventDefault();
  }
}, { passive: false });

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(vuetify)
app.use(router)

// Registrar el plugin de valle-tpv-lib de manera más segura
if (ValleTpvLib && typeof ValleTpvLib.install === 'function') {
  app.use(ValleTpvLib)
} else {
  console.error('ValleTpvLib plugin no está disponible')
}

app.mount('#app')

initCashKeeperWatcher()

// Registrar el Service Worker personalizado
registerCustomServiceWorker().then(() => {
  console.info('Service Worker personalizado inicializado');
});

// Configurar sistema de instrucciones después de montar la app
const setupInstructionSystem = async () => {
  const instruccionesStore = useInstruccionesStore()
  
  // Configurar listener para mensajes del Service Worker
  instruccionesStore.setupServiceWorkerListener()
  
  // Inicializar contador desde la base de datos
  await instruccionesStore.refreshFromDB()
  
  console.info('Sistema de instrucciones inicializado')
}

// Ejecutar configuración
setupInstructionSystem().catch(error => {
  console.error('Error configurando sistema de instrucciones:', error)
})