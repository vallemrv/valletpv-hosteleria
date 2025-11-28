<template>
  <UiMainWindows :title="tituloVista" icon="mdi-receipt-text">
    <template #actions>
      <UiActionButton icon="mdi-package-variant-plus" @click="varios">
        Varios
      </UiActionButton>
      <UiActionButton icon="mdi-call-split" @click="dividirCuenta">
        Dividir Cuenta
      </UiActionButton>
      <UiActionButton icon="mdi-printer" @click="imprimir" :disabled="!tieneLineasParaCobrar">
        Imprimir
      </UiActionButton>
      <UiActionButton icon="mdi-cash-register" @click="cobrar" :disabled="!tieneLineasParaCobrar">
        Cobrar
      </UiActionButton>
      <UiActionButton icon="mdi-table-chair" @click="irAMesas">
        Ir a Mesas
      </UiActionButton>
    </template>

    <div class="cuenta-container">
      <div class="cuenta-lista-container">
        <UiCuentaLista :items="cuentaItems" @delete-item="onDeleteItem" />
      </div>

      <div class="cantidad-container">
        <UiTecladoNumerico />
      </div>

      <div class="productos-container">
        <UiBotoneraProductos @productoClick="onProductoClick" />
      </div>

      <div class="secciones-container">
        <div class="secciones-list">

          <UiBotonAsociable v-for="seccion in seccionesVisibles" :key="seccion.id" :texto="seccion.nombre"
            icon="mdi-group" :selected="seccionSeleccionada?.id === seccion.id"
            :asociado="seccionesStore.seccionAsociada?.id === seccion.id" @click="seleccionarSeccion(seccion)"
            @toggle-asociacion="toggleAsociarSeccion(seccion, $event)" />

          <UiBotonAsociable texto="Buscar" icon="mdi-magnify" :selected="false" :asociado="false" :showCheckbox="false"
            @click="buscarSeccion" class="buscar-btn" />

        </div>
      </div>
    </div>

    <UiVariosDialog v-model="dialogVarios" @on_action="onDialogVariosAction" />

    <UiCobroDialog v-model="dialogCobro" :items="itemsToCobrar"
      :mostrar-teclado="!empresasStore.empresaActiva?.usa_cash_keeper" @pagoConfirmado="onDialogCobroAction" />

    <UiCashKeeperDialog v-model="dialogCashKeeper" :total-cobro="totalACobrar"
      @cobro-completado="onCashKeeperCompletado" @cobro-cancelado="onCashKeeperCancelado" />

    <UiPinPadDialog v-model="dialogPinPad" :total-cobro="totalACobrar" :mesa-id="String(mesasStore.mesaSel?.ID || 0)"
      @pago-completado="onPinPadCompletado" @pago-cancelado="onPinPadCancelado" />

    <UiSepararTicketDialog v-model="dialogDividirCuenta" :items="itemsToCobrar"
      @separacionConfirmada="onConfirmarSeparacion" />

    <UiBorrarDialog v-model="dialogBorrar" :item="itemABorrar" :nombre_mesa="mesasStore.mesaSel?.Nombre || ''"
      @anulacionConfirmada="onDialogBorrarAction" />

    <UiAutorizacionDialog v-model="mostrarAutorizacion" :camareros="camarerosConPermiso"
      :parametros="parametrosAutorizacion" @autorizacionSolicitada="onAutorizacionSolicitada"
      @autorizacionCancelada="onAutorizacionCancelada" />

    <UiTaskbarBusqueda :visible="mostrarTaskbarBusqueda" @close="cerrarTaskbarBusqueda" />

    <!-- Diálogo para selección de sugerencias -->
    <UiSeleccionarSugerenciaDialog v-if="idTeclaParaSugerencia" v-model="dialogSeleccionarSugerencia"
      :id-tecla="idTeclaParaSugerencia" @sugerencia-seleccionada="onSugerenciaSeleccionada"
      @cancelado="onSugerenciaCancelada" />

    <!-- Snackbar para mensajes de autorización -->
    <UiSnackbar v-model="mostrarSnackbar" :message="mensajeSnackbar" type="success" :timeout="4000"
      location="bottom center" @close="mostrarSnackbar = false" />

    <!-- Snackbar genérico para notificaciones -->
    <UiSnackbar v-model="snackbar.show" :message="snackbar.text" :type="snackbar.type" :timeout="2000"
      location="bottom center" />

  </UiMainWindows>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  useMesasStore, useCuentaStore,
  useSeccionesStore,
  useTeclasStore,
  Cuenta, Seccion, Tecla,
  useCamarerosStore,
  useCantidadStore,
  useEmpresasStore,
  useSugerenciasStore,
  UiSeleccionarSugerenciaDialog,
  type CuentaItem,
  type InfoCobro
} from 'valle-tpv-lib';




