<template>
  <UiMainWindows title="Arqueo de Caja" icon="mdi-cash-register" class="main-window">
    <template #actions>
      <UiActionButton icon="mdi-cash-plus" @click="dialogEfectivo = true" class="action-btn">
        Agregar Efectivo
      </UiActionButton>
      <UiActionButton icon="mdi-receipt" @click="dialogGastos = true" class="action-btn">
        Agregar Gastos
      </UiActionButton>
      <UiActionButton icon="mdi-calculator" @click="arquearCaja" color="success" class="action-btn" :loading="loadingArqueo" :disabled="loadingArqueo">
        Arquear Caja
      </UiActionButton>
      <UiActionButton icon="mdi-arrow-left" @click="cancelar" class="action-btn">
        Volver al TPV
      </UiActionButton>
    </template>

    <v-container fluid class="content-container">
      <v-row no-gutters class="fill-height">
        <v-col cols="6" class="pr-4">
          <v-card class="card-full-height elevation-6 rounded-xl">
            <v-card-title class="card-title bg-primary">
              <v-row no-gutters align="center">
                <v-col cols="6">
                  <v-icon class="mr-2">mdi-cash-multiple</v-icon>
                  Efectivo
                </v-col>
                <v-col cols="6" class="text-right">
                  <span class="mr-3 cambio-text">Cambio: {{ cambioRemanente.toFixed(2) }} €</span>
                  <v-btn icon @click="abrirDialogCambio" size="small" class="edit-btn" variant="elevated">
                    <v-icon size="20">mdi-pencil</v-icon>
                  </v-btn>
                </v-col>
              </v-row>
            </v-card-title>
            <v-card-text class="pa-0">
              <div class="table-header">
                <div class="header-row">
                  <div class="header-cell" style="width: 30%;">Denominación (€)</div>
                  <div class="header-cell" style="width: 25%;">Cantidad</div>
                  <div class="header-cell" style="width: 25%;">Total (€)</div>
                  <div class="header-cell" style="width: 10%;">Acciones</div>
                </div>
              </div>
              <div class="table-container">
                <v-data-table
                  :headers="headersCambio"
                  :items="efectivo"
                  hide-default-footer
                  hide-default-header
                  density="comfortable"
                  class="data-table"
                  :items-per-page="-1"
                  disable-pagination
                >
                <template v-slot:item.denominacion="{ item }">
                  <span class="table-text item-text">{{ item.denominacion }} €</span>
                </template>
                <template v-slot:item.cantidad="{ item }">
                  <span class="table-text item-text">{{ item.cantidad }}</span>
                </template>
                <template v-slot:item.total="{ item }">
                  <span class="table-text item-text">{{ (item.cantidad * item.denominacion).toFixed(2) }} €</span>
                </template>
                <template v-slot:item.acciones="{ item }">
                  <v-btn
                    icon
                    color="red"
                    @click="eliminarEfectivo(item)"
                    size="small"
                    class="action-btn-small"
                  >
                    <v-icon>mdi-delete</v-icon>
                  </v-btn>
                </template>
                <template v-slot:no-data>
                  <div class="text-center pa-4">
                    <span class="table-text">No hay efectivo registrado</span>
                  </div>
                </template>
              </v-data-table>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" class="pl-4">
          <v-card class="card-full-height elevation-6 rounded-xl">
            <v-card-title class="card-title bg-secondary">
              <v-icon class="mr-2">mdi-receipt</v-icon>
              Gastos
            </v-card-title>
            <v-card-text class="pa-0">
              <div class="table-header">
                <div class="header-row">
                  <div class="header-cell" style="width: 50%;">Descripción</div>
                  <div class="header-cell" style="width: 30%;">Importe (€)</div>
                  <div class="header-cell" style="width: 10%;">Acciones</div>
                </div>
              </div>
              <div class="table-container">
                <v-data-table
                  :headers="headersGastos"
                  :items="gastos"
                  hide-default-footer
                  hide-default-header
                  density="comfortable"
                  class="data-table"
                  :items-per-page="-1"
                  disable-pagination
                >
                <template v-slot:item.descripcion="{ item }">
                  <span class="table-text item-text">{{ item.descripcion }}</span>
                </template>
                <template v-slot:item.importe="{ item }">
                  <span class="table-text item-text">{{ item.importe.toFixed(2) }} €</span>
                </template>
                <template v-slot:item.acciones="{ item }">
                  <v-btn
                    icon
                    color="red"
                    @click="eliminarGasto(item)"
                    size="small"
                    class="action-btn-small"
                  >
                    <v-icon>mdi-delete</v-icon>
                  </v-btn>
                </template>
                <template v-slot:no-data>
                  <div class="text-center pa-4">
                    <span class="table-text">No hay gastos registrados</span>
                  </div>
                </template>
              </v-data-table>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Snackbar para mostrar advertencia de arqueo -->
    <UiSnackbar
      v-model="mostrarAdvertenciaArqueo"
      :message="mensajeAdvertenciaArqueo"
      type="warning"
      :timeout="-1"
      @close="mostrarAdvertenciaArqueo = false"
    />
    
    <!-- Snackbar genérico para notificaciones -->
    <UiSnackbar 
      v-model="snackbar.show" 
      :message="snackbar.text"
      :type="snackbar.type"
      :timeout="4000"
    />
    
    <!-- Debug: Mostrar estado de hayArqueo -->
    <div v-if="false" style="position: fixed; top: 10px; right: 10px; background: red; color: white; padding: 5px; z-index: 9999;">
      hayArqueo: {{ hayArqueo }} | mostrarAdvertencia: {{ mostrarAdvertenciaArqueo }}
    </div>

    <v-dialog v-model="dialogEfectivo" max-width="600px" persistent>
      <v-card elevation="16" rounded="xl" class="dialog-card">
        <v-card-title class="dialog-title bg-surface-variant">
          <v-icon class="mr-3" color="primary">mdi-cash-plus</v-icon>
          Agregar Efectivo
        </v-card-title>

        <v-card-text class="pa-6">
          <p class="text-medium-emphasis mb-6 text-body-1">
            Introduce la denominación y cantidad del efectivo:
          </p>
          
          <v-text-field
            v-model.number="nuevoEfectivo.denominacion"
            label="Denominación (€)"
            type="number"
            step="0.01"
            min="0"
            variant="outlined"
            density="comfortable"
            prepend-inner-icon="mdi-currency-eur"
            class="mb-4 dialog-field"
          />
          
          <v-text-field
            v-model.number="nuevoEfectivo.cantidad"
            label="Cantidad"
            type="number"
            min="0"
            variant="outlined"
            density="comfortable"
            prepend-inner-icon="mdi-counter"
            class="dialog-field"
          />
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions class="pa-6">
          <v-row no-gutters>
            <v-col cols="6" class="pr-2">
              <v-btn
                variant="tonal"
                @click="dialogEfectivo = false"
                block
                size="large"
                height="72"
                elevation="2"
                rounded="lg"
                class="dialog-btn"
              >
                Cancelar
              </v-btn>
            </v-col>
            <v-col cols="6" class="pl-2">
              <v-btn
                variant="elevated"
                @click="agregarEfectivo"
                block
                size="large"
                height="72"
                color="primary"
                elevation="2"
                rounded="lg"
                :disabled="!nuevoEfectivo.denominacion || !nuevoEfectivo.cantidad"
                class="dialog-btn"
              >
                Agregar
              </v-btn>
            </v-col>
          </v-row>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="dialogGastos" max-width="600px" persistent>
      <v-card elevation="16" rounded="xl" class="dialog-card">
        <v-card-title class="dialog-title bg-surface-variant">
          <v-icon class="mr-3" color="secondary">mdi-receipt</v-icon>
          Agregar Gastos
        </v-card-title>

        <v-card-text class="pa-6">
          <p class="text-medium-emphasis mb-6 text-body-1">
            Introduce la descripción e importe del gasto:
          </p>
          
          <v-text-field
            v-model="nuevoGasto.descripcion"
            label="Descripción"
            variant="outlined"
            density="comfortable"
            prepend-inner-icon="mdi-text"
            class="mb-4 dialog-field"
          />
          
          <v-text-field
            v-model.number="nuevoGasto.importe"
            label="Importe (€)"
            type="number"
            step="0.01"
            min="0"
            variant="outlined"
            density="comfortable"
            prepend-inner-icon="mdi-currency-eur"
            class="dialog-field"
          />
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions class="pa-6">
          <v-row no-gutters>
            <v-col cols="6" class="pr-2">
              <v-btn
                variant="tonal"
                @click="dialogGastos = false"
                block
                size="large"
                height="72"
                elevation="2"
                rounded="lg"
                class="dialog-btn"
              >
                Cancelar
              </v-btn>
            </v-col>
            <v-col cols="6" class="pl-2">
              <v-btn
                variant="elevated"
                @click="agregarGasto"
                block
                size="large"
                height="72"
                color="primary"
                elevation="2"
                rounded="lg"
                :disabled="!nuevoGasto.descripcion || !nuevoGasto.importe"
                class="dialog-btn"
              >
                Agregar
              </v-btn>
            </v-col>
          </v-row>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="dialogCambio" max-width="600px" persistent>
      <v-card elevation="16" rounded="xl" class="dialog-card">
        <v-card-title class="dialog-title bg-surface-variant">
          <v-icon class="mr-3" color="primary">mdi-cash</v-icon>
          Modificar Cambio Remanente
        </v-card-title>

        <v-card-text class="pa-6">
          <p class="text-medium-emphasis mb-6 text-body-1">
            Introduce el nuevo cambio remanente:
          </p>
          
          <v-text-field
            v-model.number="nuevoCambio"
            label="Cambio Remanente (€)"
            type="number"
            step="0.01"
            min="0"
            variant="outlined"
            density="comfortable"
            prepend-inner-icon="mdi-currency-eur"
            class="dialog-field"
          />
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions class="pa-6">
          <v-row no-gutters>
            <v-col cols="6" class="pr-2">
              <v-btn
                variant="tonal"
                @click="dialogCambio = false"
                block
                size="large"
                height="72"
                elevation="2"
                rounded="lg"
                class="dialog-btn"
              >
                Cancelar
              </v-btn>
            </v-col>
            <v-col cols="6" class="pl-2">
              <v-btn
                variant="elevated"
                @click="confirmarCambio"
                block
                size="large"
                height="72"
                color="primary"
                elevation="2"
                rounded="lg"
                :disabled="!nuevoCambio && nuevoCambio !== 0"
                class="dialog-btn"
              >
                Modificar
              </v-btn>
            </v-col>
          </v-row>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </UiMainWindows>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useEmpresasStore } from 'valle-tpv-lib';

