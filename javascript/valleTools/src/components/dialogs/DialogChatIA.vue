<template>
      <!-- Diálogo -->
      <v-dialog v-model="dialogVisible" 
               :fullscreen="true" :scrim="false"
                transition="dialog-bottom-transition" >
      <v-card>
         <v-toolbar color="light-blue lighten-3" dark>
                <v-btn
                icon
                dark
                @click="dialogVisible = false"
                >
                <v-icon>mdi-close</v-icon>
            </v-btn>
           <v-toolbar-title>Título del diálogo</v-toolbar-title>
        </v-toolbar>

          <v-card-text>
            <!-- Lista de elementos -->
            <v-list>
              <v-list-item v-for="(item, index) in lista" :key="index">
                
                  <v-list-item-title>{{ item }}</v-list-item-title>
               
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>
      </v-dialog>
  </template>
  
  <script>
  import BarraIA from '@/components/tools/BarraIA.vue';
  export default {
    components: {
      BarraIA
    },
    data() {
      return {
        dialogVisible: false,
        texto: '',
        lista: []
      };
    },
    methods: {
      mostrarDialog() {
        this.dialogVisible = true;
      },
      agregarElemento() {
        this.lista.push(this.texto);
        this.texto = '';
      }
    }
  };
  </script>
  
  