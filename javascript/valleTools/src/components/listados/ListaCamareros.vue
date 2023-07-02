<template>
   <TablasDatos :store="store"/>
</template>
<script>
import TablasDatos from '@/components/tools/TablasDatos.vue';
import { CamarerosStore } from '@/stores/camareros';
import { useEmpresasStore } from '@/stores/empresasStore';
import { watch } from 'vue';
export default {
    components:{
        TablasDatos
    },
    setup() {
        const store = CamarerosStore();
        const empresasStore = useEmpresasStore();
        watch(() => empresasStore.empresaSel, (empresa) => {
            if(empresa)
                store.load(empresasStore.getPathDoc(store.collectionName));
        });
        
        return { store, empresasStore };
    },
    mounted() {
        if(this.empresasStore.empresaSel && this.store.items.length == 0){
           this.store.load(this.empresasStore.getPathDoc(this.store.collectionName));
        }
      
    },
    

};
</script>