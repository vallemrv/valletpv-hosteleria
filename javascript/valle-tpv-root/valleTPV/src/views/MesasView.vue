<template>
  <UiMainWindows :title="tituloVentana" icon="mdi-table-chair">
    <template #actions>
      <UiActionButton v-if="!mesaAccion" icon="mdi-receipt" @click="mostrarTickets = true">Tickets</UiActionButton>
      <UiActionButton v-if="!mesaAccion" icon="mdi-cash-register" @click="abrirCajon">{{ textoCajon }}</UiActionButton>
      <UiActionButton 
        v-if="!mesaAccion" 
        icon="mdi-arrow-left" 
        @click="volverAlTPV"
      >
        Volver al TPV
      </UiActionButton>
      <UiActionButton 
        v-else 
        icon="mdi-close" 
        @click="cancelarAccion"
      >
        Cancelar
      </UiActionButton>
     
    </template>

    <v-container fluid class="content-wrapper">
      <v-row no-gutters class="fill-height">
        <!-- Grid de mesas 90% a la izquierda -->
        <v-col cols="auto" class="mesas-grid-container">
          <div class="mesas-grid">
            <div 
              v-for="mesa in mesasZonaActual" 
              :key="mesa.ID" 
              class="mesa-slot"
            >
              <UiMesaButton
                :nombre="mesa.Nombre || ''"
                :id="mesa.ID"
                :estado="getEstadoMesa(mesa)"
                :mesaAccion="mesaAccion"
                :icon-url="getIconUrlMesa()"
                @mesa-click="onMesaClick(mesa)"
                @menu-select="onMesaMenuSelect(mesa, $event)"
              />
            </div>
          </div>
        </v-col>

        <!-- Lista de zonas 10% a la derecha -->
        <v-col cols="auto" class="zonas-container">
          <div class="zonas-list">
            <UiBotonAsociable
              v-for="zona in zonas"
              :key="zona.id"
              :texto="zona.nombre"
              icon="mdi-map-marker"
              :image-url="zona.icon"
              :selected="zonaSeleccionada?.id === zona.id"
              :asociado="zonasStore.zonaAsociada?.id === zona.id"
              :show-checkbox="false"
              @click="seleccionarZona(zona)"
              @toggle-asociacion="toggleAsociarZona(zona, $event)"
            />
          </div>
        </v-col>
      </v-row>
    </v-container>

    <UiBorrarDialog
      v-model="dialogVisible"
      :item="null"
      :nombre_mesa="nombre_mesa"
      @anulacionConfirmada="handleAnulacionConfirmada"
    >
      <template #extra-actions>
        <v-btn 
          variant="tonal"
          color="grey-darken-1" 
          @click="cancelarAccion"
          block
          class="cancelar-accion-btn"
        >
          Cancelar acción
        </v-btn>
      </template>
    </UiBorrarDialog>

    <UiTicketsDialog 
      v-model="mostrarTickets" 
    />

    <!-- Diálogo de CashKeeper para cambio -->
    <UiCambioCashKeeperDialog
      v-model="mostrarCambioCashKeeper"
    />

    <UiAutorizacionDialog
      v-model="mostrarAutorizacion"
      :camareros="camarerosConPermiso"
      :parametros="parametrosAutorizacion"
      @autorizacionSolicitada="onAutorizacionSolicitada"
      @autorizacionCancelada="onAutorizacionCancelada"
    />

    <!-- Snackbar para mensajes de autorización -->
    <UiSnackbar
      v-model="mostrarSnackbar"
      :message="mensajeSnackbar"
      type="success"
      :timeout="4000"
      @close="mostrarSnackbar = false"
    />
    
    <!-- Snackbar genérico para notificaciones -->
    <UiSnackbar 
      v-model="snackbar.show" 
      :message="snackbar.text"
      :type="snackbar.type"
      :timeout="4000"
    />

  </UiMainWindows>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useZonasStore, useMesasStore, useCuentaStore, useCamarerosStore, useEmpresasStore, Mesa, Zona, type MesasAccion } from 'valle-tpv-lib';
import { UiCambioCashKeeperDialog } from 'valle-tpv-lib';


const router = useRouter();
const mesasStore = useMesasStore();
const zonasStore = useZonasStore();
const cuentaStore = useCuentaStore();
const camarerosStore = useCamarerosStore();
const empresasStore = useEmpresasStore();

const dialogVisible = ref(false);
const mesaAccion = ref<MesasAccion | null>(null);
const mesaSeleccionada = ref<Mesa | null>(null);
const nombre_mesa = computed(() => mesaSeleccionada.value?.Nombre || null);

