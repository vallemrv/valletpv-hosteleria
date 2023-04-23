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
        <v-toolbar-title>ValleGES IA</v-toolbar-title>
      </v-app-bar>
  
      <v-main>
        <ChatComponent/>
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
   
  </template>
  
  <script> 
  import axios from "axios";
  import ChatComponent from "./ChatComponent.vue";
  import { useChatStore } from "@/stores/chatStore";

  export default {
    components: {
      ChatComponent
    },
    setup() {
    const chatStore = useChatStore();
     return {
         chatStore
      };
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
        try {
            var params = new FormData();
            var sendObj = {
               count: 0,
               instruciones: "",
               query:this.message
            }
            params.append("message", JSON.stringify(sendObj));
            const response = await axios.post("http://localhost:8000/valleIA/gpt3_api/", params);

            // Aquí puedes procesar la respuesta generada por el modelo GPT-3 y hacer lo que necesites con ella
            const generated_text = response.data.generated_text;
            console.log("Respuesta de GPT-3:", generated_text);
            this.chatStore.addItems({
              type: "question",
              text: this.message
            })
            this.chatStore.addItems(generated_text)
            
            this.message = ""; // Limpiar el mensaje después de enviarlo
          } catch (error) {
            console.error("Error al enviar el mensaje a la vista:", error);
          }
      }
    }
  };
  </script>
  