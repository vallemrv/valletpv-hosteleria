<template>
    <v-card elevation="4">
        <v-card-title>
            <v-toolbar :title="store.titulo">
                <v-btn color="primary" icon @click="crearItem">
                    <v-icon>mdi-plus</v-icon>
                </v-btn>
            </v-toolbar>
        </v-card-title>

        <v-table class="elevation-1 border ma-2">
            <thead>
                <tr>
                    <th v-for="(title, index) in store.headers" :key="index">{{ title }}</th>
                    <th>Acciones</th>
                    <th v-if="store.switchKey"></th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(item, index) in store.items" :key="index">
                    <td v-for="(key, index) in store.showKeys" :key="index">{{ item[key] }}</td>
                    <td>
                        <v-icon small class="mr-2" @click="editarItem(item)">
                            mdi-pencil
                        </v-icon>
                        <v-icon small @click="borrarItem(item)">
                            mdi-delete
                        </v-icon>
                         <v-icon  v-for="(action, index) in store.extraAcitons" small  :key="index"
                         @click="store.execAction(item, action.action)">
                            {{action.icon}}   
                        </v-icon>
                    </td>
                    <td v-if="store.switchKey">
                        <v-switch  v-model="item[store.switchKey]"  color="primary"
                            @change="store.switchCh(item)"></v-switch>
                    </td>
                </tr>
            </tbody>
        </v-table>
    </v-card>
    <DialogFormDinamico ref="editDialog" @save="guardarItem" />
    <DialogConfirm ref="confirmDialog" @result="confirmarBorrado" />

    <v-snackbar v-model="snackbar" :color="snackbarColor" :timeout="snackbarTimeout" top>
        {{ snackbarText }}
    </v-snackbar>
</template>
  
<script>
import DialogFormDinamico from "@/components/dialogs/DialogFormDinamico.vue";
import DialogConfirm from "@/components/dialogs/DialogConfirm.vue";

export default {
    props: {
        store: {
            type: Object,
        },
    },
    components: {
        DialogFormDinamico,
        DialogConfirm,
    },
    data() {
        return {
            snackbar: false,
            snackbarColor: "",
            snackbarTimeout: 2000,
            isNew: false,
        };
    },
    methods: {
        mostrarMensaje(mensaje, color) {
            this.snackbarText = mensaje;
            this.snackbarColor = color;
            this.snackbar = true;
        },
        async guardarItem(formObject) {
            let error = null;
            if (this.isNew) 
                error = await this.store.add(formObject);
            else 
                error = await this.store.update(formObject);

            if (!error) {
                this.mostrarMensaje("Item creado correctamente", "success");
            }else{
                this.mostrarMensaje(error, "error");
            }
    
        },
        crearItem() {
            this.isNew = true;
            this.$refs.editDialog.openDialog(this.store.newItem, "Crear Item", this.store.fields);
        },
        editarItem(item) {
            this.isNew = false;
            this.$refs.editDialog.openDialog(item, "Editar Item", this.store.fields);
        },
        borrarItem(item) {
            this.$refs.confirmDialog.openDialog(`¿Estás seguro de que quieres borrar el item ${item[this.store.displayName]}?`, item);
        },
        async confirmarBorrado(resultado, item) {
            if (resultado) {
                const error = await this.store.delete(item);
                if (error) {
                    this.mostrarMensaje(error, "error");
                } else {
                    this.mostrarMensaje("Item borrado correctamente", "success");
                }
            }
        },
    },
};
</script>
  