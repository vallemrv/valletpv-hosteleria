<template>
    <v-app-bar color="indigo darken-1">
        <v-toolbar-title>{{ empStore.getDisplayName() }}</v-toolbar-title>
        <v-toolbar-subtitle>{{ titulo }}</v-toolbar-subtitle>
        <v-spacer></v-spacer>
        <slot></slot>
        <v-menu>
            <template v-slot:activator="{ props }">
                <v-btn v-bind="props" icon>
                    <v-icon>mdi-store</v-icon>
                </v-btn>
            </template>
            <v-list>
                <v-list-item v-for="(item, index) in empStore.empresas" :key="index" @click="empStore.selEmpresa(item)">
                    <template v-slot:append v-if="item.id == empStore.empresa.id">
                        <v-icon color="green" >mdi-check</v-icon>
                    </template>
                    <v-list-item-title>{{ item.nombre }}</v-list-item-title>

                </v-list-item>
                <v-divider></v-divider>
                <v-list-item @click="profileEmp">
                    <v-list-item-title>Profile</v-list-item-title>
                </v-list-item>
            </v-list>
        </v-menu>
    </v-app-bar>
    <DialogFormDinamico ref="editEmpresaDialog" @save="save" />
</template>

<script>
import { EmpresaStore } from '@/stores/empresaStore';
import DialogFormDinamico from '@/components/dialogs/DialogFormDinamico.vue';

export default {
    props:["titulo"],
    components: {
        DialogFormDinamico
    },
    setup() {
        const empStore = EmpresaStore();
        return { empStore };
    },
    methods: {
        profileEmp() {
            this.$refs.editEmpresaDialog.openDialog(this.empStore.profile, 'Editar Empresa', this.empStore.fields);
       
        },
        save(item){
            this.empStore.update(item);
        }
    }
};
</script>