const router = useRouter();
const mesasStore = useMesasStore();
const cuentaStore = useCuentaStore();
const seccionesStore = useSeccionesStore();
const teclasStore = useTeclasStore();
const camarerosStore = useCamarerosStore();
const cantidadStore = useCantidadStore();
const empresasStore = useEmpresasStore();
const sugerenciasStore = useSugerenciasStore();

// Computed para el título que incluye la mesa seleccionada
const tituloVista = computed(() => {
  const mesaSeleccionada = mesasStore.mesaSel;
  return mesaSeleccionada
    ? `Cuenta - ${mesaSeleccionada.Nombre || `Mesa ${mesaSeleccionada.ID}`}`
    : 'Cuenta';
});

// Datos reactivos
// Variables para el diálogo de "Varios"
const dialogVarios = ref(false)
const dialogCobro = ref(false)
const dialogDividirCuenta = ref(false)
const dialogBorrar = ref(false)
const dialogCashKeeper = ref(false)
const dialogPinPad = ref(false)

// Variables para el diálogo de selección de sugerencias
const dialogSeleccionarSugerencia = ref(false)
const idTeclaParaSugerencia = ref<string | null>(null)
const productoParaSugerencia = ref<Tecla | null>(null) // Guardamos referencia al producto original

// Variables para el sistema de autorización de borrado de líneas
const mostrarAutorizacion = ref(false);
const parametrosAutorizacion = ref<Record<string, any>>({});

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

const itemsToCobrar = ref<Array<CuentaItem>>([]);
const itemABorrar = ref<CuentaItem | null>(null);


const cuentaItems = computed(() => cuentaStore.cuentaDetalladaPorMesa(mesasStore.mesaSel?.ID || 0));

// Computed para calcular el total a cobrar
const totalACobrar = computed(() => {
  return itemsToCobrar.value.reduce((sum, item) => sum + item.total, 0);
});

// Computed para verificar si hay líneas para cobrar
const tieneLineasParaCobrar = computed(() => cuentaItems.value.length > 0);

// Computed para obtener camareros con permiso de borrar líneas
const camarerosConPermiso = computed(() => {
  return camarerosStore.camarerosPorPermiso('borrar_linea');
});

const secciones = computed(() => seccionesStore.items);
const seccionSeleccionada = computed(() => seccionesStore.seccSel);

// CAMBIO: Nueva propiedad computada que devuelve solo las 5 primeras secciones
const seccionesVisibles = computed(() => secciones.value.slice(0, 6));

const mostrarTaskbarBusqueda = ref(false);

// Variables para el temporizador de inactividad
const temporizadorInactividad = ref<ReturnType<typeof setTimeout> | null>(null);
const tiempoInactividad = 20000; // 20 segundos

// Computed para verificar si hay diálogos activos que deben pausar el temporizador
const dialogosActivos = computed(() => {
  return dialogCobro.value ||
    dialogDividirCuenta.value ||
    dialogCashKeeper.value ||
    dialogPinPad.value ||
    dialogVarios.value ||
    dialogBorrar.value ||
    mostrarAutorizacion.value ||
    dialogSeleccionarSugerencia.value ||
    mostrarTaskbarBusqueda.value;
});

