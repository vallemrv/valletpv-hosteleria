<template>
    <MainToolBar titulo="Camareros">
        <v-btn color="white" icon @click="showActivos = !showActivos">
            <v-icon>mdi-swap-horizontal</v-icon>
        </v-btn>
    </MainToolBar>
    <v-main>
        <v-container>
            <v-row>
                <v-col cols="12">
                    <ListaCamareros :showActivos="showActivos" />
                </v-col>

            </v-row>
        </v-container>
    </v-main>
</template>
<script>
import ListaCamareros from '@/components/listados/ListaCamareros.vue';
import MainToolBar from '@/components/tools/MainToolBar.vue';
export default {
    name: 'CamarerosView',
    components: {
        ListaCamareros,
        MainToolBar
    },
    data(){
        return {
            showActivos: true,
            
        }
    }
}
</script>
