<template>
  <UiMainWindows title="Empresas" icon="mdi-domain">
    <template #actions>
      <UiActionButton icon="mdi-plus" @click="abrirDialogoCreacion">Añadir empresa</UiActionButton>
      <UiActionButton :disabled="!hayEmpresasActivas" icon="mdi-cash-register" @click="goHome">Ir al TPV</UiActionButton>
    </template>
    <div class="main-container">
      <v-alert
        v-if="empresas.length === 0"
        type="info"
        color="primary"
        elevation="8"
        rounded="xl"
        class="my-6 text-center"
        icon="mdi-information"
      >
        <span style="font-size:1.3rem;font-weight:500;">
          No hay empresas creadas.<br>
          Para poder usar el TPV, primero debes crear al menos una empresa.
        </span>
      </v-alert>
      <v-row v-if="empresas.length > 0" class="mt-4" dense>
        <v-col v-for="empresa in empresas" :key="empresa.id" cols="6" md="6">
          <UiBotonInfoEmpresa
            :empresa="empresa"
            @editar="onEditarEmpresa"
            @borrar="onBorrarEmpresa"
            @click="onSeleccionarEmpresa(empresa)"
          />
        </v-col>
      </v-row>
    </div>
 

  <UiDialogScaffold
    v-model="showDialog"
    icon="mdi-domain-plus"
    :title="dialogTitle"
    :actions="dialogActions"
    @on_action="handleDialogAction"
    :width="'80vw'"
    style="max-width:1200px;"
  >
    <v-form ref="formRef" v-model="formValid">
      <v-row>
        <v-col cols="12" md="12">
          <v-text-field
            v-model="form.url_servidor"
            label="URL"
            required
            class="mb-2"
            :disabled="false"
            style="font-size:1.1rem;"
          >
            <template #append>
              <UiActionButton
                icon="mdi-link"
                @click="connectUid"
                :disabled="!form.url_servidor"
                style="margin-left:8px;"
              />
            </template>
          </v-text-field>
        </v-col>
        <v-col cols="6" md="6">
          <v-text-field 
            v-model="form.nombre" 
            label="Nombre" 
            required 
            :disabled="!servidorVerificado"
            style="font-size:1.1rem;"
          />
        </v-col>
        <v-col cols="6" md="6">
          <v-text-field
            v-model="form.descripcion"
            label="Descripción (Alias)"
            required
            :disabled="!servidorVerificado"
            style="font-size:1.1rem;"
          />
        </v-col>
        <v-col cols="12" md="6">
          <v-switch 
            v-model="form.usa_cash_keeper" 
            label="Usa Cash Keeper" 
            :disabled="!servidorVerificado"
            color="success"
            hide-details
            density="comfortable"
            class="elegant-switch"
          />
          <v-spacer style="height: 32px; display: block;" />
          <v-switch 
            v-model="form.usa_pinpad" 
            label="Usa Pinpad" 
            :disabled="!servidorVerificado"
            color="success"
            hide-details
            density="comfortable"
            class="elegant-switch"
          />
          <v-spacer style="height: 16px; display: block;" />
        </v-col>
        <v-col cols="12" md="6">
          <v-text-field v-model="form.url_cash_keeper" label="URL Cash Keeper" :disabled="!servidorVerificado || !form.usa_cash_keeper" />
          <v-spacer style="height: 16px; display: block;" />
          <v-text-field v-model="form.url_pinpad" label="URL Pinpad" :disabled="!servidorVerificado || !form.usa_pinpad" />
          <div v-if="form.uid" class="mt-2">
            <v-chip color="primary">UID: {{ form.uid }}</v-chip>
          </div>
        </v-col>
      </v-row>
    </v-form>
  </UiDialogScaffold>
  
  <UiSnackbar 
    v-model="snackbar.show" 
    :message="snackbar.text"
    :type="snackbar.type"
    :timeout="3500"
  />
  
  </UiMainWindows>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useEmpresasStore, Empresa } from 'valle-tpv-lib';


