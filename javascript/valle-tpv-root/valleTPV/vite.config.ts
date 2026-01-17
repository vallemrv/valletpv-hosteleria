// COPIA Y PEGA ESTO EN: valleTPV/vite.config.js

import { fileURLToPath, URL } from 'node:url';
import path from 'node:path';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import { VitePWA } from 'vite-plugin-pwa';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig(({ mode }) => {
  return {
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
        'valle-tpv-lib': fileURLToPath(new URL('./src/lib/index.ts', import.meta.url))
      },
      dedupe: ['vue', 'pinia', 'vuetify', 'vue-router', '@vueuse/core']
    },
    optimizeDeps: {
      include: ['vue', 'pinia', 'vuetify', 'vue-router']
    },
    plugins: [
      vue(),
      vueJsx(),
      VitePWA({
        registerType: 'autoUpdate',
        devOptions: {
          enabled: true,
        },
        workbox: {
          globPatterns: ['**/*.{js,css,html,ico,png,svg,woff,woff2}'],
          maximumFileSizeToCacheInBytes: 5 * 1024 * 1024,
        },
        includeAssets: ['favicon.ico', 'apple-touch-icon.png', 'mask-icon.svg'],
        manifest: {
          name: 'Valle TPV - Hostelería',
          short_name: 'ValleTPV',
          description: 'Sistema de punto de venta para hostelería',
          theme_color: '#1976d2',
          background_color: '#ffffff',
          display: 'fullscreen',
          display_override: ['fullscreen', 'standalone'],
          orientation: 'portrait',
          scope: '/',
          start_url: '/',
          icons: [
            {
              src: 'pwa-192x192.png',
              sizes: '192x192',
              type: 'image/png',
            },
            {
              src: 'pwa-512x512.png',
              sizes: '512x512',
              type: 'image/png',
            },
            {
              src: 'pwa-512x512.png',
              sizes: '512x512',
              type: 'image/png',
              purpose: 'any maskable',
            },
          ],
        },
      }),
    ],
    build: {
      commonjsOptions: {
        include: [/node_modules/]
      },
      rollupOptions: {
        output: {
          manualChunks(id) {
            // Vue core en un chunk
            if (id.includes('node_modules/vue/') ||
              id.includes('node_modules/@vue/') ||
              id.includes('node_modules/vue-router/') ||
              id.includes('node_modules/pinia/')) {
              return 'vue-vendor';
            }
            // Vuetify en otro chunk
            if (id.includes('node_modules/vuetify/')) {
              return 'vuetify-vendor';
            }
            // Todo lo demás de node_modules en vendor
            if (id.includes('node_modules/')) {
              return 'vendor';
            }
          }
        }
      }
    }
  };
});