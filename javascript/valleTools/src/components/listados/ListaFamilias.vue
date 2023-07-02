<template>
   <TablasDatos :store="store"/>
</template>
<script>
import TablasDatos from '@/components/tools/TablasDatos.vue';
import { FamiliasStore } from '@/stores/familias/familias';
import { useEmpresasStore } from '@/stores/empresasStore';
import { watch } from 'vue';

export default {
    components:{
        TablasDatos
    },
    setup() {
        const store = FamiliasStore();
        const empresasStore = useEmpresasStore();
        watch(() => empresasStore.empresaSel, (empresaSel) => {
            if (empresaSel) {
                store.load(empresasStore.getPathDoc(store.collectionName));
            }
        }   );
         
        return { store , empresasStore };
    },
    mounted() {
        if (this.empresasStore.empresaSel) {
            this.store.load(this.empresasStore.getPathDoc(this.store.collectionName));
        }
       
    }

};
</script>