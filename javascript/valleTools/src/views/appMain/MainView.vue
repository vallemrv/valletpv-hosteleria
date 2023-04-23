<template>
    <v-app>
      <v-navigation-drawer expand-on-hover
           rail v-model="drawer" >
        <v-list>
          <v-list-item
            prepend-avatar="https://randomuser.me/api/portraits/women/85.jpg"
            title="Sandra Adams"
            subtitle="sandra_a88@gmailcom"
          ></v-list-item>
        </v-list>

        <v-divider></v-divider>

        <v-list density="compact" nav>
          <v-list-item prepend-icon="mdi-folder" title="My Files" value="myfiles"></v-list-item>
          <v-list-item prepend-icon="mdi-account-multiple" title="Shared with me" value="shared"></v-list-item>
          <v-list-item prepend-icon="mdi-star" title="Starred" value="starred"></v-list-item>
        </v-list>
      </v-navigation-drawer>
      <v-app-bar app>
        <v-toolbar-title>{{ empresa.nombre }}</v-toolbar-title>
  
        <v-spacer></v-spacer>
  
        <v-btn icon @click="borrarEmpresa">
          <v-icon>mdi-delete</v-icon>
        </v-btn>
  
        <v-btn icon @click="modificarEmpresa">
          <v-icon>mdi-pencil</v-icon>
        </v-btn>
  
        <v-menu offset-y>
          <template v-slot:activator="{ on, attrs }">
            <v-btn icon v-bind="attrs" v-on="on">
              <v-icon>mdi-dots-vertical</v-icon>
            </v-btn>
          </template>
  
          <v-list>
            <v-list-item
              v-for="empresaItem in empresas"
              :key="empresaItem.id"
              @click="seleccionarEmpresa(empresaItem)"
            >
              <v-list-item-title>{{ empresaItem.nombre }}</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </v-app-bar>
  
      <!-- Aquí va el contenido principal de tu aplicación -->
      <v-main>

      </v-main>
       
      <v-footer app>
        <!-- Barra del pie de página -->
        <v-textarea
        color="success"
        v-model="message"
        label="Instrucciones"
        rows="2"
        clear-icon="mdi-close-circle"
        clearable
        :append-inner-icon="message ? 'mdi-send' : (isRecording ? 'mdi-stop': 'mdi-microphone') "
        @click:append-inner="message ? enviarInst() : toggleRecording()" :disabled="isRecordingDisabled && !message"
        append>
        
        </v-textarea>
        
      </v-footer>
    </v-app>
  </template>
  
  <script>
  
  import { useEmpresaStore } from "@/stores/empresaStore";

    export default {
    data() {
        return {
        drawer: false,
        message: "",
        isRecording: false,
        isRecordingDisabled: false,
        };
    },
    setup() {
        const empresaStore = useEmpresaStore();

        return {
        empresa: empresaStore.empresa,
        empresas: empresaStore.empresas,
        borrarEmpresa: empresaStore.rmEmpresa,
        modificarEmpresa: () => {}, // Agrega aquí la función para modificar la empresa
        seleccionarEmpresa: (empresaItem) => {
            empresaStore.empresa = empresaItem;
            localStorage.setItem("valleges_empresa", JSON.stringify(empresaItem));
        },
        };
    },
    methods: {
        toggleRecording() {
        // Agrega aquí la lógica para activar/desactivar la grabación
        },
        enviarInst() {
        // Agrega aquí la lógica para enviar instrucciones
        },
        
    },
    };
    
  </script>
  