const mostrarTickets = ref(false);
const mostrarAutorizacion = ref(false);
const parametrosAutorizacion = ref<Record<string, any>>({});
const mostrarCambioCashKeeper = ref(false);

// Variables para el snackbar de autorización
const mostrarSnackbar = ref(false);
const mensajeSnackbar = ref('');

// Snackbar genérico para notificaciones
const snackbar = ref({ show: false, text: '', type: 'info' as 'info' | 'success' | 'warning' | 'error' });
function showSnackbar(text: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') {
  snackbar.value.text = text;
  snackbar.value.type = type;
  snackbar.value.show = true;
}

// Computed para el título dinámico
const tituloVentana = computed(() => {
  
  if (mesaAccion !== null && mesaAccion.value?.tipo && mesaAccion.value.mesa) {
    const accion = mesaAccion.value.tipo === 'juntar' ? 'Juntando' : 
                   mesaAccion.value.tipo === 'mover' ? 'Moviendo' : 'Borrando';
    return `${accion} mesa ${mesaAccion.value.mesa.Nombre}`;
  }

  return 'Mesas';
});

// Computed para el texto del botón de cajón/cambio
const textoCajon = computed(() => {
  const empresaActiva = empresasStore.empresaActiva;
  return empresaActiva?.usa_cash_keeper ? 'Cambio' : 'Abrir cajón';
});

const camarerosConPermiso = computed(() => {
  return camarerosStore.camarerosPorPermiso('borrar_mesa');
});

const zonas = computed(() => zonasStore.items);
const zonaSeleccionada = computed(() =>  zonasStore.zonaSeleccionada);

// Computed para obtener las mesas de la zona seleccionada
const mesasZonaActual = computed(() => mesasStore.mesasPorZona(zonaSeleccionada.value));

async function handleAnulacionConfirmada(motivo: string) {
  
  if (mesaSeleccionada.value) {
    // Verificar si hay camareros con permiso de borrar_mesas usando el computed
    if (camarerosConPermiso.value.length > 0) {
      // Si hay camareros con permiso, solicitar autorización
      parametrosAutorizacion.value = {
        inst:{
          idm: mesaSeleccionada.value.ID,
          idc: camarerosStore.camarerosSel?.id || null,
          motivo: motivo,
        },
        mesa_nombre: mesaSeleccionada.value.Nombre,
        accion: 'borrar_mesa'
      };
      mostrarAutorizacion.value = true;
      dialogVisible.value = false; // Cerrar el diálogo de borrar temporalmente
    } else {
      // Si no hay camareros con permiso, proceder con el borrado normal
      // Obtener todas las líneas de la mesa
      const lineas = cuentaStore.lineasPorMesa(mesaSeleccionada.value.ID);
      
      // Eliminar las líneas con el motivo
      await cuentaStore.rmConMotivo(mesaSeleccionada.value.ID, motivo, lineas);
      
      // Cerrar la mesa
      await mesasStore.cerrarMesa(mesaSeleccionada.value.ID);
      showSnackbar('Mesa borrada correctamente', 'success');
      
      // Resetear la acción después de borrar
      cancelarAccion();
    }
  }
}

// Nueva función para cancelar cualquier acción pendiente
function cancelarAccion() {
  mesaAccion.value = null;
  mesaSeleccionada.value = null;
  dialogVisible.value = false;
  // Reiniciar temporizador al cancelar acción
  iniciarTemporizador();
}

// Función para manejar la autorización solicitada
async function onAutorizacionSolicitada() {
  // Mostrar snackbar de confirmación
  mensajeSnackbar.value = 'Se ha solicitado autorización para borrar la mesa';
  mostrarSnackbar.value = true;
  
  // Cerrar el diálogo de autorización
  mostrarAutorizacion.value = false;
  
  // Cancelar la acción
  cancelarAccion();
}

// Función para manejar la cancelación de autorización
function onAutorizacionCancelada() {
  // Solo cancelar la acción, sin mostrar snackbar
  cancelarAccion();
}

function toggleAsociarZona(zona: Zona, asociar: boolean) {
  if (asociar) {
    zonasStore.asociar(zona.id);
    showSnackbar(`Zona ${zona.nombre} asociada`, 'info');
  } else {
    zonasStore.desasociar();
    showSnackbar(`Zona ${zona.nombre} desasociada`, 'info');
  }
  zonasStore.setZona(zona.id);
  // Reiniciar temporizador al interactuar con zona
  iniciarTemporizador();
}

// Función para volver al TPV
function volverAlTPV() {
  limpiarTemporizador();
  router.replace("/"); // Volver a la vista principal
}