const router = useRouter();
const empresasStore = useEmpresasStore();

const cambio = ref(0);
const cambioRemanente = ref(0);
const hayArqueo = ref(true);
const loadingArqueo = ref(false);
const mostrarAdvertenciaArqueo = ref(false);
const mensajeAdvertenciaArqueo = ref('No hay tickets nuevos para hacer un arqueo. Si realizas un arqueo, modificarás el arqueo ya existente.');
const efectivo = ref<{ denominacion: number; cantidad: number }[]>([]);
const gastos = ref<{ descripcion: string; importe: number }[]>([]);
const dialogEfectivo = ref(false);
const dialogGastos = ref(false);
const dialogCambio = ref(false);
const nuevoEfectivo = ref({ denominacion: 0, cantidad: 0 });
const nuevoGasto = ref({ descripcion: '', importe: 0 });
const nuevoCambio = ref(0);

// Snackbar genérico para notificaciones
const snackbar = ref({ show: false, text: '', type: 'info' as 'info' | 'success' | 'warning' | 'error' });
function showSnackbar(text: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') {
  snackbar.value.text = text;
  snackbar.value.type = type;
  snackbar.value.show = true;
}

const headersCambio = [
  { title: 'Denominación (€)', key: 'denominacion', width: '30%' },
  { title: 'Cantidad', key: 'cantidad', width: '30%' },
  { title: 'Total (€)', key: 'total', width: '30%' },
  { title: 'Acciones', key: 'acciones', sortable: false, width: '10%' },
];

