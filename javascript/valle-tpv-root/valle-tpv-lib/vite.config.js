import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  build: {
    lib: {
      entry: 'src/index.ts',
      name: 'ValleTpvLib',
      fileName: (format) => format === 'es' ? 'index.esm.js' : 'index.js',
      formats: ['es', 'cjs']
    },
    rollupOptions: {
      external: ['vue', 'vuetify', 'vuetify/lib', 'vuetify/components', 'pinia', 'dexie', 'vue-router', '@vueuse/core'],
      output: {
        globals: {
          vue: 'Vue',
          vuetify: 'Vuetify',
          pinia: 'Pinia',
          dexie: 'Dexie',
          'vue-router': 'VueRouter'
        },
        exports: 'named',
        assetFileNames: 'valle-tpv-lib.[ext]',
        // Preservar la estructura de módulos para mejor tree-shaking
        preserveModules: false,
        // Asegurar que las importaciones de Vue estén correctas
        interop: 'auto'
      }
    },
    // Optimización
    minify: false, // No minificar para debugging
    sourcemap: true
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
});
