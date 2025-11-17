<template>
  <v-toolbar color="#cfb6d4">
    <v-toolbar-title>
      Receptor <span v-if="empresa">{{ empresa.nombre }}</span>
    </v-toolbar-title>
    <v-spacer></v-spacer>
    <v-btn icon @click="() => (showSelDialog = true)">
      <v-icon>mdi-list-status</v-icon></v-btn
    >
    <v-btn icon @click="() => (showDialog = true)"> <v-icon>mdi-cog</v-icon></v-btn>
  </v-toolbar>
  
  <v-container fluid class="pb-16">
    <!-- Componente de estado de conexión -->
    <connection-status 
      v-if="serverUrl"
      :is-ws-connected="isWsConnected"
      :is-http-connected="isHttpConnected"
      :is-secure="useHttps"
    />
    
    <!-- Vista principal agrupada -->
    <vista-principal :receptor="receptorActual" />
  </v-container>

  <v-btn
      @click="abrirServidos"
      icon
      size="large"
      color="success"
      location="bottom right"
      position="fixed"
      class="fab-servidos"
    >
      <v-icon>mdi-check-all</v-icon>
    </v-btn>

  <v-dialog v-model="showDialog" max-width="500" persistent>
    <v-card title="Configuración del Servidor">
      <v-card-text>
        <v-row>
          <v-col cols="12">
            <v-text-field 
              v-model="empresaNombre" 
              label="Nombre de la Empresa" 
              placeholder="Mi Empresa"
              hide-details="auto"
              required>
            </v-text-field>
          </v-col>
          <v-col cols="12">
            <v-text-field 
              v-model="deviceAlias" 
              label="Alias del Dispositivo" 
              placeholder="Receptor-1"
              hint="Identificador único de este dispositivo"
              persistent-hint
              hide-details="auto"
              required>
            </v-text-field>
          </v-col>
          <v-col cols="12">
            <v-text-field 
              v-model="server" 
              label="Dirección del servidor" 
              placeholder="ejemplo.com:8000 o https://ejemplo.com:8000"
              hint="Puedes incluir http:// o https://, si no se especifica se usará http://"
              persistent-hint
              hide-details="auto"
              required>
            </v-text-field>
          </v-col>
          <v-col cols="12" v-if="serverError">
            <v-alert type="error" variant="tonal">
              {{ serverError }}
            </v-alert>
          </v-col>
        </v-row>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="showDialog = false">Cancelar</v-btn>
        <v-btn @click="server_change()" color="primary" :loading="isConnecting">Conectar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>  <v-dialog v-model="showSelDialog" max-width="400" persistent>
    <v-card title="Selecionar receptor">
      <v-card-text>
        <v-row class="pa-4">
          <v-col class="pa-0" cols="12" v-for="(r, i) in receptores_mod" :key="i">
            {{ r.Nombre }}
            <v-switch
              v-model="r.is_sel"
              @change="on_change(r)"
              class="float-right"
              hide-details="auto"
              color="success"
            ></v-switch>
          </v-col>
        </v-row>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="showSelDialog = false">Cancelar</v-btn>
        <v-btn @click="receptores_change()" color="primary">Aceptar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <valle-pedidos 
    @close="() => (showPedidos = false)" 
    :show="showPedidos"
    :receptor="receptorActual"
  ></valle-pedidos>
</template>

<script>
import VWebsocket from "@/websocket";
import { useMainStore } from "@/stores/main";
import { computed, onMounted, ref, watch } from 'vue';
import VistaPrincipal from "@/components/VistaPrincipal.vue";
import VallePedidos from "@/components/VallePedidos.vue";
import ConnectionStatus from "@/components/ConnectionStatus.vue";

