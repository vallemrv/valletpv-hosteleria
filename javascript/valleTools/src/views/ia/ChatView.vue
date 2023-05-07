<template>
    <v-container fluid>
      <v-app-bar color="primary" dark>
        <v-btn icon @click="$router.go(-1)">
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>
        <v-toolbar-title>{{ titulo }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="borrarChat">
          <v-icon>mdi-delete</v-icon>
        </v-btn>
      </v-app-bar>
  
      <v-main>
        <v-container>
          <!-- Contenido del chat aquí -->
        </v-container>
      </v-main>
  
      <v-footer fixed>
        <v-textarea
          v-model="mensaje"
          placeholder="Escribe un mensaje"
          solo
          hide-details
          :auto-grow="true"
        ></v-textarea>
        <v-btn v-if="mensaje" @click="enviarMensaje">Enviar</v-btn>
        <vue-audio-recorder-and-player v-else></vue-audio-recorder-and-player>
        <v-btn @click="uploadImage">Enviar foto</v-btn>
        <vue2-dropzone ref="myDropzone" @vdropzone-success="success"></vue2-dropzone>
      </v-footer>
    </v-container>
  </template>
  
  <script>
  import VueAudioRecorderAndPlayer from "vue-audio-recorder-and-player";
  import Vue2Dropzone from "vue2-dropzone";
  
  export default {
    components: {
      VueAudioRecorderAndPlayer,
      Vue2Dropzone,
    },
    props: {
        titulo: {
        type: String,
        required: true
        },
        tipo: {
        type: String,
        required: true
        },
        opciones: {
        type: String,
        required: true
        },
    },
    data() {
      return {
        titulo: "Chat",
        mensaje: "",
      };
    },
    methods: {
      borrarChat() {
        // Lógica para borrar el chat
      },
      enviarMensaje() {
        // Lógica para enviar el mensaje
      },
      enviarAudio(audio) {
        // Lógica para enviar el audio
      },
      uploadImage() {
        this.$refs.myDropzone.selectFiles();
      },
      success(file, response) {
        // Lógica para manejar la imagen subida
      },
    },
  };
  </script>
  