const showDialog = ref(false);
const empresasStore = useEmpresasStore();

const router = useRouter();
function goHome() {
  router.replace({ name: 'CamarerosHome' });
}

const formValid = ref(false);
const formRef = ref();

const form = ref<Empresa>({
  id: 0,
  nombre: '',
  url_servidor: '',
  descripcion: '',
  activa: 1,
  usa_cash_keeper: 0,
  url_cash_keeper: '',
  usa_pinpad: 0,
  url_pinpad: '',
  uid: '',
  validate: () => true
});

const empresas = computed(() => empresasStore.items);
const hayEmpresasActivas = computed(() => empresasStore.empresaActiva);

onMounted(() => {
  empresasStore.initStore();
});

const dialogTitle = ref('Añadir empresa');
const isEditMode = ref(false);
const aliasOriginal = ref(''); // Para detectar cambios en el alias al editar
const servidorVerificado = ref(false); // Para habilitar campos después de verificar servidor

const snackbar = ref({ show: false, text: '', type: 'info' as 'info' | 'success' | 'warning' | 'error' });
function showSnackbar(text: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') {
  snackbar.value.text = text;
  snackbar.value.type = type;
  snackbar.value.show = true;
}

const dialogActions = [
  { id: 'cancel', text: 'Cancelar', icon: 'mdi-close', color: 'grey' },
  { id: 'save', text: 'Guardar', icon: 'mdi-content-save', color: 'primary', disabled: computed(() => !servidorVerificado.value) }
];

async function connectUid() {
  if (!form.value.url_servidor) return;
  
  try {
    // Primero verificar que el servidor esté disponible con /api/health
    const healthRes = await fetch(`${form.value.url_servidor}/api/health`);
    const healthData = await healthRes.json();
    
    if (!healthData.success || healthData.status !== 'ok') {
      showSnackbar('El servidor no está disponible o la URL es incorrecta', 'error');
      servidorVerificado.value = false;
      return;
    }
    
    servidorVerificado.value = true;
    showSnackbar('Servidor verificado correctamente', 'success');
    
  } catch (e) {
    servidorVerificado.value = false;
    showSnackbar('Error al verificar el servidor. Verifica la URL', 'error');
  }
}

async function handleDialogAction(actionId: string) {
  if (actionId === 'cancel') {
    showDialog.value = false;
    limpiarDialogo();
    return;
  }
  if (actionId === 'save') {
    if (!form.value.descripcion || !form.value.url_servidor) {
      showSnackbar('Faltan datos obligatorios (URL y descripción)', 'error');
      return;
    }
    
    try {
      // Caso 1: Empresa nueva (sin UID)
      if (!form.value.uid) {
        // Llamar a create_uid enviando el alias
        const fd = new FormData();
        fd.append('alias', form.value.descripcion);
        
        const resCreateUid = await fetch(`${form.value.url_servidor}/api/dispositivo/create_uid`, {
          method: 'POST',
          body: fd
        });
        const dataCreateUid = await resCreateUid.json();
        
        if (!dataCreateUid.uid) {
          showSnackbar('No se pudo crear el UID en el servidor', 'error');
          return;
        }
        
        form.value.uid = dataCreateUid.uid;
        
        // Crear nueva empresa
        const nuevaEmpresa = new Empresa({
          id: Date.now(),
          nombre: form.value.nombre || form.value.descripcion,
          descripcion: form.value.descripcion,
          url_servidor: form.value.url_servidor,
          uid: form.value.uid,
          activa: 1,
          usa_cash_keeper: form.value.usa_cash_keeper,
          url_cash_keeper: form.value.url_cash_keeper,
          usa_pinpad: form.value.usa_pinpad,
          url_pinpad: form.value.url_pinpad
        });
        empresasStore.insert(nuevaEmpresa);
        empresasStore.setEmpresaActiva(nuevaEmpresa.id);
        showSnackbar('Empresa creada correctamente', 'success');
      } 
      // Caso 2: Modificar empresa existente
      else {
        // Si se modificó el alias, llamar a set_alias
        if (form.value.descripcion !== aliasOriginal.value) {
          const fd = new FormData();
          fd.append('uid', form.value.uid);
          fd.append('alias', form.value.descripcion);
          
          await fetch(`${form.value.url_servidor}/api/dispositivo/set_alias`, {
            method: 'POST',
            body: fd
          });
        }
        
        // Actualizar empresa existente
        const empresaActualizada = new Empresa({
          id: form.value.id,
          nombre: form.value.nombre || form.value.descripcion,
          descripcion: form.value.descripcion,
          url_servidor: form.value.url_servidor,
          uid: form.value.uid,
          activa: form.value.activa,
          usa_cash_keeper: form.value.usa_cash_keeper,
          url_cash_keeper: form.value.url_cash_keeper,
          usa_pinpad: form.value.usa_pinpad,
          url_pinpad: form.value.url_pinpad
        });
        empresasStore.update(empresaActualizada);
        showSnackbar('Empresa actualizada correctamente', 'success');
      }
    
      showDialog.value = false;
      limpiarDialogo();

    } catch (e) {
      console.error('Error al guardar la empresa:', e);
      showSnackbar('Error al guardar la empresa', 'error');
    }
  }
}

