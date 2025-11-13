<template>
  <v-dialog 
    v-model="isOpen" 
    persistent 
    max-width="700px"
    class="arqueo-cashkeeper-dialog"
  >
    <v-card class="dialog-card elevation-12 rounded-xl">
      <!-- Título -->
      <v-card-title class="dialog-title bg-primary pa-6">
        <div class="title-container">
          <v-icon size="48" class="mr-4">mdi-cash-register</v-icon>
          <span class="dialog-title-text">Arqueo de Caja con CashKeeper</span>
        </div>
      </v-card-title>

      <v-card-text class="dialog-content pa-8">
        <!-- Mensaje informativo grande -->
        <div class="mensaje-info">
          <v-icon size="80" color="info" class="mb-4">mdi-information</v-icon>
          <p class="mensaje-texto">{{ mensajeEstado }}</p>
        </div>

        <!-- Indicador de carga -->
        <div v-if="isProcessing" class="loading-container">
          <v-progress-circular
            indeterminate
            size="64"
            width="6"
            color="primary"
          />
          <p class="loading-text mt-4">Procesando arqueo...</p>
        </div>

        <!-- Información de error -->
        <v-alert
          v-if="hasError"
          type="error"
          prominent
          variant="tonal"
          class="mt-4"
        >
          <div class="alert-title">Error</div>
          <div>{{ errorMessage }}</div>
        </v-alert>
      </v-card-text>

      <v-card-actions class="dialog-actions pa-6">
        <v-row class="action-buttons-row" no-gutters>
          <v-col cols="6" class="pr-2">
            <v-btn
              size="x-large"
              color="error"
              variant="elevated"
              block
              class="action-button cancel-button"
              @click="cancelar"
              :disabled="isProcessing"
            >
              <v-icon size="48">mdi-close-circle</v-icon>
              <span class="button-text">Cancelar</span>
            </v-btn>
          </v-col>
          <v-col cols="6" class="pl-2">
            <v-btn
              size="x-large"
              color="success"
              variant="elevated"
              block
              class="action-button arquear-button"
              @click="arquearCaja"
              :disabled="!puedeArquear || isProcessing"
              :loading="isProcessing"
            >
              <v-icon size="48">mdi-check-circle</v-icon>
              <span class="button-text">Arquear Caja</span>
            </v-btn>
          </v-col>
        </v-row>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useCashKeeperStore } from '../../store/cashKeeperStore';
import { useEmpresasStore } from '../../store/dbStore/empresasStore';

interface Props {
  modelValue: boolean;
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void;
  (e: 'arqueo-completado'): void;
  (e: 'arqueo-cancelado'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const cashKeeperStore = useCashKeeperStore();
const empresasStore = useEmpresasStore();

const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});

const mensajeEstado = ref('Preparando arqueo de caja. Por favor, espere...');
const isProcessing = ref(false);
const hasError = ref(false);
const errorMessage = ref('');
const puedeArquear = ref(false);

// Datos del arqueo desde el servidor
const cambio = ref(0);
const cambioReal = ref(0);
const stacke = ref(0);
const hayArqueo = ref(false);

// Datos de las denominaciones del CashKeeper
const denominacionesDisponibles = ref<Record<number, number>>({});
const totalRecicladores = ref(0);
const totalAlmacenes = ref(0);

// Inicializar cuando se abre el diálogo
watch(() => props.modelValue, async (newValue) => {
  if (newValue) {
    await inicializar();
  }
});

onMounted(async () => {
  if (props.modelValue) {
    await inicializar();
  }
});