const headersGastos = [
  { title: 'Descripción', key: 'descripcion', width: '60%' },
  { title: 'Importe (€)', key: 'importe', width: '30%' },
  { title: 'Acciones', key: 'acciones', sortable: false, width: '10%' },
];

const totalEfectivo = computed(() => {
  return efectivo.value.reduce((sum, item) => sum + (item.cantidad * item.denominacion), 0) + cambioRemanente.value;
});

const totalGastos = computed(() => {
  return gastos.value.reduce((sum, item) => sum + item.importe, 0);
});

onMounted(async () => {
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva || !empresaActiva.url_servidor) {
    console.error('No se pudo obtener la URL del servidor.');
    return;
  }

  const formData = new FormData();
  formData.append('uid', empresaActiva.uid);

  try {
    const response = await fetch(`${empresaActiva.url_servidor}/api/arqueo/getcambio`, {
      method: 'POST',
      body: formData,
    });

    if (response.ok) {
      const data = await response.json();
      cambio.value = data.cambio;
      cambioRemanente.value = data.cambio;
      hayArqueo.value = data.hay_arqueo !== undefined ? data.hay_arqueo : true;
      
      // Mostrar advertencia si no hay arqueo posible
      if (!hayArqueo.value) {
        mostrarAdvertenciaArqueo.value = true;
      }
    } else {
      console.error('Error al obtener cambio:', response.status);
      showSnackbar('Error al cargar los datos del arqueo', 'error');
    }
  } catch (error) {
    console.error('Error de red al obtener cambio:', error);
    showSnackbar('Error de conexión al cargar los datos', 'error');
  }
});

