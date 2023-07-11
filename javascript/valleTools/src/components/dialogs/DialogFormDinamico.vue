<template>
    <v-dialog v-model="dialog" persistent max-width="600px">
        <v-card>
            <v-card-title>
                <span class="headline">{{ dialogTitle }}</span>
            </v-card-title>
            <v-card-text>
                <v-container>
                    <v-row>
                        <v-col cols="12" sm="6" v-for="field in  fields " :key="field.key">
                            <v-text-field :ref="field.key" hide-details="auto" v-if="field.type === 'text'"
                                v-model="formObject[field.key]" :label="field.label" :rules="field.rules"></v-text-field>
                            <v-select :ref="field.key" v-if="field.type === 'select'" hide-details="auto"
                                :items="field.options" :label="field.label" :rules="field.rules" item-title="text"
                                item-value="value" :multiple="field.multiple" return-object
                                v-model="formObject[field.key]"></v-select>
                            <v-file-input :ref="field.key" hide-details="auto" v-else-if="field.type === 'file'"
                                :label="field.label" :rules="field.rules" v-model="formObject[field.key]"></v-file-input>
                            <v-text-field :ref="field.key" hide-details="auto" v-else-if="field.type === 'number'"
                                v-model="formObject[field.key]" :label="field.label" :rules="field.rules"
                                type="number"></v-text-field>
                            <v-checkbox :ref="field.key" hide-details="auto" v-else-if="field.type === 'checkbox'"
                                v-model="formObject[field.key]" :label="field.label" :rules="field.rules"
                                :value="field.value"></v-checkbox>
                            <v-card :ref="field.key" hide-details="auto" v-else-if="field.type === 'color'"
                                :style="{ backgroundColor: formObject[field.key], height: '100%' }"
                                class="d-flex align-center justify-center" @click="dialogColor = true">
                                <span>Color</span>
                                <v-dialog v-model="dialogColor" width="300">
                                    <v-color-picker v-model="formObject[field.key]" hide-inputs
                                        hide-mode-switch></v-color-picker>
                                </v-dialog>
                            </v-card>

                        </v-col>

                    </v-row>
                </v-container>
            </v-card-text>
            <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn color="blue darken-1" text @click="dialog = false">Cerrar</v-btn>
                <v-btn color="blue darken-1" text @click="saveForm">Guardar</v-btn>
            </v-card-actions>
        </v-card>
        <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="3000" top>{{ snackbarText }}</v-snackbar>
    </v-dialog>
</template>
  
<script>


export default {
    data() {
        return {
            snackbar: false,
            snackbarText: '',
            snackbarColor: '',
            dialogColor: false,
            dialog: false,
            dialogTitle: '',
            formObject: {},
            fields: [],
            storage: null,
            url: '',
        }
    },
    methods: {
        // Abrir el diálogo para crear un nuevo registro
        openDialog(formObject, title, fields, storage = null, url = null) {
            this.storage = storage;
            this.url = url;
            this.formObject = { ...formObject } // Crear una copia profunda del objeto para evitar mutaciones indeseadas
            this.dialogTitle = title;
            this.dialog = true;
            this.fields = fields;
        },
        // Validar el formulario antes de guardar
        // Si el formulario no es válido, señalar los campos que no cumplen con las reglas
        async validate() {
            let valid = true;
            this.fields.forEach(field => {
                if (field.rules) {
                    field.rules.forEach(rule => {
                        if (rule(this.formObject[field.key]) !== true) {
                            valid = false;
                            this.$refs[field.key][0].validate().then(
                                (isValid) => {
                                    valid = valid && isValid;
                                }
                            );
                        }
                    });
                }
            });
            return valid;
        },
        async saveForm() {
            let isValid = await this.validate();
            if (!isValid) {
                this.snackbar = true;
                this.snackbarText = 'Por favor, rellene todos los campos obligatorios.';
                this.snackbarColor = 'error';
                return;
            }
            this.$emit('save', this.formObject);
            this.dialog = false;
        },
    }
}
</script>
  