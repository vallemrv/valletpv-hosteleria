<template>
    
      <v-navigation-drawer v-model="drawer" app>
        <!-- Contenido del menú lateral -->
      </v-navigation-drawer>
  
      <v-app-bar app>
        <!-- Encabezado -->
        <v-btn icon @click="drawer = !drawer">
          <v-icon>mdi-menu</v-icon>
        </v-btn>
        <v-toolbar-title>Titulo</v-toolbar-title>
      </v-app-bar>
  
      <v-main>
        <v-container fluid>
          
        </v-container>
      </v-main>
  
      <v-footer app>
        <!-- Barra del pie de página -->
        <v-text-field
        color="success"
        loadings
        v-model="message"
        label="Instrucciones"
        multi-line
        rows="2"
        hide-detailclear-icon="mdi-close-circle"
        clearable
        :append-inner-icon="message ? 'mdi-send' : 'mdi-microphone'"
        append->
        
      </v-text-field>
        
      </v-footer>
   
  </template>
  
  <script>
  
  
  export default {
    components: {
      
    },
    data() {
      return {
        drawer: false,
        message: ""
      };
    }
  };
  </script>
  