watch(hayArqueo, (newValue) => {
  if (!newValue) {
    mostrarAdvertenciaArqueo.value = true;
  }
});

const modificarCambio = async () => {
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva || !empresaActiva.url_servidor) {
    console.error('No se pudo obtener la URL del servidor.');
    return;
  }

  const formData = new FormData();
  formData.append('uid', empresaActiva.uid);
  formData.append('cambio', cambioRemanente.value.toString());

  try {
    const response = await fetch(`${empresaActiva.url_servidor}/api/arqueo/setcambio`, {
      method: 'POST',
      body: formData,
    });

    if (response.ok) {
      cambio.value = cambioRemanente.value;
      showSnackbar('Cambio modificado exitosamente', 'success');
    } else {
      showSnackbar('Error al modificar el cambio', 'error');
    }
  } catch (error) {
    showSnackbar('Error de conexión al modificar el cambio', 'error');
  }
};

const abrirDialogCambio = () => {
  nuevoCambio.value = cambioRemanente.value;
  dialogCambio.value = true;
};

const confirmarCambio = async () => {
  cambioRemanente.value = nuevoCambio.value;
  await modificarCambio();
  dialogCambio.value = false;
};

const agregarEfectivo = () => {
  if (nuevoEfectivo.value.denominacion > 0 && nuevoEfectivo.value.cantidad > 0) {
    efectivo.value.push({ ...nuevoEfectivo.value });
    nuevoEfectivo.value = { denominacion: 0, cantidad: 0 };
    dialogEfectivo.value = false;
    showSnackbar('Efectivo agregado correctamente', 'success');
  }
};

const agregarGasto = () => {
  if (nuevoGasto.value.descripcion && nuevoGasto.value.importe > 0) {
    gastos.value.push({ ...nuevoGasto.value });
    nuevoGasto.value = { descripcion: '', importe: 0 };
    dialogGastos.value = false;
    showSnackbar('Gasto agregado correctamente', 'success');
  }
};

const eliminarEfectivo = (item: { denominacion: number; cantidad: number }) => {
  const index = efectivo.value.findIndex(
    (ef) => ef.denominacion === item.denominacion && ef.cantidad === item.cantidad
  );
  if (index !== -1) {
    efectivo.value.splice(index, 1);
    showSnackbar('Efectivo eliminado', 'info');
  }
};

const eliminarGasto = (item: { descripcion: string; importe: number }) => {
  const index = gastos.value.findIndex(
    (gasto) => gasto.descripcion === item.descripcion && gasto.importe === item.importe
  );
  if (index !== -1) {
    gastos.value.splice(index, 1);
    showSnackbar('Gasto eliminado', 'info');
  }
};