// Función para iniciar/reiniciar el temporizador
function iniciarTemporizador() {
  limpiarTemporizador();

  // No iniciar temporizador si hay diálogos activos
  if (dialogosActivos.value) {
    return;
  }

  temporizadorInactividad.value = setTimeout(async () => {
    // Verificar nuevamente si hay diálogos activos antes de redirigir
    if (!dialogosActivos.value) {
      // Comprobar si hay líneas en la mesa
      const lineasActuales = cuentaStore.cuentaDetalladaPorMesa(mesasStore.mesaSel?.ID || 0);

      if (lineasActuales.length === 0) {
        // Si no hay líneas, cerrar la mesa
        await mesasStore.cerrarMesa(mesasStore.mesaSel?.ID || 0);
      } else {
        // Si hay líneas, aparcar la mesa
        await cuentaStore.aparcarMesa(mesasStore.mesaSel?.ID || 0);
      }

      router.replace("/mesas"); // Volver a la vista principal
    }
  }, tiempoInactividad);
}

// Función para limpiar el temporizador
function limpiarTemporizador() {
  if (temporizadorInactividad.value) {
    clearTimeout(temporizadorInactividad.value);
    temporizadorInactividad.value = null;
  }
}

// Función para manejar actividad del usuario
function manejarActividad() {
  iniciarTemporizador();
}

// Watch para pausar/reanudar temporizador según diálogos activos
watch(dialogosActivos, (activos) => {
  if (activos) {
    limpiarTemporizador();
  } else {
    iniciarTemporizador();
  }
});

async function onDialogCobroAction(infoCobro: InfoCobro, itemsCobrados: CuentaItem[]) {
  const empresaActiva = empresasStore.empresaActiva;

  // Verificar si usa CashKeeper o PinPad según el tipo de pago
  if (infoCobro.tipo === 'efectivo' && empresaActiva?.usa_cash_keeper) {
    // Guardar items para cobrar y abrir diálogo de CashKeeper
    itemsToCobrar.value = itemsCobrados;
    dialogCashKeeper.value = true;
    return;
  }

  if (infoCobro.tipo === 'tarjeta' && empresaActiva?.usa_pinpad) {
    // Guardar items para cobrar y abrir diálogo de PinPad
    itemsToCobrar.value = itemsCobrados;
    dialogPinPad.value = true;
    return;
  }

  // Cobro normal sin dispositivos especiales
  await cuentaStore.cobrarMesa(mesasStore.mesaSel?.ID || 0, infoCobro, itemsCobrados);
  showSnackbar('Cobro realizado correctamente', 'success');

  // Verificar si después del cobro la mesa quedó sin líneas
  const lineasRestantes = cuentaStore.cuentaDetalladaPorMesa(mesasStore.mesaSel?.ID || 0);
  if (lineasRestantes.length === 0) {
    await mesasStore.cerrarMesa(mesasStore.mesaSel?.ID || 0);
    limpiarTemporizador();
    router.replace("/mesas");
  }
}

async function onConfirmarSeparacion(itemsSeparados: CuentaItem[]) {
  dialogCobro.value = true;
  itemsToCobrar.value = itemsSeparados;
  showSnackbar('Cuenta dividida correctamente', 'info');
}

// Funciones para manejar los eventos de CashKeeper
async function onCashKeeperCompletado(infoCobro: InfoCobro) {
  // El CashKeeperDialog ya proporciona todos los datos necesarios en infoCobro
  await cuentaStore.cobrarMesa(mesasStore.mesaSel?.ID || 0, infoCobro, itemsToCobrar.value);
  showSnackbar('Cobro con CashKeeper realizado correctamente', 'success');
  dialogCashKeeper.value = false;

  // Verificar si después del cobro la mesa quedó sin líneas
  const lineasRestantes = cuentaStore.cuentaDetalladaPorMesa(mesasStore.mesaSel?.ID || 0);
  if (lineasRestantes.length === 0) {
    await mesasStore.cerrarMesa(mesasStore.mesaSel?.ID || 0);
    limpiarTemporizador();
    router.replace("/mesas");
  }
}

