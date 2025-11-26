<template>
  <v-container fluid>
    <!-- Pedidos ordenados por ID -->
    <v-row>
      <v-col 
        v-for="pedido in vistaDatos" 
        :key="pedido.pedido_id" 
        cols="12" 
        sm="6" 
        md="4"
        lg="3"
      >
              <v-card 
                elevation="2" 
                :class="{'border-urgent': esUrgente(pedido)}"
                class="h-100 d-flex flex-column"
              >
                <v-card-title class="pa-4" :class="esUrgente(pedido) ? 'bg-error text-white' : 'bg-grey-lighten-3'">
                  <div class="d-flex flex-column w-100">
                    <div class="d-flex justify-space-between align-center">
                      <span class="text-h4 font-weight-bold">
                        <v-icon v-if="esUrgente(pedido)" start size="x-large">mdi-alert</v-icon>
                        Mesa {{ pedido.mesa || pedido.pedido_id }}
                      </span>
                      <span class="text-h6">{{ pedido.hora }}</span>
                    </div>
                    <div class="text-h6 mt-2">
                      <v-icon size="large">mdi-account</v-icon>
                      {{ pedido.camarero || 'Sin camarero' }}
                    </div>
                  </div>
                </v-card-title>
                
                <v-divider></v-divider>
                
                <!-- Artículos agrupados -->
                <v-card-text class="pa-4 flex-grow-1">
                  <v-row 
                    v-for="articulo in pedido.articulosArray" 
                    :key="`${pedido.pedido_id}-${articulo.idart}-${articulo.descripcion}`"
                    class="mb-3 articulo-row"
                    @click="toggleLineasArticulo(articulo)"
                  >
                    <v-col cols="auto" class="pr-0">
                      <div class="cantidad-box">
                        {{ articulo.cantidad }}
                      </div>
                    </v-col>
                    <v-col>
                      <div class="text-h5 font-weight-bold mb-1" :class="{'text-decoration-line-through': articulo.pendientes === 0}">
                        {{ articulo.descripcion }}
                        <v-icon v-if="articulo.lineas.some(l => l.urgente)" color="error" size="large">
                          mdi-alert-circle
                        </v-icon>
                      </div>
                      <div class="text-h6">
                        <span v-if="articulo.servidas > 0" class="text-success">
                          {{ articulo.servidas }} servidas
                        </span>
                        <span v-if="articulo.pendientes > 0" class="text-warning ml-2">
                          {{ articulo.pendientes }} pendientes
                        </span>
                      </div>
                    </v-col>
                  </v-row>
                </v-card-text>
                
                <v-divider></v-divider>
                
                <!-- Acciones del pedido -->
                <v-card-actions class="pa-4">
                  <v-btn 
                    @click="servirPedido(pedido)" 
                    color="success" 
                    variant="tonal"
                    size="x-large"
                    block
                    class="text-h6"
                  >
                    <v-icon start size="large">mdi-check</v-icon>
                    Servir todo
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-col>
    </v-row>
    
    <!-- Mensaje si no hay pedidos -->
    <v-card v-if="!vistaDatos || vistaDatos.length === 0" class="text-center pa-16">
      <v-icon size="128" color="grey">mdi-food-off</v-icon>
      <p class="text-h3 mt-8">No hay pedidos pendientes</p>
    </v-card>
  </v-container>
</template>

<script setup>
import { computed } from 'vue'
import { useMainStore } from '@/stores/main'

const props = defineProps({
  receptor: {
    type: [String, Array],
    default: null
  }
})

const store = useMainStore()

// Vista principal agrupada
const vistaDatos = computed(() => store.vistaPrincipal(props.receptor))

// Verificar si un pedido tiene líneas urgentes
const esUrgente = (pedido) => {
  return pedido.articulosArray?.some(art => 
    art.lineas.some(l => l.urgente === true)
  )
}

// Toggle de líneas de un artículo (marcar/desmarcar como servidas individualmente)
const toggleLineasArticulo = async (articulo) => {
  // Si hay líneas pendientes, marcar todas como servidas
  // Si todas están servidas, desmarcar todas
  const idsPendientes = articulo.lineas
    .filter(l => !l.servido)
    .map(l => l.id)
  
  if (idsPendientes.length > 0) {
    await store.servirLineas(idsPendientes)
  } else {
    // Todas están servidas, desmarcar
    const idsServidas = articulo.lineas.map(l => l.id)
    await store.desmarcarServido(idsServidas)
  }
}

// Servir todas las líneas de un pedido
const servirPedido = async (pedido) => {
  const idsLineas = []
  
  pedido.articulosArray.forEach(articulo => {
    articulo.lineas.forEach(linea => {
      if (!linea.servido) {
        idsLineas.push(linea.id)
      }
    })
  })
  
  if (idsLineas.length > 0) {
    await store.servirLineas(idsLineas)
  }
}
</script>

<style scoped>
.border-urgent {
  border: 3px solid #C62828 !important;
  animation: pulse-urgent 2s infinite;
}

@keyframes pulse-urgent {
  0%, 100% {
    box-shadow: 0 0 10px rgba(198, 40, 40, 0.5);
  }
  50% {
    box-shadow: 0 0 20px rgba(198, 40, 40, 0.8);
  }
}

.h-100 {
  height: 100%;
}

.cantidad-box {
  background-color: black;
  color: white;
  font-size: 2rem;
  font-weight: 900;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.articulo-row {
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.articulo-row:hover {
  background-color: rgba(0, 0, 0, 0.05);
}
</style>