async function inicializar() {
  
  // RESETEAR TODAS LAS VARIABLES
  hasError.value = false;
  errorMessage.value = '';
  puedeArquear.value = false;
  cambio.value = 0;
  cambioReal.value = 0;
  stacke.value = 0;
  hayArqueo.value = false;
  denominacionesDisponibles.value = {};
  totalRecicladores.value = 0;
  totalAlmacenes.value = 0;
  
  mensajeEstado.value = 'Conectando con CashKeeper y obteniendo datos del servidor...';

  try {
    // FASE 1: Conectar al CashKeeper si no está conectado
    if (!cashKeeperStore.isConnected) {
      await cashKeeperStore.conectar();
    } else {
      console.warn('✅ [FASE 1] CashKeeper ya estaba conectado');
    }

    // FASE 2: Cargar denominaciones y obtener datos del servidor en paralelo
    mensajeEstado.value = 'Cargando denominaciones del CashKeeper y datos del servidor...';
    
    const [_, datosArqueo] = await Promise.all([
      cargarDenominacionesCashKeeper(),
      obtenerDatosArqueo()
    ]);
    
    
    if (!datosArqueo.hay_arqueo) {
      console.warn('⚠️ [FASE 3] No hay tickets nuevos para arquear');
      mensajeEstado.value = 'No se puede realizar el arqueo porque no hay tickets nuevos. Si continúas, modificarás el arqueo existente.';
      puedeArquear.value = false;
      hasError.value = true;
      errorMessage.value = 'No hay tickets nuevos para arquear';
      return;
    }
    
    
    // FASE 4: Guardar datos del arqueo
    cambio.value = datosArqueo.cambio;
    cambioReal.value = datosArqueo.cambio_real;
    stacke.value = datosArqueo.stacke;
    hayArqueo.value = datosArqueo.hay_arqueo;
    
    procesarDenominaciones();
    
    
    // FASE 6: Verificar si hay suficiente cambio después de mover el exceso
    const verificacion = verificarCambioSuficienteTrasMover();
    
    
    if (!verificacion.suficiente) {
      mensajeEstado.value = verificacion.mensaje;
      puedeArquear.value = false;
      hasError.value = true;
      errorMessage.value = 'No hay suficiente cambio en los recicladores';
      return;
    }
    
    
    // FASE 7: Todo listo para arquear
    mensajeEstado.value = 'La contabilidad está lista para cerrar caja. Puede pulsar el botón de "Arquear Caja".';
    puedeArquear.value = true;
    
    
  } catch (error: any) {
    hasError.value = true;
    errorMessage.value = error.message || 'Error al inicializar el arqueo';
    mensajeEstado.value = 'Error al preparar el arqueo';
    puedeArquear.value = false;
  }
}

async function cargarDenominacionesCashKeeper() {
  
  return new Promise<void>((resolve, reject) => {
    // IMPORTANTE: Resetear el estado del store antes de cargar para FORZAR que se recargue
    // Guardamos el estado inicial con las cantidades en 0 para detectar cuando lleguen datos nuevos
    
    // Marcar todas las cantidades como 0 para forzar detección de cambios
    Object.keys(cashKeeperStore.denominacionesRecicladores).forEach(key => {
      cashKeeperStore.denominacionesRecicladores[key].cantidad = 0;
    });
    Object.keys(cashKeeperStore.denominacionesStacker).forEach(key => {
      cashKeeperStore.denominacionesStacker[key].cantidad = 0;
    });
    
    // Ahora enviar el comando para cargar las denominaciones
    cashKeeperStore.cargarDenominaciones();
    
    let attempts = 0;
    const maxAttempts = 100; // 10 segundos (100 * 100ms)
    
    // Esperar hasta que las denominaciones tengan datos (cantidad > 0)
    const checkInterval = setInterval(() => {
      attempts++;
      
      // Verificar si hay datos reales (cantidad > 0)
      const tieneDatos = Object.values(cashKeeperStore.denominacionesRecicladores).some(d => d.cantidad > 0) ||
                        Object.values(cashKeeperStore.denominacionesStacker).some(d => d.cantidad > 0);
      
      if (tieneDatos) {
        clearInterval(checkInterval);
        resolve();
        return;
      }
      
      
      if (attempts >= maxAttempts) {
        clearInterval(checkInterval);
        reject(new Error('Timeout al cargar denominaciones del CashKeeper'));
      }
    }, 100);
  });
}