function limpiarDialogo() {
  form.value = new Empresa({
    nombre: '',
    url_servidor: '',
    descripcion: '',
    activa: 1, 
    usa_cash_keeper: 0,
    url_cash_keeper: '',
    usa_pinpad: 0,
    url_pinpad: '',
    uid: '',
  });
  isEditMode.value = false;
  aliasOriginal.value = '';
  servidorVerificado.value = false;
  dialogTitle.value = 'Añadir empresa';
}

function abrirDialogoCreacion() {
  limpiarDialogo();
  showDialog.value = true;
}

function onEditarEmpresa(empresa: any) {
  
  form.value = new Empresa({
    id: empresa.id,
    nombre: empresa.nombre || empresa.descripcion,
    url_servidor: empresa.url_servidor || empresa.url,
    descripcion: empresa.descripcion,
    uid: empresa.uid || '',
    activa: empresa.activa || 1,
    usa_cash_keeper: empresa.usa_cash_keeper || false,
    url_cash_keeper: empresa.url_cash_keeper || '',
    usa_pinpad: empresa.usa_pinpad || false,
    url_pinpad: empresa.url_pinpad || '',
  });
  
  aliasOriginal.value = empresa.descripcion; // Guardar alias original para detectar cambios
  servidorVerificado.value = true; // En modo edición, el servidor ya fue verificado
  isEditMode.value = true;
  dialogTitle.value = 'Editando empresa';
  showDialog.value = true;
}

function onBorrarEmpresa(empresa: any) {
  const eraActiva = empresa.activa === 1;
  
  empresasStore.rm(empresa.id);
  
  if (eraActiva) {
    showSnackbar('Empresa activa borrada - WebSocket desconectado', 'warning');
  } else {
    showSnackbar('Empresa borrada', 'success');
  }
}

function onSeleccionarEmpresa(empresa: any) {
  empresasStore.setEmpresaActiva(empresa.id);
  showSnackbar(`Empresa activa: ${empresa.nombre || empresa.descripcion} - WebSocket conectando...`, 'success');
}

</script>

<style scoped>
.main-container {
  height: 100%;
  width: 100%;
  padding: 24px;
  box-sizing: border-box;
}

.flex-grow-1 {
  flex-grow: 1;
}

/* Estilos para switches elegantes */
.elegant-switch {
  --v-input-control-height: 48px;
}

.elegant-switch :deep(.v-switch__track) {
  width: 64px;
  height: 34px;
  border-radius: 17px;
  opacity: 0.5;
  transition: background-color 0.3s ease;
}

.elegant-switch :deep(.v-switch__thumb) {
  width: 28px;
  height: 28px;
  border-radius: 14px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  transition: transform 0.3s ease;
}

.elegant-switch :deep(.v-label) {
  font-size: 1.1rem;
  font-weight: 500;
}
</style>
