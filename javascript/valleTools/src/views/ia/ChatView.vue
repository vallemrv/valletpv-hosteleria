<template>
  <v-container fluid>
    <v-app-bar  color="primary" dark>
      <v-btn icon @click="$router.go(-1)">
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
            <v-list-item v-for="(item, index) in items" :key="index">
              <v-card
                elevation="3"
                class="ma-2 pa-2"
                :class="{
                  'text-primary text-left': item.type === 'question',
                  'text-success text-right': item.type === 'answer',
                }"
              >
                <v-card-text class="text-h6">
                  {{ item.text }}
                  <v-container fluid v-if="item.table">
                    <v-table>
                      <template v-slot:default>
                        <thead>
                          <tr>
                            <th
                              class="text-center"
                              v-for="(header, index) in item.table.headers"
                              :key="index"
                            >
                              {{ header }}
                            </th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="(row, rowIndex) in item.table.data"
                            :key="rowIndex"
                          >
                            <td
                              class="text-center"
                              v-for="(cell, cellIndex) in row"
                              :key="cellIndex"
                            >
                              {{ cell }}
                            </td>
                          </tr>
                        </tbody>
                      </template>
                    </v-table>
                  </v-container>
                  <v-container
                    class="text-center"
                    fluid
                    v-if="item.image"
                  >
                    <v-img
                      :src="item.image"
                      max-width="300px"
                      max-height="200px"
                      contain
                    ></v-img>
                  </v-container>
                </v-card-text>
              </v-card>
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

export default {
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
      items: chatStore.items,
      url: empresaStore.empresa.url,
      token: empresaStore.empresa.token,
      empresaStore,
    };
  },

  data() {
    return {
      message: "",
      isRecording: false,
      isRecordingDisabled: false,
      mediaRecorder: null,
      chunks: [],
    };
  },

  methods: {

    cambiarEmpresa(nuevaEmpresa) {
      this.empresaStore.empresa = nuevaEmpresa;
      localStorage.setItem('valleges_empresa', JSON.stringify(nuevaEmpresa));
    },
    borrarChat() {
      // Lógica para borrar el chat
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
        var params = new FormData();
        var sendObj = {
          count: 0,
          instruciones: "",
          query: this.message,
        };
        params.append("user", this.token.user);
        params.append("token", this.token.token);
        params.append("tabla", this.titulo);
        params.append("message", JSON.stringify(sendObj));
        const response = await axios.post(this.url+"/app/gestion_ia/", params);

        // Aquí puedes procesar la respuesta generada por el modelo GPT-3 y hacer lo que necesites con ella
        const generated_text = response.data.generated_text;
        console.log("Respuesta de GPT-3:", generated_text);
        this.chatStore.addItems({
          type: "question",
          text: this.message,
        });
        this.chatStore.addItems(generated_text);

        this.message = ""; // Limpiar el mensaje después de enviarlo
      } catch (error) {
        console.error("Error al enviar el mensaje a la vista:", error);
      }
    },
  },
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
