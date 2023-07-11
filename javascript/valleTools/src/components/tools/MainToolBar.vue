<template>
    <v-app-bar color="indigo darken-1">
        <v-toolbar-title>{{ empStore.getDisplayName() }}</v-toolbar-title>
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

            </v-list>
        </v-menu>
    </v-app-bar>
</template>

<script>
import { EmpresaStore } from '@/stores/empresaStore';
export default {
    setup() {
        const empStore = EmpresaStore();
        return { empStore };
    },


};
</script>