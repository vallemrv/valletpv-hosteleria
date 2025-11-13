/// <reference lib="webworker" />
declare const self: ServiceWorkerGlobalScope;


// Definiciones de tipos para que TypeScript esté contento DENTRO de este fichero.
interface SyncManager {
  register(tag: string): Promise<void>;
}

// Extiende la interfaz global para que TypeScript reconozca 'sync'
declare global {
  interface ServiceWorkerRegistration {
    readonly sync: SyncManager;
  }
}

import InstructionQueue from './utils/InstructionQueue';

const instructionQueue = new InstructionQueue();

// Inicializar el contador de instrucciones al cargar el Service Worker
instructionQueue.initializeCounter().catch(error => {
  console.error('Error inicializando contador de instrucciones:', error);
});

// Cuando llega un mensaje...
self.addEventListener('message', (event) => {
  const { url, endpoint, data } = event.data;
  
  // Usamos una función anónima para poder usar async/await
  const work = async () => {
    // 1. Llamamos a la cola para que haga su trabajo
    const needsSync = await instructionQueue.addAndProcess(url, endpoint, data);

    // 2. Si la cola nos "avisa" que necesita sincronizar...
    if (needsSync && self.registration.sync) {
      // 3. ...nosotros (el Service Worker) registramos la sincronización.
      await self.registration.sync.register('instrucciones-sync');
    }
  };
  
  event.waitUntil(work());
});

// El listener de 'sync' no cambia, ya estaba bien.
self.addEventListener('sync', (event: any) => { 
  if (event.tag === 'instrucciones-sync') {
    event.waitUntil(instructionQueue.processQueueFromSync());
  }
});


// El resto de listeners (install, activate) no cambian.
self.addEventListener('activate', (event) => {
  event.waitUntil(self.clients.claim());
});

self.addEventListener('install', (event) => {
  self.skipWaiting();
});

export {};