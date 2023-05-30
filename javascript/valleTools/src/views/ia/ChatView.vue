<template>
  <v-container fluid>
    <v-app-bar  color="primary" dark>
      <v-btn icon @click="disconnectSocket(); borrarChat(); $router.go(-1)">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <v-toolbar-title class=" text-uppercase text-subtitle-1"> <samp> {{ titulo }}</samp></v-toolbar-title>
      <v-spacer></v-spacer>
      
       <!-- Agregando menú desplegable de empresas -->
       <v-menu offset-y>
        <template v-slot:activator="{ props }">
          <v-btn  v-bind="props">
            {{ empresaStore.empresa.nombre }} <v-icon>mdi-chevron-down</v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-item v-for="(empresa, index) in empresaStore.empresas" :key="index" @click="cambiarEmpresa(empresa)">
            <v-list-item-title>{{ empresa.nombre }}</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>

      <v-btn icon @click="borrarChat">
        <v-icon>mdi-delete</v-icon>
      </v-btn>
    </v-app-bar>

    <v-main>
      <v-container>
        <div>
          <v-list two-line>
            <v-list-item v-for="(item, index) in chatStore.items" :key="index">
              <v-card
                elevation="3"
                class="ma-2 pa-2"
                :class="{
                  'text-primary text-left': item.type === 'question',
                  'text-success text-right': item.type === 'answer',
                }"
              >
               <v-card-text class="text-h6" v-if="item.text" style="white-space: pre-line;">{{ item.text }}</v-card-text>
                <v-card-text v-if="item.tabla">
                     <Tabla :tabla="item.tabla"></Tabla>
                </v-card-text>
              </v-card>
              <div
              v-if="index == chatStore.items.length - 1"
              ref="ultimoElemento"></div>
            </v-list-item>
          </v-list>
        </div>
      </v-container>
    </v-main>

    <v-footer app>
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
  </v-container>
</template>

<script>
import axios from "axios";
import { useChatStore } from "@/stores/chatStore";
import { useEmpresaStore } from "@/stores/empresaStore";
import Tabla from "@/components/Tabla.vue"
import ReconnectingWebSocket from '@/api/';

export default {
  components: {
    Tabla
  },
  props: {
    titulo: {
      type: String,
      required: true,
    },
    tipo: {
      type: String,
      required: true,
    },
    opciones: {
      type: String,
    },
  },
  setup() {
    const chatStore = useChatStore();
    const empresaStore = useEmpresaStore();
    return {
      url: empresaStore.empresa.url,
      token: empresaStore.empresa.token,
      empresaStore,
      chatStore,
    };
  },

  data() {
    return {
      message: "",
      isRecording: false,
      isRecordingDisabled: false,
      mediaRecorder: null,
      chunks: [],
      socket:null
    };
  },
  computed:{
    countItems(){
      return this.chatStore.items.lenght - 1
    }
  },
  methods: {

    cambiarEmpresa(nuevaEmpresa) {
      this.empresaStore.empresa = nuevaEmpresa;
      localStorage.setItem('valleges_empresa', JSON.stringify(nuevaEmpresa));
    },
    borrarChat() {
      this.chatStore.items = [];
      this.message = "";
    },

    async toggleRecording() {
      if (this.isRecording) {
        this.mediaRecorder.stop();
        this.isRecordingDisabled = true;
        setTimeout(async () => {
          const blob = new Blob(this.chunks, { type: "audio/wav" });
          const formData = new FormData();
          formData.append("user", this.token.user);
          formData.append("token", this.token.token);
          formData.append("audio", blob, "grabacion.wav");
          try {
            const response = await axios.post(this.url+"/app/gestion_ia/upload", formData, {
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

    async enviarInst() {
      try {
        if (this.socket && this.message.trim() !== "") {
          var sendObj = {
            opciones: {},
            query: this.message.trim(),
            tabla: this.titulo
          };
      
          this.socket.send(JSON.stringify({message: sendObj, 
                            token:this.token }));
          const question = {
            type:"quiestion",
            text: this.message
          }
          this.borrarChat();
          this.chatStore.addItems(question);
          this.$nextTick(() => {
               this.$refs.ultimoElemento[0].scrollIntoView({ behavior: 'smooth' });
           });
          this.message = "";
        }
      } catch (error) {
        console.error("Error al enviar el mensaje a la vista:", error);
      }
    },
    onMessageCallback(message){
        const item = {
            type: message.type,
            text: "",
            tabla: [],
        }
        var count = 0;

        // Si el mensaje es un array, maneja cada elemento del array
        if (Array.isArray(message.text)) {
            message.text.forEach(e => {
                if (e.tabla) item.tabla = e.tabla;
                else if (e.sucess) {
                    count++;
                } else {
                    item.text += " " + e;
                }
            });
        }
        // Si el mensaje no es un array, maneja el objeto individual
        else {
            if (message.text.tabla) item.tabla = message.text.tabla;
            else if (message.text.success) {
                count++;
            } else {
                item.text += " " + message.text;
            }
        }

        if (count > 0) item.text += " " + count + " acciones ejecutadas con exito.";
        this.chatStore.addItems(item);
        this.$nextTick(() => {
            this.$refs.ultimoElemento[0].scrollIntoView({ behavior: 'smooth' });
        });
    },
    connectSocket() {
      
      const websocketUrl = this.url.replace("http://", "ws://") +
                           "/ws/gestion_ia/"+this.token.user+"/"
      this.socket =  new ReconnectingWebSocket(websocketUrl, this.onMessageCallback);
    },
    disconnectSocket() {
      if (this.socket) {
        this.socket.close();
        this.socket = null;
      }
    },
  },
  mounted(){
    this.connectSocket()
  },
  beforeUnmount(){
    this.disconnectSocket();
  }
};
</script>

<style scoped>

.text-primary {
  color: #1976d2 !important; /* Personaliza el color de las preguntas aquí */
}

.text-success {
  color: #4caf50 !important; /* Personaliza el color de las respuestas aquí */
}
</style>
