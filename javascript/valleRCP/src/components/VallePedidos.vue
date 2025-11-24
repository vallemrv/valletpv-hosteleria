<template>
  <v-dialog :model-value="show" @update:model-value="salir" fullscreen transition="dialog-bottom-transition">
    <v-card class="h-100 rounded-0 d-flex flex-column">
      <v-toolbar color="primary" class="flex-grow-0">
        <v-btn icon @click="salir()">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>Pedidos Servidos</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-chip>{{ totalPedidosServidos }} pedidos</v-chip>
      </v-toolbar>
      
      <v-card-text class="flex-grow-1 overflow-y-auto pa-3 pb-16 bg-grey-lighten-3">
        <!-- Lista plana de pedidos servidos -->
        <v-row>
          <v-col 
            v-for="pedido in vistaDatos" 
            :key="pedido.pedido_id" 
            cols="12" 
            sm="6" 
            md="4"
            lg="3"
          >
            <v-card elevation="1" class="h-100 bg-grey-lighten-4">
              <v-card-title class="pa-2 bg-success-lighten-1">
                <div class="d-flex flex-column w-100">
                  <div class="d-flex justify-space-between align-center">
                    <span>
                      <v-icon start color="white">mdi-check-circle</v-icon>
                      Mesa {{ pedido.mesa || pedido.pedido_id }}
                    </span>
                    <span class="text-caption">{{ pedido.hora }}</span>
                  </div>
                  <div class="text-caption mt-1">
                    <v-icon size="small" class="mr-1">mdi-account</v-icon>
                    {{ pedido.camarero || 'Sin camarero' }}
                  </div>
                </div>
              </v-card-title>
                    
                    <v-divider></v-divider>
                    
                    <!-- Artículos servidos -->
                    <v-card-text class="pa-2">
                      <v-list dense class="pa-0 bg-transparent">
                        <v-list-item 
                          v-for="articulo in pedido.articulosArray" 
                          :key="`${pedido.pedido_id}-${articulo.idart}`"
                          class="pa-1"
                        >
                          <template v-slot:prepend>
                            <v-chip color="success" size="small" class="mr-2">
                              {{ articulo.cantidad }}
                            </v-chip>
                          </template>
                          
                          <v-list-item-title class="text-decoration-line-through">
                            {{ articulo.descripcion }}
                          </v-list-item-title>
                          
                          <v-list-item-subtitle class="text-caption text-success">
                            Todas servidas
                          </v-list-item-subtitle>
                        </v-list-item>
                      </v-list>
                    </v-card-text>
                    
                    <v-divider></v-divider>
                    
                    <!-- Acción: Recuperar pedido (marcar como no servido) -->
                    <v-card-actions class="pa-2">
                      <v-btn 
                        @click="recuperarPedido(pedido)" 
                        color="warning" 
                        variant="tonal"
                        size="small"
                        block
                      >
                        <v-icon start>mdi-undo</v-icon>
                        Recuperar
                      </v-btn>
              </v-card-actions>
            </v-card>
          </v-col>
        </v-row>
        
        <!-- Mensaje si no hay pedidos servidos -->
        <v-card v-if="!vistaDatos || vistaDatos.length === 0" class="text-center pa-8">
          <v-icon size="64" color="grey">mdi-food-off</v-icon>
          <p class="text-h6 mt-4">No hay pedidos servidos</p>
        </v-card>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { useMainStore } from '@/stores/main'

const props = defineProps({
  show: Boolean,
  receptor: {
    type: [String, Array],
    default: null
  }
})

const emit = defineEmits(['close'])

const store = useMainStore()

// Vista de pedidos servidos agrupada
const vistaDatos = computed(() => store.vistaPrincipalServidos(props.receptor))

// Total de pedidos servidos
const totalPedidosServidos = computed(() => {
  return vistaDatos.value ? vistaDatos.value.length : 0
})

// Recuperar pedido: marcar todas sus líneas como no servidas
const recuperarPedido = async (pedido) => {
  const idsLineas = []
  
  pedido.articulosArray.forEach(articulo => {
    articulo.lineas.forEach(linea => {
      idsLineas.push(linea.id)
    })
  })
  
  if (idsLineas.length > 0) {
    await store.desmarcarServido(idsLineas)
  }
}

const salir = () => {
  emit('close')
}
</script>

<style scoped>
.h-100 {
  height: 100%;
}

.bg-success-lighten-1 {
  background-color: #81C784 !important;
}
</style>