function onCashKeeperCancelado() {
  dialogCashKeeper.value = false;
  showSnackbar('Cobro con CashKeeper cancelado', 'warning');
}

// Funciones para manejar los eventos de PinPad
async function onPinPadCompletado(recibo: any) {
  const totalCobrado = itemsToCobrar.value.reduce((sum, item) => sum + item.total, 0);

  // Serializar el recibo a JSON string para enviar al servidor
  const reciboJson = recibo ? JSON.stringify(recibo) : '';

  const infoCobro: InfoCobro = {
    tipo: 'tarjeta',
    totalEntregado: 0,
    cambio: 0,
    totalCobrado: totalCobrado,
    recibo: reciboJson
  };

  await cuentaStore.cobrarMesa(mesasStore.mesaSel?.ID || 0, infoCobro, itemsToCobrar.value);
  showSnackbar('Pago con tarjeta realizado correctamente', 'success');
  dialogPinPad.value = false;

  // Verificar si después del cobro la mesa quedó sin líneas
  const lineasRestantes = cuentaStore.cuentaDetalladaPorMesa(mesasStore.mesaSel?.ID || 0);
  if (lineasRestantes.length === 0) {
    await mesasStore.cerrarMesa(mesasStore.mesaSel?.ID || 0);
    limpiarTemporizador();
    router.replace("/mesas");
  }
}

function onPinPadCancelado() {
  dialogPinPad.value = false;
  showSnackbar('Pago con tarjeta cancelado', 'warning');
}

async function onDialogBorrarAction(motivo: string) {
  const item = itemABorrar.value;

  if (item == null) return;

  const lineas: Cuenta[] = await cuentaStore.desgloseLineas(
    mesasStore.mesaSel?.ID || 0, item.descripcion, item.precio, item.cantidad);

  // Verificar si hay camareros con permiso de borrar_lineas
  if (camarerosConPermiso.value.length > 0) {
    // Si hay camareros con permiso, solicitar autorización
    parametrosAutorizacion.value = {
      inst: {
        idm: mesasStore.mesaSel?.ID || 0,
        idc: camarerosStore.camarerosSel?.id || null,
        Descripcion: lineas[0]?.Descripcion || '',
        can: lineas.length,
        motivo: motivo,
        ids: lineas.map(linea => linea.ID).filter(id => id !== null)
      },
      mesa_nombre: mesasStore.mesaSel?.Nombre || '',
      accion: 'borrar_linea'
    };
    mostrarAutorizacion.value = true;
    dialogBorrar.value = false; // Cerrar el diálogo de borrar temporalmente
  } else {
    // Si no hay camareros con permiso, proceder con el borrado normal
    await cuentaStore.rmConMotivo(mesasStore.mesaSel?.ID || 0, motivo, lineas);
    showSnackbar('Líneas eliminadas de la cuenta', 'info');

    if (cuentaItems.value.length === 0) {
      mesasStore.cerrarMesa(mesasStore.mesaSel?.ID || 0);
    }
    itemABorrar.value = null;
  }
}

function onDialogVariosAction(action: { id: string; data?: any }) {
  if (action.id === 'aceptar' && action.data) {
    cantidadStore.setCantidad(action.data.cantidad);
    const producto = new Tecla({
      ID: -1,
      nombre: action.data.descripcion,
      descripcion_t: action.data.descripcion,
      descripcion_r: action.data.descripcion,
      p1: action.data.precio,
      p2: action.data.precio,
      hay_existencias: 1
    });

    pedirProducto(producto);
    dialogVarios.value = false; // Cerrar el diálogo
    showSnackbar('Producto añadido a la cuenta', 'success');
  } else if (action.id === 'cancelar') {
    dialogVarios.value = false; // Cerrar el diálogo
  }
}