const arquearCaja = async () => {
  loadingArqueo.value = true;
  
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva || !empresaActiva.url_servidor) {
    console.error('No se pudo obtener la URL del servidor.');
    loadingArqueo.value = false;
    return;
  }

  const formData = new FormData();
  formData.append('uid', empresaActiva.uid);
  formData.append('efectivo', totalEfectivo.value.toString());
  formData.append('cambio', cambioRemanente.value.toString());
  formData.append('gastos', totalGastos.value.toString());
  formData.append('des_efectivo', JSON.stringify(efectivo.value));
  formData.append('des_gastos', JSON.stringify(gastos.value));

  try {
    const response = await fetch(`${empresaActiva.url_servidor}/api/arqueo/arquear`, {
      method: 'POST',
      body: formData,
    });

    if (response.ok) {
      showSnackbar('Arqueo realizado exitosamente', 'success');
      setTimeout(() => {
        router.push('/');
      }, 1500);
    } else {
      showSnackbar('Error al realizar el arqueo', 'error');
    }
  } catch (error) {
    showSnackbar('Error de conexión al realizar el arqueo', 'error');
  } finally {
    loadingArqueo.value = false;
  }
};

const cancelar = () => {
  router.push('/');
};
</script>

<style scoped>
.main-window {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.content-container {
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  height: calc(100vh - 200px);
}

.card-full-height {
  height: calc(100vh - 250px);
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  overflow: hidden;
}

/* --- MEJORA: Hace que el contenido de la tarjeta (las tablas) ocupe el espacio restante --- */
.card-full-height > .v-card-text {
  flex: 1 1 auto;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.card-title {
  padding: 16px;
  font-size: 1.25rem;
  color: white;
}

.bg-primary {
  background-color: #42a5f5 !important;
}

.bg-secondary {
  background-color: #ffca28 !important;
}

/* --- MEJORA: Contenedor de la tabla para que el scroll funcione --- */
.table-container {
  flex-grow: 1; /* Ocupa el espacio disponible */
  overflow-y: auto; /* Scroll automático */
  scrollbar-width: thin;
  scrollbar-color: #888 #f1f1f1;
}

.table-header {
  background-color: #f5f5f5;
  border-bottom: 2px solid #e0e0e0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-row {
  display: flex;
  align-items: center;
  padding: 0;
}

/* --- MEJORA: Alineación de cabeceras --- */
.header-cell {
  padding: 16px 12px;
  font-weight: 600;
  font-size: 1.1rem;
  color: #333;
  border-right: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center; /* Centrado por defecto */
}

.header-cell:last-child {
  border-right: none;
}

/* --- MEJORA: Scrollbar estilo Chrome --- */
.table-container::-webkit-scrollbar {
  width: 12px;
}

.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 6px;
  margin: 4px;
}

.table-container::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 6px;
  border: 2px solid #f1f1f1;
}

.table-container::-webkit-scrollbar-thumb:hover {
  background: #555;
}

.table-text {
  font-size: 1.4rem;
  font-weight: 500;
}

/* --- MEJORA: Alineación del texto en las celdas --- */
.item-text {
  display: block;
  width: 100%;
  padding: 0 12px;
  text-align: center; /* Centrado por defecto */
}

/* --- MEJORA: Clase para alinear a la derecha --- */
.text-right {
  text-align: right !important;
  justify-content: flex-end !important; /* Para las cabeceras flex */
}


.data-table :deep(.v-data-table__tr) {
  border-bottom: 1px solid #e0e0e0;
}

.data-table :deep(.v-data-table-rows-no-data) {
  padding: 20px;
}

.data-table :deep(.v-data-table__td) {
  padding: 16px 0px !important; /* Ajustamos padding para que la clase interna lo controle */
  border-bottom: 1px solid #f0f0f0;
}

.action-btn-small {
  margin: 0;
}

.dialog-card {
  border-radius: 16px;
  overflow: hidden;
}

.dialog-title {
  padding: 16px;
  font-size: 1.5rem;
  color: #333;
}

.dialog-field :deep(.v-field) {
  border-radius: 8px;
  font-size: 1.2rem;
}

.dialog-field :deep(.v-field__input) {
  font-size: 1.2rem;
  padding: 16px 12px;
}

.dialog-field :deep(.v-label) {
  font-size: 1.1rem;
}

.dialog-btn {
  margin: 0;
  font-weight: 600;
  font-size: 1.1rem;
}

.action-btn {
  padding: 8px 16px;
  border-radius: 8px;
}

.banner-warning {
  margin: 16px 0;
  border-radius: 8px;
}

/* --- MEJORA: Texto del cambio más grande --- */
.cambio-text {
  font-size: 1.4rem; /* Aumentado */
  font-weight: 500;
  color: white;
}

.edit-btn {
  color: white;
  background-color: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
}
</style>