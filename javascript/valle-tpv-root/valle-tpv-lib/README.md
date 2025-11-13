# Valle TPV Library

Una librería de componentes, stores y utilidades reutilizables para ValleTPV.

## Instalación

```bash
npm install valle-tpv-lib
```

## Uso

### Como Plugin de Vue

```javascript
import { createApp } from 'vue';
import ValleTpvLib from 'valle-tpv-lib';
import App from './App.vue';

const app = createApp(App);
app.use(ValleTpvLib);
app.mount('#app');
```

### Importaciones Individuales

```javascript
import { 
  Camarero, 
  Empresa, 
  camarerosStore, 
  empresasStore,
  connectionStore,
  DB,
  WebSocketHandler 
} from 'valle-tpv-lib';
```

## Componentes

### ExampleButton
Componente de botón reutilizable basado en Vuetify.

```vue
<template>
  <ExampleButton color="primary" @click="handleClick">
    Mi Botón
  </ExampleButton>
</template>
```

## Modelos

### Camarero
Modelo para gestionar datos de camareros.

```javascript
import { Camarero } from 'valle-tpv-lib';

const camarero = new Camarero({
  nombre: 'Juan',
  apellidos: 'Pérez',
  activo: true,
  permisos: ['ventas', 'admin']
});
```

### Empresa
Modelo para gestionar datos de empresas.

```javascript
import { Empresa } from 'valle-tpv-lib';

const empresa = new Empresa({
  uid: 'empresa-123',
  descripcion: 'Mi Empresa',
  url_servidor: 'https://api.example.com'
});
```

## Stores

### camarerosStore
Store reactivo para gestionar camareros.

```javascript
import { camarerosStore } from 'valle-tpv-lib';

// Crear nuevo camarero
await camarerosStore.altaCamarero('Juan', 'Pérez');

// Obtener camareros autorizados
const autorizados = camarerosStore.getCamarerosAutorizados();

// Obtener camareros con permisos específicos
const conPermisos = camarerosStore.getConPermisos('admin');
```

### empresasStore
Store reactivo para gestionar empresas.

```javascript
import { empresasStore } from 'valle-tpv-lib';

// Obtener empresa activa
const activa = empresasStore.getActiva();

// Establecer empresa activa
empresasStore.setActiva(123);
```

### connectionStore
Store reactivo para gestionar el estado de conexión.

```javascript
import { connectionStore } from 'valle-tpv-lib';

// Verificar estado de conexión
console.log(connectionStore.state.isConnected);

// Establecer estado de conexión
connectionStore.setConnected(true);

// Establecer error
connectionStore.setError('Error de conexión');
```

## Base de Datos

### DB
Interfaz para interactuar con IndexedDB usando Dexie.

```javascript
import { DB } from 'valle-tpv-lib';

// Agregar registro
await DB.add('camareros', { nombre: 'Juan', apellidos: 'Pérez' });

// Obtener todos los registros
const camareros = await DB.getAll('camareros');

// Actualizar registro
await DB.update('camareros', 1, { activo: false });

// Eliminar registro
await DB.remove('camareros', 1);

// Sincronizar con servidor
await DB.syncWithServer('camareros', 'device-uid');
```

## Utilidades

### WebSocketHandler
Manejador de conexiones WebSocket con reconexión automática.

```javascript
import { WebSocketHandler } from 'valle-tpv-lib';

const wsHandler = new WebSocketHandler(
  'ws://example.com',
  '/ws/updates/',
  'device-123'
);

// La conexión se establece automáticamente
// Incluye manejo de reconexión automática cada 5 segundos
```

## Desarrollo

### Scripts disponibles

```bash
# Compilar la librería
npm run build

# Verificar tipos
npm run type-check

# Lint del código
npm run lint

# Servidor de desarrollo
npm run serve
```

### Estructura del proyecto

```
src/
├── components/          # Componentes Vue
├── models/             # Modelos de datos
├── store/              # Stores reactivos
├── db/                 # Interfaz de base de datos
├── utils/              # Utilidades
└── index.ts           # Punto de entrada principal
```

## TypeScript

La librería está completamente tipada con TypeScript. Todos los tipos están exportados y disponibles para uso:

```typescript
import { CamareroParams, EmpresaParams, BaseItem } from 'valle-tpv-lib';
```