export default {
  components: { VistaPrincipal, VallePedidos, ConnectionStatus },
  setup() {
    const store = useMainStore();
    
    // Reactive data
    const showSelDialog = ref(false);
    const showDialog = ref(false);
    const server = ref("");
    const useHttps = ref(false);
    const empresaNombre = ref("");
    const deviceAlias = ref("");
    const serverError = ref("");
    const isConnecting = ref(false);
    const ws = ref([]);
    const receptores_sel = ref([]);
    const showPedidos = ref(false);

    // Computed properties
    const items = computed(() => store.items);
    const isWsConnected = computed(() => store.isWsConnected);
    const isHttpConnected = computed(() => store.isHttpConnected);
    const receptores = computed(() => store.receptores);
    const empresa = computed(() => store.empresa);
    
    // Receptor actual (primer receptor seleccionado o null para ver todos)
    const receptorActual = computed(() => {
      const recs = getReceptores();
      return recs.length > 0 ? recs[0].nomimp : null;
    });
    
    // URLs calculadas
    const serverUrl = computed(() => {
      if (!server.value) return '';
      
      // Si la URL ya tiene protocolo, usarla tal cual
      if (server.value.startsWith('http://') || server.value.startsWith('https://')) {
        return server.value;
      }
      
      // Si no, agregar el protocolo correspondiente
      const protocol = useHttps.value ? 'https://' : 'http://';
      return `${protocol}${server.value}`;
    });
    
    const wsUrl = computed(() => {
      if (!server.value) return '';
      
      let baseUrl = server.value;
      
      // Si la URL tiene protocolo HTTP/HTTPS, reemplazarlo por WS/WSS
      if (baseUrl.startsWith('https://')) {
        return baseUrl.replace('https://', 'wss://') + '/ws/comunicacion/[receptor]';
      } else if (baseUrl.startsWith('http://')) {
        return baseUrl.replace('http://', 'ws://') + '/ws/comunicacion/[receptor]';
      }
      
      // Si no tiene protocolo, usar el protocolo correspondiente
      const protocol = useHttps.value ? 'wss://' : 'ws://';
      return `${protocol}${baseUrl}/ws/comunicacion/[receptor]`;
    });
    
    const receptores_mod = computed(() => {
      receptores.value.forEach((e) => {
        e.is_sel = is_sel(e.ID);
      });
      return receptores.value;
    });

    // Methods
    const is_sel = (id) => {
      return Object.values(receptores_sel.value).includes(id);
    };

    const getReceptores = () => {
      if (receptores_sel.value.length > 0) {
        // Eliminar duplicados de receptores_sel usando Set
        const uniqueIds = [...new Set(receptores_sel.value)];
        
        return Object.values(receptores.value).filter((r) => {
          return uniqueIds.includes(r.ID);
        });
      }
      return [];
    };

    const on_change = (r) => {
      if (r.is_sel) {
        // Solo agregar si no está ya incluido (evitar duplicados)
        if (!receptores_sel.value.includes(r.ID)) {
          receptores_sel.value.push(r.ID);
        }
      } else {
        // Eliminar de la selección
        receptores_sel.value = receptores_sel.value.filter((e) => e != r.ID);

        // Cerrar inmediatamente la conexión WebSocket de este receptor si existe
        try {
          const idx = ws.value.findIndex((w) => w.receptor === r.nomimp);
          if (idx !== -1) {
            ws.value[idx].disconnect();
            ws.value.splice(idx, 1);
          }
        } catch (err) {
          console.warn(`No se pudo cerrar la conexión de ${r.Nombre}:`, err);
        }

        // Persistir el cambio para evitar reconexiones posteriores
        try {
          localStorage.receptores = JSON.stringify(receptores_sel.value);
        } catch (_) {}
      }
      
      // Eliminar duplicados finales
      receptores_sel.value = [...new Set(receptores_sel.value)];
    };

    const receptores_change = () => {
      showSelDialog.value = false;
      localStorage.receptores = JSON.stringify(receptores_sel.value);
      connect();
    };

    const server_change = async () => {
      serverError.value = "";
      isConnecting.value = true;
      
      // Validar campos requeridos
      if (!empresaNombre.value || !deviceAlias.value || !server.value) {
        serverError.value = "Por favor, completa todos los campos";
        isConnecting.value = false;
        return;
      }
      
      // Detectar automáticamente el protocolo de la URL
      let fullServerUrl = server.value.trim();
      let detectedHttps = false;
      
      if (fullServerUrl.startsWith('https://')) {
        detectedHttps = true;
      } else if (fullServerUrl.startsWith('http://')) {
        detectedHttps = false;
      } else {
        // Si no hay protocolo, agregar http:// por defecto
        fullServerUrl = `http://${fullServerUrl}`;
        detectedHttps = false;
      }
      
      // Actualizar el valor de useHttps basándose en la detección
      useHttps.value = detectedHttps;
      
      try {
        // Paso 1: Verificar que el servidor esté disponible llamando a /api/health/
        const healthCheck = await store.checkServerHealth(fullServerUrl);
        
        if (!healthCheck || !healthCheck.success) {
          serverError.value = "No se pudo conectar con el servidor. Verifica la dirección.";
          isConnecting.value = false;
          return;
        }
        
        // Guardar la configuración del servidor
        localStorage.server = fullServerUrl;
        localStorage.useHttps = useHttps.value.toString();
        localStorage.empresaNombre = empresaNombre.value;
        localStorage.deviceAlias = deviceAlias.value;
        
        // Paso 2: Crear/obtener el UID del dispositivo con el alias
        const uid = await store.createDeviceUID(deviceAlias.value);
        
        if (!uid) {
          serverError.value = "Error al crear el UID del dispositivo";
          isConnecting.value = false;
          return;
        }
        
        // Paso 3: Obtener el listado de receptores (ya incluye el UID automáticamente)
        if (!receptores.value || receptores.value.length <= 0) {
          await store.getListado();
        }
        
        // Reiniciar receptores seleccionados
        receptores_sel.value = [];
        localStorage.receptores = JSON.stringify(receptores_sel.value);
        

        
        // Cerrar el diálogo
        showDialog.value = false;
        isConnecting.value = false;
        
      } catch (error) {
        console.error('Error al configurar el servidor:', error);
        serverError.value = error.message || "Error al conectar con el servidor";
        isConnecting.value = false;
      }
    };

    const connect = () => {
      // Evitar conectar si no hay receptores seleccionados
      const receptoresParaConectar = getReceptores();
      if (receptoresParaConectar.length === 0) {
        return;
      }
      
      // Desconectar TODAS las conexiones existentes y limpiar timers
      VWebsocket.disconnectAll();
      ws.value = []; // Limpiar el array completamente
      
      // Crear nuevas conexiones solo para receptores seleccionados
      receptoresParaConectar.forEach((r) => {
          // Usar la URL almacenada que ya incluye el protocolo
          const serverUrl = localStorage.server || serverUrl.value;
          
          var ws_aux = new VWebsocket(serverUrl, r, store);
          ws.value.push(ws_aux);
          ws_aux.connect();
      });
    };
    
    const abrirServidos = () => {
      showPedidos.value = true;
    };

    const cerrarServidos = () => {
      showPedidos.value = false;
    };

    // Watchers
    watch(receptores, (v) => {
      // Solo conectar si hay receptores y al menos uno seleccionado
      if (v && v.length > 0 && receptores_sel.value.length > 0) {
        connect();
      }
    }, { deep: true });

    // Lifecycle
    onMounted(async () => {
      // Inicializar IndexedDB y cargar todo en memoria
      try {
        await store.inicializarDB();
      } catch (error) {
        console.error('Error al inicializar base de datos:', error);
      }
      
      // Solicitar permisos para notificaciones y audio
      if ('Notification' in window && Notification.permission === 'default') {
        try {
          await Notification.requestPermission();
        } catch (error) {
          // Notificaciones no disponibles
        }
      }

      // Solicitar autorización de audio siempre al iniciar la aplicación
      await store.requestAudioPermission();

      // Registrar callback para cerrar vista de servidos
      store.registrarCerrarServidos(cerrarServidos);
      
      // Limpiar service workers antiguos si existen
      if ('serviceWorker' in navigator) {
        const registrations = await navigator.serviceWorker.getRegistrations()
        for (let registration of registrations) {
          if (registration.scope.includes('/app/')) {
            await registration.unregister()
          }
        }
      }
      
      if (localStorage.server) {
        // Mantener la URL completa con el protocolo para que el usuario la vea
        const savedServer = localStorage.server;
        server.value = savedServer;
        
        // Detectar el protocolo para uso interno
        if (savedServer.startsWith('https://')) {
          useHttps.value = true;
        } else if (savedServer.startsWith('http://')) {
          useHttps.value = false;
        } else {
          useHttps.value = false; // Por defecto HTTP
        }
        
        // Cargar nombre de empresa y alias si existen
        if (localStorage.empresaNombre) {
          empresaNombre.value = localStorage.empresaNombre;
        }
        if (localStorage.deviceAlias) {
          deviceAlias.value = localStorage.deviceAlias;
        }
        
        if (localStorage.receptores) {
          try {
            const storedReceptores = JSON.parse(localStorage.receptores);
            // Eliminar duplicados del localStorage
            receptores_sel.value = [...new Set(storedReceptores)];
          } catch (e) {
            console.error('Error parsing localStorage.receptores:', e);
            receptores_sel.value = [];
          }
        }
        if (!receptores.value || receptores.value.length <= 0) {
          store.getListado();
        } else {
          connect();
        }
      }
    });

    return {
      // Reactive data
      showSelDialog,
      showDialog,
      server,
      useHttps,
      empresaNombre,
      deviceAlias,
      serverError,
      isConnecting,
      ws,
      receptores_sel,
      showPedidos,
      // Computed
      items,
      isWsConnected,
      isHttpConnected: isHttpConnected,
      receptores,
      empresa,
      serverUrl,
      wsUrl,
      receptores_mod,
      receptorActual,
      // Methods
      is_sel,
      getReceptores,
      on_change,
      receptores_change,
      server_change,
      connect,
      abrirServidos,
      cerrarServidos
    };
  }
};
</script>

<style>
.fab-servidos {
  margin-bottom: 2rem;
  margin-right: 1.5rem;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Asegurar que el contenedor tenga espacio suficiente */
.pb-16 {
  padding-bottom: 6rem !important;
}
</style>