// Función para manejar la autorización solicitada
async function onAutorizacionSolicitada() {
  // Mostrar snackbar de confirmación
  mensajeSnackbar.value = 'Se ha solicitado autorización para borrar las líneas';
  mostrarSnackbar.value = true;

  // Cerrar el diálogo de autorización
  mostrarAutorizacion.value = false;

  // Limpiar el item a borrar
  itemABorrar.value = null;
}

// Función para manejar la cancelación de autorización
function onAutorizacionCancelada() {
  // Limpiar el item a borrar sin mostrar snackbar
  itemABorrar.value = null;
}

// Métodos de navegación
async function irAMesas() {
  limpiarTemporizador();

  // Comprobar si hay líneas en la mesa
  const lineasActuales = cuentaStore.cuentaDetalladaPorMesa(mesasStore.mesaSel?.ID || 0);

  if (lineasActuales.length === 0) {
    // Si no hay líneas, cerrar la mesa
    await mesasStore.cerrarMesa(mesasStore.mesaSel?.ID || 0);
  } else {
    // Si hay líneas, aparcar la mesa
    await cuentaStore.aparcarMesa(mesasStore.mesaSel?.ID || 0);
  }

  router.replace("/mesas");
}

// Métodos de acciones
async function cobrar() {
  await cuentaStore.aparcarMesa(mesasStore.mesaSel?.ID || 0);
  itemsToCobrar.value = cuentaItems.value;
  dialogCobro.value = true;
}

async function imprimir() {
  await cuentaStore.aparcarMesa(mesasStore.mesaSel?.ID || 0);
  cuentaStore.imprimirCuenta(mesasStore.mesaSel);
  showSnackbar('Cuenta enviada a impresión', 'info');
}

function varios() {
  dialogVarios.value = true;
}

async function dividirCuenta() {
  await cuentaStore.aparcarMesa(mesasStore.mesaSel?.ID || 0);
  // Usar getter que agrupa por Descripcion (no descripcion_t)
  itemsToCobrar.value = cuentaStore.cuentaAgrupadaPorPedido(mesasStore.mesaSel?.ID || 0);
  dialogDividirCuenta.value = true;
}

// AÑADIDO: Función para togglear la asociación de una sección
function toggleAsociarSeccion(seccion: Seccion, asociar: boolean | null) {
  if (asociar) {
    seccionesStore.asociarSeccion(seccion.id);
  } else {
    seccionesStore.quitarAsociacion();
  }
  seccionesStore.setSeccion(seccion);
  // Cerrar taskbar de búsqueda y volver al modo sección para mostrar productos
  mostrarTaskbarBusqueda.value = false;
  teclasStore.tipo = 'seccion';
  teclasStore.textoBusqueda = null;
}


// Métodos de cuenta
async function onDeleteItem(item: CuentaItem) {

  const itemModificado: CuentaItem = {
    cantidad: cantidadStore.cantidad <= item.cantidad ? cantidadStore.cantidad : item.cantidad,
    descripcion: item.descripcion,
    precio: item.precio,
    total: item.total,
  }

  if (item.estado === 'N') {
    const lineas: Cuenta[] = await cuentaStore.desgloseLineas(
      mesasStore.mesaSel?.ID || 0, item.descripcion, item.precio, itemModificado.cantidad);

    // Borrar directamente sin diálogo
    for (let linea of lineas) {
      if (linea.ID) {
        await cuentaStore.rm(linea.ID);
      }
    }

    showSnackbar('Elemento eliminado de la cuenta', 'info');
    cantidadStore.reset();
    return;
  }

  itemABorrar.value = itemModificado;
  dialogBorrar.value = true;
  cantidadStore.reset();
}

