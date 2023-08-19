<template>
   <TablasDatos :store="store"/>
</template>
<script>
import TablasDatos from '@/components/tools/TablasDatos.vue';
import { CamarerosStore } from '@/stores/camareros';
import { EmpresaStore } from '@/stores/empresaStore';
import { watch } from 'vue';
export default {
    props: {
        showActivos: {
            type: Boolean,
            default: true
        }
    },
    components:{
        TablasDatos
    },
    setup() {
        const store = CamarerosStore();
        const empresaStore = EmpresaStore();
        watch(() => empresaStore.empresa, (empresa) => {
            if(empresa){
                store.load(empresaStore);
                store.setShowActivos(this.showActivos);
            }
        });
        return { store, empresaStore };
    },
    watch: {
        showActivos(showActivos){
            this.store.setShowActivos(showActivos);
        }
    },
    mounted() {
        if(this.empresaStore.empresa && this.store.items.length == 0){
            this.store.load(this.empresaStore);
            this.store.setShowActivos(this.showActivos);
        }
    },
    

};
</script>