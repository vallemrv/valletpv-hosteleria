<template>
    <MainToolBar titulo="Camareros" />
    <v-main>
        <v-container>
            <v-row>
                <v-col cols="12">
                    <ListaCamareros />
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
}
</script>
