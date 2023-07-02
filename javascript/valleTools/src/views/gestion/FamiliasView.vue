<template>
    <v-row>
        <v-col cols="12" md="6">
            <ListaFamilias />
        </v-col>
        <v-col cols="12" md="6">
            <ListaReceptores />
        </v-col>
    </v-row>
</template>
<script>
import ListaFamilias from '../../components/listados/ListaFamilias.vue';
import ListaReceptores from '../../components/listados/ListaReceptores.vue';

export default {
    name: 'FamiliasView',
    components: {
        ListaFamilias,
        ListaReceptores
    }, 
}
</script>
