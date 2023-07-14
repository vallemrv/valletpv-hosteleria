<template>
    <v-toolbar>
                <v-row>
                    <v-col cols="8">
                        <v-toolbar-title class="ml-3 mt-3">Cambiar precios</v-toolbar-title>
                    </v-col>
                    <v-col cols="4">
                        <v-text-field v-model="str_buscar" @change.prepend="buscar" variant="outlined" hide-details="auto"
                            prepend-inner-icon="mdi-magnify"></v-text-field>

                    </v-col>
                </v-row>

            </v-toolbar>
</template>


<script>
import {TeclasStore} from "@/stores/teclados/teclas.js";
export default{
    setup(){
        const store = TeclasStore();
        return { store }
    },
    data(){
        return {
            str_buscar: ""
        }
    },
    methods:
    {
        buscar(){
            if(this.str_buscar == ""){
                this.store.load({parent_id: null});
                return;
            }
            this.store.load({nombre__icontains: this.str_buscar});
        }
    }
}
</script>