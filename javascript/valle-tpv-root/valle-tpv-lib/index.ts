import { Plugin, App } from 'vue';

// Plugin principal
declare const ValleTpvLib: Plugin & {
  install: (app: App) => void;
};

export default ValleTpvLib;
// Stores
export { useEmpresasStore } from './src/store/dbStore/empresasStore';
export { useCamarerosStore } from './src/store/dbStore/camarerosStore';
export { useMesasStore } from './src/store/dbStore/mesasStore';
export { useZonasStore } from './src/store/dbStore/zonasStore';
export { useSeccionesStore } from './src/store/dbStore/seccionesStore';
export { useTeclasStore } from './src/store/dbStore/teclasStore';
export { useSugerenciasStore } from './src/store/dbStore/sugerenciasStore';
export { useConnectionStore } from './src/store/connectionStore';
export { useCuentaStore } from './src/store/dbStore/cuentasStore';
export { useInstruccionesStore } from './src/store/instruccionesStore';
export { useStorePlaning } from './src/store/storePlaning';
export { useCantidadStore } from './src/store/cantidadStore';
export { useCashKeeperStore } from './src/store/cashKeeperStore';
export { usePinPadStore } from './src/store/pinPadStore';


// Utils
export { default as WebSocketHandler } from './src/utils/WebSocketHandler';
export { default as InstructionQueue } from './src/utils/InstructionQueue';

export { default as Empresa } from './src/models/empresa';
export { default as Camarero } from './src/models/camarero';
export { default as Seccion } from './src/models/seccion';
export { default as Tecla } from './src/models/tecla';
export { default as Sugerencia } from './src/models/sugerencia';
export { default as Cuenta } from './src/models/cuenta';
export { CuentaItem } from './src/models/cuenta';
export { InfoCobro } from './src/models/cuenta';
export { Mesa, Zona, MesaEstado,  MesasAccion } from './src/models/mesaZona';
export { useWebSocket } from './src/composables/useWebSocket';

