<template>
    <MainToolBar  titulo="Dispositivos"/>
    <v-main>
        <v-container>
            <v-row>
                <v-col cols="12" >
                    <ListaDispositivos />
                </v-col>
            </v-row>
        </v-container>
    </v-main>
</template>
<script>

import ListaDispositivos from '@/components/listados/ListaDispositivos.vue';
import MainToolBar from '@/components/tools/MainToolBar.vue';

export default {
    name: 'DispositivosView',
    components: {
        ListaDispositivos,
        MainToolBar
    },
}
</script>
