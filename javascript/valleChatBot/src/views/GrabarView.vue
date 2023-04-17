<template>
    <div>
      <button @click="toggleRecording" :disabled="isRecordingDisabled">
        {{ isRecording ? "Detener" : "Grabar" }}
      </button>
      <p v-if="transcript">Transcripción: {{ transcript }}</p>
    </div>
  </template>
  
  <script>
  import axios from "axios";
  
  export default {
    data() {
      return {
        isRecording: false,
        isRecordingDisabled: false,
        mediaRecorder: null,
        chunks: [],
        transcript: "",
      };
    },
    methods: {
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
              this.transcript = response.data.transcript;
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
    },
  };
  </script>
  