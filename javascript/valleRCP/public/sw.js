// Service Worker básico para permitir instalación PWA
const CACHE_NAME = 'vallercp-v1';

// Instalación - no cachear nada por ahora
self.addEventListener('install', (event) => {
  console.log('Service Worker instalado');
  self.skipWaiting();
});

// Activación - limpiar cachés antiguos si los hay
self.addEventListener('activate', (event) => {
  console.log('Service Worker activado');
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME) {
            return caches.delete(cacheName);
          }
        })
      );
    }).then(() => self.clients.claim())
  );
});

// Fetch - dejar pasar todas las peticiones sin interceptar
self.addEventListener('fetch', (event) => {
  // No hacer nada, dejar que las peticiones pasen normalmente
  return;
});
