<template>
  <UiMainWindows title="Camareros" icon="mdi-account-group">
    <template #actions>
      <UiActionButton icon="mdi-plus" @click="goCamareros">camareros</UiActionButton>
      <UiActionButton icon="mdi-printer" @click="abrirReceptores">Receptores</UiActionButton>
      <UiActionButton icon="mdi-receipt" @click="abrirTickets">Tickets</UiActionButton>
      <UiActionButton icon="mdi-cash-register" @click="abrirCajon">{{ textoCajon }}</UiActionButton>
      <UiMenuButton
        :items="menuItems"
        @on_select="handleMenuSelect"
      />
    </template>

    <div class="contenedor-botonera">
      <div
        v-for="camarero in camarerosParaMostrar"
        :key="camarero.id"
        class="camarero-slot"
      >
        <UiCamareroBtn
          :nombre_completo="camarero.nombre + ' ' + camarero.apellidos"
          @click="selectCamarero(camarero)"
        />
      </div>
    </div>

    <!-- Diálogos -->
    <UiReceptoresDialog 
      v-model="mostrarReceptores" 
      @cambiosGuardados="onCambiosGuardados" 
    />
    
    <UiTicketsDialog 
      v-model="mostrarTickets" 
    />

    <!-- Diálogo de CashKeeper para cambio -->
    <UiCambioCashKeeperDialog
      v-model="mostrarCambioCashKeeper"
    />
    
    <!-- Diálogo de Arqueo con CashKeeper -->
    <UiArqueoConCashKeeperDialog
      v-model="mostrarArqueoCashKeeper"
      @arqueo-completado="onArqueoCompletado"
      @arqueo-cancelado="onArqueoCancelado"
    />
    
    <!-- Snackbar para error de conexión -->
    <UiSnackbar
      v-model="mostrarErrorConexion"
      message="Se necesita conexión a internet para acceder a esta función"
      type="error"
      :timeout="4000"
      @close="mostrarErrorConexion = false"
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
import { useRouter } from 'vue-router';
import { onMounted, computed, ref } from 'vue';
import { useCamarerosStore, Camarero, useEmpresasStore, useConnectionStore } from 'valle-tpv-lib'; // Ajusta la ruta si es necesario
import { UiArqueoConCashKeeperDialog } from 'valle-tpv-lib';

const router = useRouter();
const camarerosStore = useCamarerosStore();
const empresasStore = useEmpresasStore();
const connectionStore = useConnectionStore();
const camarerosAuth = computed(() => camarerosStore.camarerosAuth);

// CAMBIO: Computed para limitar a 9 los camareros a mostrar
const camarerosParaMostrar = computed(() => camarerosAuth.value.slice(0, 9));

// Computed para el texto del botón de cajón/cambio
const textoCajon = computed(() => {
  const empresaActiva = empresasStore.empresaActiva;
  return empresaActiva?.usa_cash_keeper ? 'Cambio' : 'Abrir cajón';
});

// Referencias para los diálogos
const mostrarReceptores = ref(false);
const mostrarTickets = ref(false);
const mostrarCambioCashKeeper = ref(false);
const mostrarArqueoCashKeeper = ref(false);
const mostrarErrorConexion = ref(false);

// Snackbar genérico para notificaciones
const snackbar = ref({ show: false, text: '', type: 'info' as 'info' | 'success' | 'warning' | 'error' });
function showSnackbar(text: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') {
  snackbar.value.text = text;
  snackbar.value.type = type;
  snackbar.value.show = true;
}

onMounted(async () => {
  await camarerosStore.initStore();
  camarerosStore.camarerosSel = null;
});


const menuItems = [
  { icon: 'mdi-domain', text: 'Empresas', id: 'empresas' },
  { icon: 'mdi-cash-multiple', text: 'Arqueo caja', id: 'arqueo' }
];

function goCamareros() {
  router.push({ name: 'CamarerosManager' });
}

// Función para abrir diálogo de receptores
function abrirReceptores() {
  // Verificar conexión antes de abrir receptores
  if (!connectionStore.isConnected) {
    mostrarErrorConexion.value = true;
    return;
  }
  mostrarReceptores.value = true;
}

// Función para abrir diálogo de tickets
function abrirTickets() {
  // Verificar conexión antes de abrir tickets
  if (!connectionStore.isConnected) {
    mostrarErrorConexion.value = true;
    return;
  }
  mostrarTickets.value = true;
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

function selectCamarero(camarero: Camarero) {
  camarerosStore.setCamarerosSel(camarero);
  router.push({ name: 'Mesas' });
}

function handleMenuSelect(id: string) {
  if (id === 'empresas') {
    router.replace({ name: 'Empresas' });
    return;
  }
  if (id === 'arqueo') {
    // Verificar conexión antes de navegar al arqueo
    if (!connectionStore.isConnected) {
      mostrarErrorConexion.value = true;
      return;
    }
    
    // Verificar si usa CashKeeper
    const empresaActiva = empresasStore.empresaActiva;
    if (empresaActiva?.usa_cash_keeper) {
      // Abrir diálogo de arqueo con CashKeeper
      mostrarArqueoCashKeeper.value = true;
    } else {
      // Navegar a la vista tradicional de arqueo
      router.push({ name: 'Arqueo' });
    }
    return;
  }
}

// Funciones para manejar los eventos de los diálogos
function onCambiosGuardados() {
  showSnackbar('Receptores configurados correctamente', 'success');
}

function onArqueoCompletado() {
  showSnackbar('Arqueo realizado correctamente', 'success');
}

function onArqueoCancelado() {
  showSnackbar('Arqueo cancelado', 'info');
}
</script>


<style scoped>
.contenedor-botonera {
  /* Hacemos que la rejilla ocupe todo el espacio disponible */
  width: 100%;
  height: 100%;
  
  /* Definimos la rejilla de 3x3 */
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 3 columnas de igual ancho */
  grid-template-rows: repeat(3, 1fr);    /* 3 filas de igual alto */
  
  /* Espaciado */
  gap: 16px;
  padding: 16px;
  box-sizing: border-box;
}

.camarero-slot {
  /* Hacemos que el botón se estire para llenar la celda */
  display: flex;
  align-items: stretch;
  justify-content: stretch;
}
</style>