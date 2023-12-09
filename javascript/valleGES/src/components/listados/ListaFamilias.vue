<template>
   <TablasDatos :store="store"/>
</template>
<script>
import TablasDatos from '@/components/tools/TablasDatos.vue';
import { FamiliasStore } from '@/stores/familias/familias';
import { EmpresaStore } from '@/stores/empresaStore';
import { watch } from 'vue';

export default {
    components:{
        TablasDatos
    },
    setup() {
        const store = FamiliasStore();
        const empresaStore = EmpresaStore();
        watch(() => empresaStore.empresa, (empresa) => {
            if (empresa) {
                store.load(empresaStore);
            }
        }   );
         
        return { store , empresaStore };
    },
};
</script>