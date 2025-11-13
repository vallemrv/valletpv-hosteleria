<template>
  <div class="botonera-container">
    <div 
      v-for="tecla in productosActuales" 
      :key="tecla.ID" 
      class="producto-slot"
    >
      <UiBotonTecla
        :nombre="tecla.nombre"
        :precio="precioAMostrar(tecla)"
        :bloqueada="tecla.hay_existencias === 0"
        @tecla-click="onProductoClick(tecla)"
        @bloqueo-changed="mostrarConfirmacionBloqueo(tecla, $event)"
      />
    </div>

    <!-- Diálogo de confirmación para bloquear/desbloquear -->
    <v-dialog
      v-model="dialogConfirmacion"
      max-width="500px"
      persistent
    >
      <v-card>
        <v-card-title class="text-h5 bg-warning">
          <v-icon left>mdi-alert</v-icon>
          Confirmar {{ accionBloqueo }}
        </v-card-title>
        
        <v-card-text class="pt-4">
          <p class="text-body-1">
            ¿Está seguro de que desea <strong>{{ accionBloqueo }}</strong> el producto?
          </p>
          <p class="text-body-2 text-medium-emphasis">
            <strong>{{ teclaSeleccionada?.nombre }}</strong>
          </p>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="grey"
            variant="text"
            @click="cancelarBloqueo"
          >
            Cancelar
          </v-btn>
          <v-btn
            :color="nuevoBloqueado ? 'error' : 'success'"
            variant="elevated"
            @click="confirmarBloqueo"
          >
            {{ nuevoBloqueado ? 'Bloquear' : 'Desbloquear' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
// Importamos el botón de producto que me has pasado
import {  computed, ref } from 'vue';
import UiBotonTecla from '../botones/UiBotonTecla.vue';
import { useTeclasStore } from '../../store/dbStore/teclasStore';
import { useMesasStore } from '../../store/dbStore/mesasStore';
import { useSeccionesStore } from '../../store/dbStore/seccionesStore';
import { useEmpresasStore } from '../../store/dbStore/empresasStore';
import  Tecla  from '../../models/tecla';

// Variables para el diálogo de confirmación
const dialogConfirmacion = ref(false);
const teclaSeleccionada = ref<Tecla | null>(null);
const nuevoBloqueado = ref(false);
const accionBloqueo = computed(() => nuevoBloqueado.value ? 'bloquear' : 'desbloquear');



const teclasStore = useTeclasStore();
const mesasStore = useMesasStore();
const seccionesStore = useSeccionesStore();
const empresasStore = useEmpresasStore();

const seccionSeleccionada = computed(() => seccionesStore.seccSel);
const productos = computed(() => teclasStore.teclasPorSec(seccionSeleccionada.value?.id || 0));
const productosPorPadre = computed(() => teclasStore.teclasPorTeclaPadre);
const productosFiltrados = computed(() => teclasStore.teclasFiltradasPorTexto);


const tarifa = computed(() => mesasStore.mesaSel?.Tarifa);
const tiposProductos = computed(() => teclasStore.tipo || "seccion");
const precioAMostrar = (tecla: Tecla) => {
  if (tecla.tipo == "CM") {
    return 0;
  }
    return tarifa.value === 2 ? tecla.p2 : tecla.p1;
};


// Computed para productos actuales
const productosActuales = computed(() => {
    if (tiposProductos.value === "seccion") {
      return productos.value.slice(0, 18); // 3x6 = 18 productos máximo
    } else if (tiposProductos.value === "hijos") {
      return productosPorPadre.value.slice(0, 18);
    } else {
      return productosFiltrados.value.slice(0, 18);
    }
});

const emit = defineEmits<{
  productoClick: [producto: Tecla];
}>();

// Función para mostrar el diálogo de confirmación
function mostrarConfirmacionBloqueo(tecla: Tecla, bloqueado: boolean) {
  teclaSeleccionada.value = tecla;
  nuevoBloqueado.value = bloqueado;
  dialogConfirmacion.value = true;
}

// Función para cancelar el bloqueo
function cancelarBloqueo() {
  dialogConfirmacion.value = false;
  teclaSeleccionada.value = null;
}

// Función para confirmar el bloqueo/desbloqueo
async function confirmarBloqueo() {
  if (!teclaSeleccionada.value) return;
  
  await onBloqueoChanged(teclaSeleccionada.value, nuevoBloqueado.value);
  
  dialogConfirmacion.value = false;
  teclaSeleccionada.value = null;
}

async function onBloqueoChanged(tecla: Tecla, bloqueado: boolean) {
  const hay_existencias = bloqueado ? 0 : 1;
  tecla.hay_existencias = hay_existencias;
  await teclasStore.update(tecla);

  // Llamada al servidor
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva?.uid || !empresaActiva?.url_servidor) {
    console.error('No hay una empresa activa configurada correctamente.');
    return;
  }

  try {
    const formData = new FormData();
    formData.append('IDTecla', tecla.ID.toString());
    formData.append('uid', empresaActiva.uid);

    // Determinar el endpoint según el estado de bloqueo
    const endpoint = bloqueado ? 'agregar' : 'borrar';
    const url = `${empresaActiva.url_servidor}/api/articulos/${endpoint}`;

    const response = await fetch(url, {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      throw new Error(`Error del servidor: ${response.status} ${response.statusText}`);
    }

    
  } catch (error) {
    console.error('Error al actualizar el artículo en el servidor:', error);
    // Revertir el cambio local si falla la sincronización
    tecla.hay_existencias = bloqueado ? 1 : 0;
    await teclasStore.update(tecla);
  }
}

// Función para manejar el evento de clic en un producto
function onProductoClick(producto: Tecla) {
    emit("productoClick", producto);
  }
</script>

<style scoped>
/* La clave para que todo funcione como pides */
.botonera-container {
  /* 1. Asegura que el contenedor llene a su padre (el "match_parent") */
  width: 100%;
  height: 100%;
  
  /* 2. La magia para la rejilla: usamos CSS Grid */
  display: grid;
  
  /* 3. Definimos las 3 columnas y 6 filas */
  grid-template-columns: repeat(3, 1fr); /* Tres columnas de igual tamaño flexible */
  grid-template-rows: repeat(6, 1fr);    /* Seis filas de igual tamaño flexible */
  
  /* 4. Un pequeño espacio entre los botones */
  gap: 16px;
  
  box-sizing: border-box; /* Importante para que el padding no desborde el 100% */
  
}

.producto-slot {
  /* Hacemos que el componente UiBotonTecla se estire para
     ocupar el 100% de su celda en la rejilla */
  display: flex;
  align-items: stretch;
  justify-content: stretch;
}
</style>