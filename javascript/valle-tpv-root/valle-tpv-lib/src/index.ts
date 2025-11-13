import type { App } from 'vue';
import { componentList } from './components';

const install = (app: App) => {
  Object.entries(componentList).forEach(([name, component]) => {
    app.component(name, component);
  });
};

export default { install };
export * from './components';

// Exportar utilidades, modelos y stores
export { default as Camarero, type CamareroParams } from './models/camarero';
export { default as Empresa, type EmpresaParams } from './models/empresa';
export { Mesa, type MesaParams, type MesaEstado, 
               type MesasAccion, Zona, 
               type ZonaParams } from './models/mesaZona';
export { default as Seccion, type SeccionParams } from './models/seccion';
export { default as Tecla, type TeclaParams } from './models/tecla';
export { default as Sugerencia, type SugerenciaParams } from './models/sugerencia';
export { default as Cuenta, type CuentaParams, type CuentaItem, type InfoCobro } from './models/cuenta';

export { useCamarerosStore } from './store/dbStore/camarerosStore';
export { useEmpresasStore } from './store/dbStore/empresasStore';
export { useMesasStore } from './store/dbStore/mesasStore';
export { useZonasStore } from './store/dbStore/zonasStore';
export { useSeccionesStore } from './store/dbStore/seccionesStore';
export { useTeclasStore } from './store/dbStore/teclasStore';
export { useSugerenciasStore } from './store/dbStore/sugerenciasStore';
export { useConnectionStore } from './store/connectionStore';
export { useCuentaStore } from './store/dbStore/cuentasStore';
export { useInstruccionesStore } from './store/instruccionesStore';
export { useCantidadStore } from './store/cantidadStore';
export { useCashKeeperStore, initCashKeeperWatcher } from './store/cashKeeperStore';

export { usePinPadStore } from './store/pinPadStore';

export { default as WebSocketHandler } from './utils/WebSocketHandler';
export { default as InstructionQueue } from './utils/InstructionQueue';
export { default as useWebSocket } from './composables/useWebSocket';
export { useTouchScroll } from './composables/useTouchScroll';
export * from './themes/vuetifyThemes';
