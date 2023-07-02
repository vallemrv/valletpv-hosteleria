<template>
    <v-card elevation="4">
        <v-card-title>
            <v-toolbar title="Mis Empresas">
                <v-btn color="primary" icon @click="crearEmpresa">
                    <v-icon>mdi-plus</v-icon>
                </v-btn>
            </v-toolbar>
        </v-card-title>

        <v-table  class="elevation-1 border ma-2">
            <thead>
                <tr>
                    <th v-for="(title, index) in headers " :key="index"> {{ title }}</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(empresa, index) in empresasStore.empresas" :key="index">
                    <td>{{ empresa.alias }}</td>
                    <td>
                        <v-icon small class="mr-2" @click="editarEmpresa(empresa)">
                            mdi-pencil
                        </v-icon>
                        <v-icon small @click="borrarEmpresa(empresa)">
                            mdi-delete
                        </v-icon>
                    </td>
                    <td>
                        <v-switch :disabled="empresa.selected" v-model="empresa.selected" color="primary"
                            @change="empresasStore.selEmpresa(empresa)"></v-switch>
                    </td>
                </tr>
            </tbody>
        </v-table>
    </v-card>
    <DialogFormDinamico ref="editEmpresaDialog" @save="save" />
    <DialogConfirm ref="confirmDialog" @result="result" />

    <v-snackbar v-model="snackbar" :color="snackbarColor" :timeout="snackbarTimeout" top>
        {{ snackbarText }}
    </v-snackbar>
</template>
  
<script>
import DialogFormDinamico from "@/components/dialogs/DialogFormDinamico.vue";
import DialogConfirm from "@/components/dialogs/DialogConfirm.vue";
import { useEmpresasStore } from '@/stores/empresasStore';
import { fb_create, fb_delete, fb_update, fb_pathDoc, storage} from '@/api';


export default {
    components: {
        DialogFormDinamico,
        DialogConfirm,
    },
    props: {
        user: {
            type: Object,
        },
    },
    setup() {
        const empresasStore = useEmpresasStore();

        return {
            empresasStore,
        };
    },
    data() {
        return {
            snackbar: false,
            snackbarColor: "",
            snackbarTimeout: 2000,
            headers: ["Nombre", "Acciones", ""], // Aquí va el código para mostrar los nombres de las columnas
            isNew: false,
        };
    
    },
    computed: {
        // Aquí va el código para mostrar las empresas del usuario
        fields() {
            let campos = [
                {
                    key: "alias",
                    label: "Alias",
                    type: "text",
                    rules: [v => !!v || "El alias es requerido"],
                },
                {
                    key: "empresa",
                    label: "Nombre de la Empresa",
                    type: "text",
                    rules: [v => !!v || "El nombre de la empresa es requerido"],
                },
                {
                    key: "email",
                    label: "Email",
                    type: "text",
                },
                {
                    key: "razonSocial",
                    label: "Razón Social",
                    type: "text",
                    rules: [v => !!v || "La razón social es requerida"],
                },
                {
                    key: "nif",
                    label: "NIF",
                    type: "text",
                    rules: [v => !!v || "El NIF es requerido"],
                },
                {
                    key: "direccion",
                    label: "Dirección",
                    type: "text",
                    rules: [v => !!v || "La dirección es requerida"],
                },
                {
                    key: "telefono",
                    label: "Teléfono",
                    type: "text",
                    rules: [v => !!v || "El teléfono es requerido"],
                },
                {
                    key: "poblacion",
                    label: "Población",
                    type: "text",
                    rules: [v => !!v || "La población es requerida"],
                },
                {
                    key: "provincia",
                    label: "Provincia",
                    type: "text",
                    rules: [v => !!v || "La provincia es requerida"],
                },
                {
                    key: "cp",
                    label: "Código Postal",
                    type: "text",
                    rules: [v => !!v || "El código postal es requerido"],
                }
            ]
            if (!this.isNew) {
                campos.push({
                    key: "logoImpresora",
                    label: "Logo Impresora",
                    type: "file",
                    rules: [],
                },
                    {
                        key: "logoCorporacion",
                        label: "Logo Corporación",
                        type: "file",
                        rules: [],
                    })
            }
            return campos;
        }

    },
    methods: {
        mostrarMensaje(mensaje, color) {
            this.snackbarText = mensaje;
            this.snackbarColor = color;
            this.snackbar = true;
        },
        async save(formObject) {
            // Aquí va el código para guardar la empresa
            const pathDoc = fb_pathDoc("usuarios", this.user.id, "empresas");
            if (this.isNew) {
                let { doc: obj, error: errorNew } = await fb_create(pathDoc, formObject);
                if (obj) {
                    this.mostrarMensaje("Empresa creada correctamente", "success");
                } else {
                    this.mostrarMensaje("Error al crear la empresa" + errorNew, "error");
                }
            } else {
                let error = await fb_update(pathDoc, formObject.id, formObject,);
                if (!error) {
                    this.mostrarMensaje("Empresa actualizada correctamente", "success");
                } else {
                    this.mostrarMensaje("Error al actualizar la empresa ", "error");
                }
            }
        },
        crearEmpresa() {
            this.isNew = true;
            this.$refs.editEmpresaDialog.openDialog({
                alias: "",
                empresa: "",
                email: "",
                razonSocial: "",
                nif: "",
                direccion: "",
                telefono: "",
                poblacion: "",
                provincia: "",
                cp: "",
                logoImpresora: [],
                logoCorporacion: [],
            }, 'Crear Empresa', this.fields);
        },
        editarEmpresa(empresa) {
            // Aquí va el código para editar la empresa seleccionada
            this.isNew = false;
            this.$refs.editEmpresaDialog.openDialog(empresa, 'Editar Empresa', this.fields, storage, empresa.empresa + "/logos/");
        },
        borrarEmpresa(empresa) {
            this.$refs.confirmDialog.openDialog("¿Estás seguro de que quieres borrar la empresa " + empresa.empresa + "?", empresa);
        },
        async result(result, empresa) {
            if (result) {
                const pathDoc = fb_pathDoc("usuarios", this.user.id, "empresas");
                const error = await fb_delete(pathDoc, empresa.id);
                if (error) {
                    this.mostrarMensaje("Error al borrar la empresa", "error");
                } else {
                    this.mostrarMensaje("Empresa borrada correctamente", "success");
                }
            }
        },
        
    },
    
};
</script>
  