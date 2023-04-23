<template>
    <v-container fluid>
      <v-row class="ma-auto w-lg-75" justify="center">
        <v-col cols="12" sm="8" md="6">
          <v-card>
            <v-card-title>{{ title }}</v-card-title>
            <v-card-text>
              <v-form ref="form">
                <v-text-field label="Nombre de la empresa" v-model="companyName"></v-text-field>
                <v-text-field label="URL" v-model="url" type="url"></v-text-field>
                <v-text-field label="Nombre de usuario" v-model="username"></v-text-field>
                <v-text-field label="Contraseña" v-model="password" type="password"></v-text-field>
              </v-form>
            </v-card-text>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="red" @click="cancel" v-if="store.countEmpresas > 0">Cancelar</v-btn>
              <v-btn color="green" @click="submit">Enviar</v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </template>
  
  <script>
  import { defineComponent } from "vue";
  import { useStore } from "../path/to/your/store";
  
  export default defineComponent({
    setup() {
      const store = useStore();
      const title = store.title;
      return { store, title };
    },
    data() {
      return {
        companyName: "",
        url: "",
        username: "",
        password: "",
      };
    },
    methods: {
      submit() {
        // Procesa los datos del formulario aquí
      },
      cancel() {
        // Lógica para el botón cancelar aquí
      },
    },
  });
  </script>
  