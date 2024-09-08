<template>
   <TablasDatos :store="store"/>
</template>
<script>
import TablasDatos from '@/components/tools/TablasDatos.vue';
import { EmpresaStore } from '@/stores/empresaStore';
import { watch } from 'vue';
import { DispositivosStore } from '@/stores/dispositivos';
export default {
    components:{
        TablasDatos
    },
    setup() {
        const store = DispositivosStore();
        const empresaStore = EmpresaStore();
        watch(() => empresaStore.empresa, (empresa) => {
            if(empresa)
                store.load(empresaStore);
        });
        
        return { store, empresaStore };
    },
    mounted() {
        if(this.empresaStore.empresa && this.store.items.length == 0){
            this.store.load(this.empresaStore);
        }
    },
    

};
</script>