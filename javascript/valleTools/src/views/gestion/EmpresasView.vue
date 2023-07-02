<template>
    <v-main>
        <v-container>
            <v-row>
                <v-col cols="12" md="6"></v-col>
                <v-col cols="12" md="6">
                    <ListaEmpresas />
                </v-col>
            </v-row>
        </v-container>
    </v-main>
</template>


<script>
import ListaEmpresas from "@/components/listados/ListaEmpresas.vue";
import { useUserStore } from "@/stores/userStore";
import { useEmpresasStore } from "@/stores/empresasStore";
import { ConfigStore } from "@/stores/configStore";

export default {
    components: {
        ListaEmpresas,
    },
    props: {
        titulo: String,
        default: "empresas"
    },
    setup() {
        const userStore = useUserStore();
        const empStore = useEmpresasStore();
        const configStore = ConfigStore();
        return {
            userStore,
            empStore,
            configStore,
        };

    },
    methods: {
        goToProfile() {
            this.$router.push({ name: "profile" });
        },
        logout() {
            auth.signOut();
        },
    },
};
</script>