// Función principal que decide qué tipo de apertura usar
function abrirCajon() {
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva) {
    console.error('No hay empresa activa configurada');
    showSnackbar('No hay empresa activa configurada', 'error');
    return;
  }
  
  // Verificar si usa CashKeeper (manejo robusto de diferentes tipos)
  const usaCashKeeper = Boolean(empresaActiva.usa_cash_keeper);
  
  if (usaCashKeeper) {
    // Usar CashKeeper - mostrar diálogo visual
    abrirCajonCashKeeper();
  } else {
    // Usar cajón manual/impresora - enviar señal al servidor
    abrirCajonManual();
  }
}

// Función para iniciar cambio con CashKeeper (solo muestra diálogo)
function abrirCajonCashKeeper() {
  mostrarCambioCashKeeper.value = true;
}

// Función para abrir cajón manual/impresora (envía señal al servidor)
async function abrirCajonManual() {
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva || !empresaActiva.url_servidor) {
    console.error('No hay URL del servidor configurada para cajón manual');
    showSnackbar('No hay servidor configurado para el cajón', 'error');
    return;
  }
  
  const formData = new FormData();
  formData.append('uid', empresaActiva.uid);

  try {
    const response = await fetch(`${empresaActiva.url_servidor}/api/impresion/abrircajon`, {
      method: 'POST',
      body: formData
    });

    if (response.ok) {
      showSnackbar('Cajón abierto exitosamente', 'success');
    } else {
      console.error('Error al abrir cajón manual:', response.statusText);
      showSnackbar('Error al abrir el cajón', 'error');
    }
  } catch (error) {
    console.error('Error en la petición al servidor:', error);
    showSnackbar('Error de conexión al abrir el cajón', 'error');
  }
}

// Función para seleccionar una zona
function seleccionarZona(zona: Zona) {
  zonasStore.setZona(zona.id);
  // Reiniciar temporizador al seleccionar zona
  iniciarTemporizador();
}

// Función para determinar el estado de la mesa (simulado por ahora)
function getEstadoMesa(mesa: Mesa): 'libre' | 'ocupada' | 'impresa' {
  // Por ahora devolvemos un estado simulado
  // Podrías implementar la lógica real según tu modelo de datos
  if (mesa.num > 0){
    return 'impresa';
  }
  return mesa.abierta === 1 ? 'ocupada' : 'libre';
}

// Función para obtener la URL del icono de la mesa
function getIconUrlMesa(): string {
  return '/planing/mesa_redonda.svg';
}

// Función para manejar selección del menú de mesa
function onMesaMenuSelect(mesa: Mesa | null, action: string) {
  if (mesa) {
    switch (action) {
      case 'juntar':
        mesaAccion.value = { tipo: 'juntar', mesa };
        break;
      case 'mover':
        mesaAccion.value = { tipo: 'mover', mesa };
        break;
      case 'borrar':
        mesaAccion.value = { tipo: 'borrar', mesa };
        mesaSeleccionada.value = mesa;
        dialogVisible.value = true;
        break;
    }
  }
}

// Función para manejar click en mesa
function onMesaClick(mesa: Mesa | null) {
  if (mesa) {
    if (mesaAccion.value?.tipo) {
      // Si hay una acción pendiente, ejecutarla
      ejecutarAccionMesa(mesa);
    } else {
      // Comportamiento normal: ir a la cuenta
      mesasStore.setMesaSeleccionada(mesa);
      router.push('/cuenta');
    }
  }
}

// Nueva función para ejecutar acciones de mesa
async function ejecutarAccionMesa(mesaDestino: Mesa) {
  if (mesaAccion.value?.tipo && mesaAccion.value?.mesa) {
    switch (mesaAccion.value.tipo) {
      case 'juntar':
        const result = await cuentaStore.juntarMesas(mesaAccion.value.mesa.ID, mesaDestino.ID);
        if (result) {
          showSnackbar(`Mesas ${mesaAccion.value.mesa.Nombre} y ${mesaDestino.Nombre} juntadas correctamente`, 'success');
        } else {
          showSnackbar(`Error al juntar las mesas. Verifica que ambas tengan productos`, 'error');
        }
        break;
      case 'mover':
        const moveResult = await cuentaStore.cambiarMesa(mesaAccion.value.mesa.ID, mesaDestino.ID);
        if (moveResult) {
          showSnackbar(`Mesa ${mesaAccion.value.mesa.Nombre} movida a ${mesaDestino.Nombre} correctamente`, 'success');
        } else {
          showSnackbar(`Error al mover la mesa. Verifica que la mesa origen tenga productos`, 'error');
        }
        break;
    }
    cancelarAccion();
  }
}