// Métodos de productos
function onProductoClick(producto: Tecla) {
  mostrarTaskbarBusqueda.value = false;
  teclasStore.tipo = 'seccion';
  teclasStore.textoBusqueda = null;
  if (producto.tipo === 'CM') {
    teclasStore.setTeclaPadreSeleccionada(producto);
  } else {
    teclasStore.setTeclaPadreSeleccionada(null);

    // Verificar si el producto tiene sugerencias con incremento > 0
    const sugerencias = sugerenciasStore.sugPorTeclaConIncremento(String(producto.ID));
    if (sugerencias && sugerencias.length > 0) {
      // Abrir diálogo para seleccionar sugerencias
      idTeclaParaSugerencia.value = String(producto.ID);
      productoParaSugerencia.value = producto;
      dialogSeleccionarSugerencia.value = true;
    } else {
      // Sin sugerencias, pedir producto directamente
      pedirProducto(producto);
    }
  }

}

function pedirProducto(producto: Tecla) {
  for (let i = 0; i < cantidadStore.cantidad; i++) {
    let cuenta = new Cuenta(
      {
        IDArt: producto.ID,
        Estado: "N",
        camarero: camarerosStore.camarerosSel?.id || 1,
        IDMesa: mesasStore.mesaSel?.ID || 0,
        nomMesa: mesasStore.mesaSel?.Nombre || '',
        descripcion_t: producto.descripcion_t,
        Descripcion: producto.descripcion_r,
        Precio: mesasStore.mesaSel?.Tarifa == 1 ? producto.p1 : producto.p2,
      }
    );
    cuentaStore.insert(cuenta);
    mesasStore.abrirMesa(mesasStore.mesaSel?.ID || 0);
  }
  cantidadStore.reset();
}

// Handlers del diálogo de sugerencias
function onSugerenciaSeleccionada(sugerencias: any[]) {
  if (!productoParaSugerencia.value) return;

  // Hacer una copia del producto original
  const productoModificado = new Tecla({
    ...productoParaSugerencia.value,
    // Calcular incremento total de sugerencias
    p1: productoParaSugerencia.value.p1 + sugerencias.reduce((sum, s) => sum + (s.incremento || 0), 0),
    p2: productoParaSugerencia.value.p2 + sugerencias.reduce((sum, s) => sum + (s.incremento || 0), 0),
    // Modificar descripción con sugerencias
    descripcion_r: `${productoParaSugerencia.value.descripcion_r} (${sugerencias.map(s => s.sugerencia).join(', ')})`
  });

  // Usar la función pedirProducto normal
  pedirProducto(productoModificado);

  // Cerrar diálogo y limpiar
  dialogSeleccionarSugerencia.value = false;
  idTeclaParaSugerencia.value = null;
  productoParaSugerencia.value = null;
}

function onSugerenciaCancelada() {
  // Al cancelar, agregar el producto sin modificadores
  if (productoParaSugerencia.value) {
    pedirProducto(productoParaSugerencia.value);
  }

  // Cerrar diálogo y limpiar
  dialogSeleccionarSugerencia.value = false;
  idTeclaParaSugerencia.value = null;
  productoParaSugerencia.value = null;
}



// Métodos de secciones
function seleccionarSeccion(seccion: any) {
  seccionesStore.setSeccion(seccion);
  // Cerrar taskbar de búsqueda y volver al modo sección para mostrar productos
  mostrarTaskbarBusqueda.value = false;
  teclasStore.tipo = 'seccion';
  teclasStore.textoBusqueda = null;
}

// CAMBIO: Nueva función para el botón de búsqueda
function buscarSeccion() {
  teclasStore.tipo = 'find';
  mostrarTaskbarBusqueda.value = true;
}

