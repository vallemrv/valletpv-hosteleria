import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    // Aumentar el límite de advertencia a 800kB para chunks grandes esperados
    chunkSizeWarningLimit: 800,
    rollupOptions: {
      output: {
        // Dividir chunks manualmente para optimizar la carga
        manualChunks: {
          // Chunk separado para Vue y sus dependencias centrales
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          
          // Chunk separado para Vuetify (la librería más pesada)
          'vuetify-vendor': ['vuetify'],
          
          // Chunk separado para iconos de Material Design
          'mdi-vendor': ['@mdi/font'],
          
          // Chunk separado para utilidades de gráficos
          'chart-vendor': ['chart.js', 'vue-chartjs'],
          
          // Chunk separado para otras dependencias
          'utils-vendor': ['axios', '@vuepic/vue-datepicker']
        },
        // Función para chunks más específicos basados en el módulo
        manualChunks(id) {
          // Separar node_modules en chunks por categoría
          if (id.includes('node_modules')) {
            // Vuetify tiene muchos componentes, separarlos por tipo
            if (id.includes('vuetify/lib/components')) {
              return 'vuetify-components'
            }
            if (id.includes('vuetify/lib/composables')) {
              return 'vuetify-composables'
            }
            if (id.includes('vuetify/lib/directives')) {
              return 'vuetify-directives'
            }
            // Separar Chart.js en su propio chunk
            if (id.includes('chart.js')) {
              return 'chartjs-vendor'
            }
          }
        }
      }
    }
  }
})
