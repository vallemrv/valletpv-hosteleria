import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    VitePWA({
      registerType: 'autoUpdate',
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        maximumFileSizeToCacheInBytes: 4000000,
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/fonts\.googleapis\.com\/.*/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'google-fonts-cache',
              expiration: {
                maxEntries: 10,
                maxAgeSeconds: 60 * 60 * 24 * 365 // <== 365 days
              }
            }
          }
        ]
      },
      includeAssets: ['favicon.ico', 'img/icons/*.png', 'robots.txt'],
      manifestFilename: 'manifest.webmanifest',
      useCredentials: false,
      disable: false
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  build: {
    // Aumentar el límite de advertencia a 800kB para chunks grandes esperados
    chunkSizeWarningLimit: 800,
    rollupOptions: {
      output: {
        // Función para dividir chunks manualmente para optimizar la carga
        manualChunks(id) {
          // Chunks para dependencias principales de Vue
          if (id.includes('vue') && !id.includes('vuetify')) {
            return 'vue-vendor'
          }
          if (id.includes('vue-router')) {
            return 'vue-vendor'
          }
          if (id.includes('pinia')) {
            return 'vue-vendor'
          }
          
          // Mantener Vuetify completo en un solo chunk para evitar dependencias circulares
          if (id.includes('vuetify')) {
            return 'vuetify-vendor'
          }
          
          // Chunk separado para iconos de Material Design
          if (id.includes('@mdi/font')) {
            return 'mdi-vendor'
          }
          
          // Chunk separado para utilidades y otras dependencias
          if (id.includes('axios') || id.includes('marked') || id.includes('ws')) {
            return 'utils-vendor'
          }
          
          // Otros node_modules en chunk vendor genérico
          if (id.includes('node_modules')) {
            return 'vendor'
          }
        }
      }
    }
  }
})
