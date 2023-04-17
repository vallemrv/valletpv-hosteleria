<template>
      <v-navigation-drawer expand-on-hover
        rail >
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
        <!-- Encabezado -->
        
        <v-toolbar-title>ValleIA</v-toolbar-title>
      </v-app-bar>
  
      <v-main>
        <v-container fluid>
          
        </v-container>
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
        append->
        
        </v-textarea>
        
      </v-footer>
   
  </template>
  
  <script> 
  import axios from "axios";

  export default {
    components: {
      
    },
    data() {
      return {
        drawer: false,
        message: "",
        isRecording: false,
        isRecordingDisabled: false,
        mediaRecorder: null,
        chunks: [],
      };
    },
    methods:{
      async toggleRecording() {
        if (this.isRecording) {
          this.mediaRecorder.stop();
          this.isRecordingDisabled = true;
          setTimeout(async () => {
            const blob = new Blob(this.chunks, { type: "audio/wav" });
            const formData = new FormData();
            formData.append("audio", blob, "grabacion.wav");
            try {
              const response = await axios.post("http://localhost:8000/valleIA/upload/", formData, {
                headers: { "Content-Type": "multipart/form-data" },
              });
              this.message = response.data.transcript;
            } catch (error) {
              console.error("Error al enviar el audio:", error);
            }
            this.chunks = [];
            this.isRecordingDisabled = false;
          }, 1000);
        } else {
          if (!navigator.mediaDevices) {
            alert("No se pueden acceder a los dispositivos de medios.");
            return;
          }
  
          try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            this.mediaRecorder = new MediaRecorder(stream);
            this.mediaRecorder.start();
            this.mediaRecorder.addEventListener("dataavailable", (event) => {
              this.chunks.push(event.data);
            });
          } catch (error) {
            console.error("Error al acceder al micrófono:", error);
            return;
          }
        }
        this.isRecording = !this.isRecording;
      },
      async enviarInst(){
        this.message = ""
      }
    }
  };
  </script>
  