async function obtenerDatosArqueo() {
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva || !empresaActiva.url_servidor) {
    throw new Error('No se pudo obtener la URL del servidor');
  }

  const formData = new FormData();
  formData.append('uid', empresaActiva.uid);

 
  try {
    const response = await fetch(`${empresaActiva.url_servidor}/api/arqueo/getcambio`, {
      method: 'POST',
      body: formData,
    });

 
    if (!response.ok) {
      throw new Error(`Error HTTP ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    
    // Validar que tenemos los datos necesarios
    if (data.cambio === undefined && data.cambio_real === undefined && data.stacke === undefined) {
      throw new Error('El servidor no devolvió datos válidos de arqueo');
    }
    
    const parsedData = {
      cambio: parseFloat(data.cambio) || 0,
      cambio_real: parseFloat(data.cambio_real) || 0,
      stacke: parseFloat(data.stacke) || 0,
      hay_arqueo: data.hay_arqueo !== undefined ? data.hay_arqueo : false
    };
    
    
    return parsedData;
  } catch (error: any) {
    console.error('❌ [obtenerDatosArqueo] Error:', error);
    throw new Error(`Error al obtener datos del arqueo: ${error.message}`);
  }
}

function procesarDenominaciones() {
  
  denominacionesDisponibles.value = {};
  totalRecicladores.value = 0;
  totalAlmacenes.value = 0;

  Object.entries(cashKeeperStore.denominacionesRecicladores).forEach(([key, denom]) => {
    const valorCentimos = denom.toCentimos;
    const cantidad = denom.cantidad;
    
    if (cantidad > 0) {
      denominacionesDisponibles.value[valorCentimos] = cantidad;
      totalRecicladores.value += (valorCentimos * cantidad) / 100;
    }
  });

  // Procesar stacker - TODOS los billetes
  Object.entries(cashKeeperStore.denominacionesStacker).forEach(([key, denom]) => {
    const valorCentimos = denom.toCentimos;
    const cantidad = denom.cantidad;
    
    // Contar TODOS los billetes del stacker
    if (cantidad > 0) {
      totalAlmacenes.value += (valorCentimos * cantidad) / 100;
    }
  });

  // Redondear a 2 decimales
  totalRecicladores.value = Math.round(totalRecicladores.value * 100) / 100;
  totalAlmacenes.value = Math.round(totalAlmacenes.value * 100) / 100;
  
 
}

/**
 * Verifica si después de mover el exceso de dinero al stacker,
 * quedará suficiente cambio en los recicladores.
 * Fórmula: ((recicladores - 20*x) - 10*y) - 5*z) >= cambio
 */
function verificarCambioSuficienteTrasMover(): { 
  suficiente: boolean; 
  mensaje: string;
  recicladorFinal: number;
} {
  const exceso = totalRecicladores.value - cambio.value;
  
  if (exceso <= 0) {
    // No hay exceso, todo bien
    return {
      suficiente: true,
      mensaje: '',
      recicladorFinal: totalRecicladores.value
    };
  }

  // Copiar cantidades de billetes de 20, 10 y 5 euros
  const billetes20 = denominacionesDisponibles.value[2000] || 0;
  const billetes10 = denominacionesDisponibles.value[1000] || 0;
  const billetes5 = denominacionesDisponibles.value[500] || 0;

  let recicladorSimulado = totalRecicladores.value;
  let excesoRestante = exceso;
  
  let billetes20AMover = 0;
  let billetes10AMover = 0;
  let billetes5AMover = 0;

  // Intentar mover billetes de 20€ primero
  if (excesoRestante >= 20 && billetes20 > 0) {
    billetes20AMover = Math.min(Math.floor(excesoRestante / 20), billetes20);
    excesoRestante -= billetes20AMover * 20;
    recicladorSimulado -= billetes20AMover * 20;
  }

  // Intentar mover billetes de 10€
  if (excesoRestante >= 10 && billetes10 > 0) {
    billetes10AMover = Math.min(Math.floor(excesoRestante / 10), billetes10);
    excesoRestante -= billetes10AMover * 10;
    recicladorSimulado -= billetes10AMover * 10;
  }

  // Intentar mover billetes de 5€
  if (excesoRestante >= 5 && billetes5 > 0) {
    billetes5AMover = Math.min(Math.floor(excesoRestante / 5), billetes5);
    excesoRestante -= billetes5AMover * 5;
    recicladorSimulado -= billetes5AMover * 5;
  }

  // Verificar: recicladorSimulado >= cambio
  const suficiente = recicladorSimulado >= cambio.value;

  

  if (!suficiente) {
    return {
      suficiente: false,
      mensaje: `No hay suficiente cambio. Después de mover el exceso, quedarían ${recicladorSimulado.toFixed(2)}€ en recicladores, pero se necesitan ${cambio.value.toFixed(2)}€ para el cambio.`,
      recicladorFinal: recicladorSimulado
    };
  }

  return {
    suficiente: true,
    mensaje: '',
    recicladorFinal: recicladorSimulado
  };
}

async function arquearCaja() {
  // Protección: evitar ejecutar el arqueo si ya está en proceso
  if (isProcessing.value) {
    console.warn('⚠️ [arquearCaja] Arqueo ya en proceso');
    return;
  }
  
  isProcessing.value = true;
  hasError.value = false;
  mensajeEstado.value = 'Procesando arqueo de caja...';

  try {
       // Guardar valores originales ANTES de cualquier modificación
    const totalRecicladores_original = totalRecicladores.value;
    const totalAlmacenes_original = totalAlmacenes.value;
    
    
    // FÓRMULA CORRECTA DEL EFECTIVO VENDIDO:
    // efectivo = (recicladores + stacker) - (cambio_real + ultimo_stacker)
    // 
    // Donde:
    // - (recicladores + stacker) = todo el dinero en CashKeeper AHORA
    // - (cambio_real + ultimo_stacker) = dinero que había en el arqueo ANTERIOR
    // - efectivo = dinero NUEVO que ha entrado (ventas)
    
    const totalDineroAhora = totalRecicladores_original + totalAlmacenes_original;
    const totalDineroAnterior = cambioReal.value + stacke.value;
    const efectivoVendido = totalDineroAhora - totalDineroAnterior;
 
    
    // Con CashKeeper, no hace falta enviar el desglose de efectivo
    // El servidor ya no lo utiliza
    const desEfectivo: Array<any> = [];

    // Calcular efectivo total presente en la máquina (recicladores + almacenes)
    const efectivoEnMaquina = Math.round((totalRecicladores_original + totalAlmacenes_original) * 100) / 100;


    mensajeEstado.value = 'Enviando arqueo al servidor...';
    
    const resultadoArqueo = await enviarArqueoAlServidor(efectivoVendido, desEfectivo, efectivoEnMaquina);
    
    if (!resultadoArqueo.success) {
      console.error('❌ [PASO 3] El servidor rechazó el arqueo');
      throw new Error('El servidor no pudo procesar el arqueo. Es posible que no haya tickets nuevos o el arqueo esté deshabilitado.');
    }
    

    // PASO 4: Mover exceso de recicladores al stacker (si hay)
    const excesoRecicladores = totalRecicladores_original - cambio.value;
    
    if (excesoRecicladores > 0) {
      console.log(`💰 [PASO 4] Moviendo exceso de ${excesoRecicladores.toFixed(2)}€ al stacker...`);
      mensajeEstado.value = 'Moviendo dinero sobrante al stacker (esto puede tardar varios minutos)...';
      
      // Enviar comando #U# de forma asíncrona sin esperar
      cerrarCashKeeper();
      
      // IMPORTANTE: NO esperamos la respuesta del comando #U# porque puede tardar mucho tiempo
      // El CashKeeper moverá el dinero en segundo plano
      // Actualizamos los valores ESTIMADOS basándonos en lo que enviamos
      
      console.log('✅ [PASO 4] Comando de mover dinero enviado al CashKeeper');
      console.log('⏳ [PASO 4] El CashKeeper moverá el dinero en segundo plano (puede tardar varios minutos)');
      
      // Esperamos un tiempo prudencial para que el comando se procese (reducido de 60s a 5s)
      await new Promise(resolve => setTimeout(resolve, 5000));
      
    } else if (excesoRecicladores < 0) {
      console.warn(`⚠️ [PASO 4] ADVERTENCIA: Faltan ${Math.abs(excesoRecicladores).toFixed(2)}€ en recicladores!`);
      console.warn(`⚠️ [PASO 4] Se esperaban ${cambio.value.toFixed(2)}€ pero solo hay ${totalRecicladores_original.toFixed(2)}€`);
    } else {
      console.warn(`✅ [PASO 4] Recicladores tienen exactamente el cambio necesario: ${cambio.value.toFixed(2)}€`);
    }
    
    mensajeEstado.value = 'Actualizando estado del arqueo...';

    // PASO 5: Actualizar cambio en el servidor con los valores FINALES (después de mover)
    mensajeEstado.value = 'Actualizando cambio en el servidor...';
    await actualizarCambioEnServidor(totalRecicladores.value, totalAlmacenes.value);

    mensajeEstado.value = 'Cierre completado. Ya puedes salir. Gracias por su colaboración.';
    puedeArquear.value = false;
    
    
    // Emitir evento de completado después de un breve delay
    setTimeout(() => {
      emit('arqueo-completado');
      isOpen.value = false;
    }, 2000);

  } catch (error: any) {
    console.error('❌ [ERROR] Error en el proceso de arqueo:', error);
    hasError.value = true;
    errorMessage.value = error.message || 'Error al realizar el arqueo';
    mensajeEstado.value = 'Error al procesar el arqueo. Puede volver a intentarlo o cerrar el diálogo.';
  } finally {
    isProcessing.value = false;
  }
}

async function enviarArqueoAlServidor(efectivoVendido: number, desEfectivo: any[], efectivoEnMaquina?: number): Promise<{ success: boolean }> {
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva || !empresaActiva.url_servidor) {
    throw new Error('No se pudo obtener la URL del servidor');
  }

  const formData = new FormData();
  formData.append('uid', empresaActiva.uid);
  formData.append('cambio', cambio.value.toFixed(2));
  
  // IMPORTANTE: El servidor espera 'efectivo' = efectivo VENDIDO (dinero nuevo)
  // efectivo_vendido = (recicladores + stacker) - (cambioReal_anterior + stacker_anterior)
  formData.append('efectivo', (Math.round(efectivoVendido * 100) / 100).toFixed(2));
  formData.append('gastos', '0.00');
  formData.append('des_efectivo', JSON.stringify(desEfectivo));
  formData.append('usaCashlogy', 'true');
  formData.append('des_gastos', '[]');


  const response = await fetch(`${empresaActiva.url_servidor}/api/arqueo/arquear`, {
    method: 'POST',
    body: formData,
  });


  if (!response.ok) {
    throw new Error(`Error HTTP al enviar el arqueo: ${response.status}`);
  }

  const data = await response.json();
  
  // Devolver el resultado del servidor
  return {
    success: data.success === true
  };
}

function cerrarCashKeeper() {
  // Si hay exceso de dinero en recicladores, moverlo al stacke
  if (totalRecicladores.value > cambio.value) {
    let excesoRestante = totalRecicladores.value - cambio.value;
    const comandoBuilder: string[] = [];
    let hasDenominations = false;
    
    // Variables para tracking de lo que se mueve
    let totalMovido = 0;

    // Obtener cantidades disponibles de billetes de 20, 10 y 5 euros
    const billetes20Disponibles = denominacionesDisponibles.value[2000] || 0;
    const billetes10Disponibles = denominacionesDisponibles.value[1000] || 0;
    const billetes5Disponibles = denominacionesDisponibles.value[500] || 0;

   

    // Mover billetes de 20€ primero
    if (excesoRestante >= 20 && billetes20Disponibles > 0) {
      const billetes20AMover = Math.min(Math.floor(excesoRestante / 20), billetes20Disponibles);
      if (billetes20AMover > 0) {
        comandoBuilder.push(`2000:${billetes20AMover}`);
        const cantidadMovida = billetes20AMover * 20;
        excesoRestante -= cantidadMovida;
        totalMovido += cantidadMovida;
        hasDenominations = true;
      }
    }

    // Mover billetes de 10€
    if (excesoRestante >= 10 && billetes10Disponibles > 0) {
      const billetes10AMover = Math.min(Math.floor(excesoRestante / 10), billetes10Disponibles);
      if (billetes10AMover > 0) {
        comandoBuilder.push(`1000:${billetes10AMover}`);
        const cantidadMovida = billetes10AMover * 10;
        excesoRestante -= cantidadMovida;
        totalMovido += cantidadMovida;
        hasDenominations = true;
      }
    }

    // Mover billetes de 5€
    if (excesoRestante >= 5 && billetes5Disponibles > 0) {
      const billetes5AMover = Math.min(Math.floor(excesoRestante / 5), billetes5Disponibles);
      if (billetes5AMover > 0) {
        comandoBuilder.push(`500:${billetes5AMover}`);
        const cantidadMovida = billetes5AMover * 5;
        excesoRestante -= cantidadMovida;
        totalMovido += cantidadMovida;
        hasDenominations = true;
      }
    }

    if (hasDenominations) {
      const comando = `#U#;${comandoBuilder.join(',')}#1#0#0#`;
      
      // Enviar comando sin esperar respuesta
      cashKeeperStore.enviarComando(comando);
      
      // Actualizar los valores locales después de enviar el comando
      totalAlmacenes.value += totalMovido;
      totalRecicladores.value -= totalMovido;
      
    } else {
      console.warn('⚠️ No hay denominaciones suficientes para mover el exceso');
    }
  } else {
    console.warn('⚠️ No hay exceso de dinero que mover');
  }
}

