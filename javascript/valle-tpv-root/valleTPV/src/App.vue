<template>
    <div id="app">
        <v-container v-if="isTooSmall" fluid class="fill-height d-flex align-center justify-center">
            <v-row class="w-100 align-center justify-center">
                <v-col cols="12" md="8" lg="6" class="d-flex justify-center">
                    <v-alert
                        type="error"
                        color="red-darken-2"
                        elevation="12"
                        rounded="xl"
                        class="pa-8 text-center"
                    >
                        <span style="font-size: 2rem; font-weight: bold;">
                            Una Experiencia a Gran Escala
                            <br />
                            <span style="font-size: 1.3rem; font-weight: normal; opacity: 0.8;">
                                Por favor, gira tu dispositivo o usa una tablet.
                            </span>
                        </span>
                    </v-alert>
                </v-col>
            </v-row>
        </v-container>
        
        <template v-else>
            <router-view />
        </template>
        <UiInfoCobroSnackBar s />
    </div>
</template>


<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue';
import { useEmpresasStore, useCamarerosStore, useWebSocket, Empresa } from 'valle-tpv-lib';
import { useDisplay } from 'vuetify';
import { useRouter } from 'vue-router';

// Obtenemos el ancho y ALTO reactivos de la pantalla
const { width, height } = useDisplay();

// El aviso se mostrará si el ancho es < 900px O si el alto es < 650px
const isTooSmall = computed(() => width.value < 900 || height.value < 650);
const router = useRouter();
const empresasStore: any = useEmpresasStore();
const camarerosStore: any = useCamarerosStore();

// Inicializar el sistema de WebSocket integrado
const { webSocketManager } = useWebSocket();

const camarerosSel = computed(() => {
    return camarerosStore.camarerosSel;
});

const empresaActiva = computed((): Empresa | null => {
    return empresasStore.empresaActiva;
});

const camarerosAuth = computed(() => {
    return camarerosStore.camarerosAuth;
});

onMounted(async () => {
    // Cargar empresas
    await empresasStore.initStore();
    await camarerosStore.initStore();
    
    // El WebSocketManager se encarga automáticamente de conectar/desconectar
    // según los cambios en empresasStore.empresaActiva
    console.info('WebSocketManager inicializado - Vigilando cambios de empresa activa');
   
    if (!empresaActiva.value) {
        router.replace('/empresas');
        console.warn('No hay empresas activas configuradas. Redirigiendo a /empresas');
    }
    else if (camarerosAuth.value.length === 0) {
        router.replace('/camareros');
        console.warn('No hay camareros autorizados. Redirigiendo a /camareros');
    }
    else if (camarerosSel.value == null) {
        router.replace('/');
        console.warn('No hay camarero seleccionado. Redirigiendo a /');
    }
});

onUnmounted(() => {
    // Limpiar el WebSocket al desmontar la aplicación
    webSocketManager.destroy();
});
</script>


<style scoped>
.fill-height {
    min-height: 100vh;
}
</style>