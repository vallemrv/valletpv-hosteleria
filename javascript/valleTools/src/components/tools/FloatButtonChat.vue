<template>
    <div class="div-fixed">
        <v-btn class="float-button" icon elevation="5" small color="primary"  @click="irAChat">
            <v-icon>mdi-chat</v-icon>
        </v-btn>
    </div>
</template>
  
<script>
export default {
    methods: {
        irAChat() {
           this.$emit("chatClick")
        }
    }
}
</script>
  
<style>
.float-button {
    position: fixed;
    bottom: 20px;
    right: 20px;
}
</style>
  