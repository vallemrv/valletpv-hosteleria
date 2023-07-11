<template>
    <MainToolBar />
    <v-main>
        <v-container>
            <v-row>
                <v-col v-if="receptores.items.length > 0" cols="12" md="6">
                    <ListaFamilias />
                </v-col>
                <v-col cols="12" md="6">
                    <ListaReceptores />
                </v-col>
            </v-row>
        </v-container>
    </v-main>
</template>
<script>
import ListaFamilias from '../../components/listados/ListaFamilias.vue';
import ListaReceptores from '../../components/listados/ListaReceptores.vue';
import MainToolBar from '../../components/tools/MainToolBar.vue';
import { ReceptoresStore } from '@/stores/familias/receptores.js';
import { FamiliasStore } from '@/stores/familias/familias.js';
import { EmpresaStore } from '@/stores/empresaStore';
import { watch } from 'vue';

export default {
    name: 'FamiliasView',
    components: {
        ListaFamilias,
        ListaReceptores,
        MainToolBar
    },
    setup() {
        const empresaStore = EmpresaStore();
        const familias = FamiliasStore();
        const receptores = ReceptoresStore();
        receptores.load(empresaStore);
        if (receptores.items.length > 0 && empresaStore.empresa) {
            familias.load(empresaStore, receptores.items);
        }
        watch(() => receptores.items.length, (e) => {
            familias.loadReceptores(receptores.items);
        });
        return { receptores };
    }
}
</script>