// Variables para el temporizador de inactividad
const temporizadorInactividad = ref<ReturnType<typeof setTimeout> | null>(null);
const tiempoInactividad = 10000; // 10 segundos

// Función para iniciar/reiniciar el temporizador
function iniciarTemporizador() {
  limpiarTemporizador();
  
  // No iniciar temporizador si hay una acción de mesa activa, diálogo de tickets o diálogo de cambio CashKeeper abierto
  if (mesaAccion.value || mostrarTickets.value || mostrarCambioCashKeeper.value) {
    return;
  }
  
  temporizadorInactividad.value = setTimeout(() => {
    volverAlTPV();
  }, tiempoInactividad);
}

// Función para limpiar el temporizador
function limpiarTemporizador() {
  if (temporizadorInactividad.value) {
    clearTimeout(temporizadorInactividad.value);
    temporizadorInactividad.value = null;
  }
}

// Watch para pausar/reanudar temporizador según acciones de mesa
watch(mesaAccion, (accion) => {
  if (accion) {
    // Hay una acción activa, pausar temporizador
    limpiarTemporizador();
  } else {
    // No hay acción activa, reanudar temporizador
    iniciarTemporizador();
  }
});

// Watch para pausar/reanudar temporizador según diálogo de tickets
watch(mostrarTickets, (mostrando) => {
  if (mostrando) {
    // Diálogo abierto, pausar temporizador
    limpiarTemporizador();
  } else {
    // Diálogo cerrado, reanudar temporizador si no hay acción activa
    if (!mesaAccion.value) {
      iniciarTemporizador();
    }
  }
});

// Watch para pausar/reanudar temporizador según diálogo de cambio CashKeeper
watch(mostrarCambioCashKeeper, (mostrando) => {
  if (mostrando) {
    // Diálogo abierto, pausar temporizador
    limpiarTemporizador();
  } else {
    // Diálogo cerrado, reanudar temporizador si no hay acción activa
    if (!mesaAccion.value) {
      iniciarTemporizador();
    }
  }
});

onMounted(() => {
  zonasStore.initStore();
  mesasStore.initStore();
  cuentaStore.initStore();
  camarerosStore.initStore();
  mesasStore.setMesaSeleccionada(null);
  
  // Iniciar temporizador al montar
  iniciarTemporizador();
});

onUnmounted(() => {
  // Limpiar temporizador al desmontar
  limpiarTemporizador();
});

</script>

<style scoped>
.content-wrapper {
  display: flex;
  height: 100%;
}

.mesas-grid-container {
  flex: 0 0 90%;
  width: 90%;
  padding-right: 16px;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
}

.mesas-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  padding: 16px;
  gap: 6px;
}

.mesa-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease-in-out;
}

/* AÑADIDO: Efecto visual al pasar el ratón por encima de la mesa */
.mesa-slot:hover {
  transform: scale(1.05); /* Hacemos que la mesa crezca un poco */
  cursor: pointer; /* Cambia el cursor para indicar que es clickeable */
}

.zonas-container {
  flex: 0 0 10%;
  width: 10%;
  padding-left: 16px;
  height: 100%;
  border-left: 1px solid rgba(var(--v-theme-outline), 0.2);
}

.zonas-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  height: 100%;
  overflow-y: auto;
  padding-right: 8px;
}

/* Scrollbar personalizado para la lista de zonas */
.zonas-list::-webkit-scrollbar {
  width: 6px;
}

.zonas-list::-webkit-scrollbar-track {
  background: rgba(var(--v-theme-outline), 0.1);
  border-radius: 3px;
}

.zonas-list::-webkit-scrollbar-thumb {
  background: rgba(var(--v-theme-primary), 0.3);
  border-radius: 3px;
}

.zonas-list::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--v-theme-primary), 0.5);
}

/* Scrollbar personalizado para el contenedor de mesas */
.mesas-grid-container::-webkit-scrollbar {
  width: 6px;
}

.mesas-grid-container::-webkit-scrollbar-track {
  background: rgba(var(--v-theme-outline), 0.1);
  border-radius: 3px;
}

.mesas-grid-container::-webkit-scrollbar-thumb {
  background: rgba(var(--v-theme-primary), 0.3);
  border-radius: 3px;
}

.mesas-grid-container::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--v-theme-primary), 0.5);
}

.cancelar-accion-btn {
  height: 80px !important;
  font-size: 1.2rem !important;
  border-radius: 16px !important;
  text-transform: none;
  font-weight: 600;
}
</style>