function cerrarTaskbarBusqueda() {
  mostrarTaskbarBusqueda.value = false;
  // Limpiar texto de búsqueda al cerrar
  teclasStore.tipo = 'seccion';
  teclasStore.textoBusqueda = null;
}


onMounted(async () => {
  await camarerosStore.initStore();
  await mesasStore.initStore();
  await seccionesStore.initStore();
  await teclasStore.initStore();
  await cuentaStore.initStore();
  await sugerenciasStore.initStore();
  await cuentaStore.comprobarCuenta(mesasStore.mesaSel?.ID || 0);
  cantidadStore.reset();

  // Configurar listeners para detectar actividad
  const eventos = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];

  eventos.forEach(evento => {
    document.addEventListener(evento, manejarActividad, true);
  });

  // Iniciar temporizador
  iniciarTemporizador();
});

onUnmounted(() => {
  // Limpiar temporizador y listeners
  limpiarTemporizador();

  const eventos = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];

  eventos.forEach(evento => {
    document.removeEventListener(evento, manejarActividad, true);
  });
});
</script>

<style scoped>
.cuenta-container {
  display: flex;
  height: 100%;
  overflow: hidden;
  padding: 0;
}

.cuenta-lista-container {
  flex: 40;
  height: 100%;
  overflow-y: auto;
  background: var(--v-theme-surface);
  border-radius: 12px;
  border: 1px solid rgba(var(--v-theme-primary), 0.12);
}

.cantidad-container {
  flex: 7;
  height: 100%;
  overflow-y: auto;
  background: var(--v-theme-surface);
  border-radius: 12px;
  border: 1px solid rgba(var(--v-theme-primary), 0.12);
}

.productos-container {
  flex: 42;
  height: 100%;
  overflow: hidden;
  background: var(--v-theme-surface);
  border-radius: 12px;
  border: 1px solid rgba(var(--v-theme-primary), 0.12);
  padding: 16px;
}

.secciones-container {
  flex: 10;
  height: 100%;
  overflow: hidden;
  background: var(--v-theme-surface);
  border-radius: 12px;
  border: 2px solid rgba(var(--v-theme-primary), 0.2);
  padding: 8px;
}

/* --- CAMBIOS PRINCIPALES PARA LA COLUMNA DE SECCIONES --- */
.secciones-list {
  display: grid;
  grid-template-rows: repeat(7, 1fr);
  /* 6 filas de igual altura */
  gap: 8px;
  height: 100%;
  /* Se elimina el overflow-y: auto; */
}

/* Estilo para el nuevo botón de búsqueda */
.buscar-btn :deep(.zona-btn) {
  border-style: dashed !important;
  background-color: rgba(var(--v-theme-on-surface), 0.04) !important;
  border-color: rgba(var(--v-theme-on-surface), 0.2) !important;
}

.buscar-btn :deep(.zona-btn:hover) {
  background-color: rgba(var(--v-theme-primary), 0.1) !important;
}

.buscar-btn :deep(.zona-text) {
  font-size: 0.9rem !important;
}

/* --- FIN DE CAMBIOS EN SECCIONES --- */


/* Scrollbar personalizado para lista de cuenta */
.cuenta-lista-container::-webkit-scrollbar {
  width: 6px;
}

.cuenta-lista-container::-webkit-scrollbar-track {
  background: rgba(var(--v-theme-on-surface), 0.05);
  border-radius: 3px;
}

.cuenta-lista-container::-webkit-scrollbar-thumb {
  background: rgba(var(--v-theme-primary), 0.3);
  border-radius: 3px;
}

.cuenta-lista-container::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--v-theme-primary), 0.5);
}


/* Estilos para el formulario del diálogo "Varios" */
.varios-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px;
}

.varios-form .v-text-field {
  margin-bottom: 0;
}

.varios-form .v-text-field--outlined {
  border-radius: 8px;
}

.varios-form .v-text-field--readonly {
  background-color: rgba(var(--v-theme-on-surface), 0.05);
}
</style>