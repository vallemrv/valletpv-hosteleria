import { build } from 'vite';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { copyFileSync, existsSync, mkdirSync } from 'node:fs';

const __dirname = dirname(fileURLToPath(import.meta.url));

async function buildServiceWorker() {
  console.log('🔨 Compilando Service Worker...');
  
  try {
    // Compilar el Service Worker desde src/lib
    await build({
      configFile: false,
      build: {
        lib: {
          entry: join(__dirname, '../src/lib/serviceWorker.ts'),
          name: 'ServiceWorker',
          fileName: () => 'sw-custom.js',
          formats: ['iife']
        },
        outDir: join(__dirname, '../dev-dist'),
        emptyOutDir: false,
        rollupOptions: {
          output: {
            entryFileNames: 'sw-custom.js',
            inlineDynamicImports: true
          }
        },
        minify: false,
        sourcemap: false
      },
      define: {
        'process.env.NODE_ENV': JSON.stringify('production')
      },
      resolve: {
        alias: {
          '@': join(__dirname, '../src')
        }
      }
    });
    
    // Copiar a public para desarrollo
    copyFileSync(
      join(__dirname, '../dev-dist/sw-custom.js'), 
      join(__dirname, '../public/sw-custom.js')
    );
    
    // Copiar a dist para producción (si existe el directorio)
    const distDir = join(__dirname, '../dist');
    if (existsSync(distDir)) {
      copyFileSync(
        join(__dirname, '../dev-dist/sw-custom.js'),
        join(distDir, 'sw-custom.js')
      );
      console.log('✅ Service Worker también copiado a dist/sw-custom.js');
    }
    
    console.log('✅ Service Worker compilado en dev-dist/sw-custom.js y copiado a public/sw-custom.js');
  } catch (error) {
    console.error('❌ Error compilando Service Worker:', error);
    process.exit(1);
  }
}

buildServiceWorker();
