// Registro del Service Worker personalizado para la cola de instrucciones

export async function registerCustomServiceWorker() {
  // Verificar soporte de Service Worker
  if (!('serviceWorker' in navigator)) {
    console.warn('⚠️ Service Worker no está soportado en este navegador');
    if (typeof navigator !== 'undefined' && 'userAgent' in navigator) {
      console.warn('Navegador:', (navigator as any).userAgent);
    }
    return null;
  }

  // Verificar contexto seguro
  if (typeof window !== 'undefined' && !window.isSecureContext) {
    console.warn('⚠️ Service Worker requiere un contexto seguro (HTTPS o localhost)');
    console.warn('URL actual:', window.location.href);
    console.warn('Usa https:// o http://localhost para habilitar Service Workers');
    return null;
  }

  try {
    // Registrar el Service Worker personalizado
    console.log('🔄 Intentando registrar Service Worker personalizado...');
    
    const registration = await navigator.serviceWorker.register('/sw-custom.js', {
      scope: '/',
      type: 'classic'
    });

    console.log('✅ Service Worker personalizado registrado:', registration.scope);

    // Esperar a que esté activo
    if (registration.active) {
      console.log('✅ Service Worker ya está activo');
    } else if (registration.installing) {
      console.log('⏳ Service Worker instalándose...');
      registration.installing.addEventListener('statechange', (e) => {
        const sw = e.target as ServiceWorker;
        console.log('Service Worker state:', sw.state);
        if (sw.state === 'activated') {
          console.log('✅ Service Worker activado');
        }
      });
    } else if (registration.waiting) {
      console.log('⏸️ Service Worker esperando para activarse');
    }

    return registration;
  } catch (error) {
    console.error('❌ Error registrando Service Worker:', error);
    if (error instanceof Error) {
      console.error('Mensaje de error:', error.message);
      console.error('Stack:', error.stack);
    }
    return null;
  }
}

// Auto-registro si no se ha registrado ya
if (typeof window !== 'undefined') {
  window.addEventListener('load', () => {
    registerCustomServiceWorker();
  });
}