async function actualizarCambioEnServidor(recicladorFinal: number, stackerFinal: number) {
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva || !empresaActiva.url_servidor) {
    throw new Error('No se pudo obtener la URL del servidor');
  }

  const formData = new FormData();
  formData.append('uid', empresaActiva.uid);
  formData.append('cambio', cambio.value.toFixed(2));
  // IMPORTANTE: Enviar valores FINALES (después de mover dinero al stacker)
  formData.append('stacke', stackerFinal.toFixed(2));
  formData.append('cambio_real', recicladorFinal.toFixed(2));


  const response = await fetch(`${empresaActiva.url_servidor}/api/arqueo/setcambio`, {
    method: 'POST',
    body: formData,
  });

  
  if (!response.ok) {
    throw new Error('Error al actualizar el cambio en el servidor');
  }

  const data = await response.json();
  
  if (data.status !== 'success' && !data.success) {
    throw new Error('Error al actualizar el efectivo en el servidor');
  }
}

function cancelar() {
  emit('arqueo-cancelado');
  isOpen.value = false;
}
</script>

<style scoped>
.arqueo-cashkeeper-dialog {
  z-index: 9999;
}

.dialog-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.dialog-title {
  color: white !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border-radius: 16px 16px 0 0;
  position: relative;
  overflow: hidden;
}

