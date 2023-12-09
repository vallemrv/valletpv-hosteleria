<template>
    <MainToolBar  titulo="Usuarios"/>
    <v-main>
        <v-container>
            <v-row>
                <v-col cols="12">
                    <ListaUsuarios />
                </v-col>
            </v-row>
        </v-container>
    </v-main>
</template>
<script>

import ListaUsuarios from '@/components/listados/ListaUsuarios.vue';
import MainToolBar from '@/components/tools/MainToolBar.vue';

export default {
    name: 'UserView',
    components: {
        ListaUsuarios,
        MainToolBar
    },
}
</script>
