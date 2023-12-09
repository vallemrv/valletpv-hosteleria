<template>
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
export default{
    data(){
        return{
            message: "",
            isRecording: false,
            isRecordingDisabled: false,
        }
    }
}
</script>