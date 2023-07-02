<template>
   <MainToolBar />
    <v-main>
        <v-container>
            <v-row>
                <v-col v-if="view=='camareros'" cols="12">

                    <ListaCamareros />
                </v-col>
                <v-col v-else-if="view=='familias'" cols="12">
                    <FamiliasView />
                </v-col>
                <v-col v-else="view=='empresas'" cols="12">
                    <EmpresasView />
                </v-col>
            </v-row>
        </v-container>
    </v-main>
</template>
  
<script>
import MainToolBar from "../../components/tools/MainToolBar.vue";
import EmpresasView from "@/views/gestion/EmpresasView.vue";
import ListaCamareros from "@/components/listados/ListaCamareros.vue";
import FamiliasView from "@/views/gestion/FamiliasView.vue";
import { useEmpresasStore } from "@/stores/empresasStore";

export default {
    props: {
        view: String,
        default: "empresas"
    },
    components: {
    EmpresasView,
    ListaCamareros,
    FamiliasView,
    MainToolBar
},
    setup() {
       
        const empStore = useEmpresasStore();
      
        return {  empStore };
    },
    computed: {
        titulo() {
            return this.empStore.getDisplayName();
        }
    },
    methods: {
        chatClick() {
            this.$router.push({ name: "ChatView" });
        }
    }
};
</script>
  