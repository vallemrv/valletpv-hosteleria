<template>
   <TablasDatos :store="store"/>
</template>
<script>
import TablasDatos from '@/components/tools/TablasDatos.vue';
import { ReceptoresStore } from '@/stores/familias/receptores';
import { EmpresaStore } from '@/stores/empresaStore';
import { watch } from 'vue';

export default {
    components:{
        TablasDatos
    },
    setup() {
        const store = ReceptoresStore();
        const empresaStore = EmpresaStore();
        watch(() => empresaStore.empresa, (empresa) => {
            if (empresa) {
                store.load(empresaStore);
            }
        }   );
        return { store, empresaStore };
    },
    mounted() {
        if (this.empresaStore.empresa) {
            this.store.load(this.empresaStore);
        }
    }

};
</script>