.dialog-title::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255,255,255,0.1) 0%, transparent 100%);
}

.title-container {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-title-text {
  font-size: 2rem;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.dialog-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.mensaje-info {
  text-align: center;
  width: 100%;
}

.mensaje-texto {
  font-size: 1.8rem;
  font-weight: 500;
  color: #333;
  line-height: 1.6;
  margin: 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 32px;
}

.loading-text {
  font-size: 1.4rem;
  font-weight: 500;
  color: #666;
}

.alert-title {
  font-size: 1.6rem;
  font-weight: 700;
  margin-bottom: 8px;
}

.dialog-actions {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);
  border-radius: 0 0 16px 16px;
}

.action-buttons-row {
  gap: 0;
}

.action-button {
  height: 120px !important;
  border-radius: 16px !important;
  font-size: 1.6rem !important;
  font-weight: 700 !important;
  text-transform: uppercase !important;
  letter-spacing: 1.5px !important;
  box-shadow: 0 8px 20px rgba(0,0,0,0.25) !important;
  transition: all 0.3s ease !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 12px !important;
  padding: 24px !important;
}

.action-button:not(:disabled):hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 32px rgba(0,0,0,0.35) !important;
}

.action-button:not(:disabled):active {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.3) !important;
}

.action-button:disabled {
  opacity: 0.4 !important;
  cursor: not-allowed !important;
}

.cancel-button {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%) !important;
}

.cancel-button:not(:disabled):hover {
  background: linear-gradient(135deg, #ff5252 0%, #d84315 100%) !important;
}

.arquear-button {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%) !important;
}

.arquear-button:not(:disabled):hover {
  background: linear-gradient(135deg, #1db954 0%, #1e8449 100%) !important;
}

.button-text {
  font-size: 1.3rem;
  line-height: 1.2;
}
</style>
