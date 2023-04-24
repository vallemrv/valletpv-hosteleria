<!-- ConfirmationDialog.vue -->
<template>
    <v-dialog v-model="dialog" max-width="290">
      <v-card>
        <v-card-title class="text-h5">Advertencia</v-card-title>
        <v-card-text>
          {{ message }}
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="green darken-1" text @click="onConfirm">Sí</v-btn>
          <v-btn color="red darken-1" text @click="onCancel">No</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </template>
  
  <script>
  export default {
    props: {
      message: {
        type: String,
        default: '',
      },
    },
    data() {
      return {
        dialog: false,
      };
    },
    methods: {
      onConfirm() {
        this.$emit('result', true);
        this.dialog = false;
      },
      onCancel() {
        this.$emit('result', false);
        this.dialog = false;
      },
      openDialog() {
        this.dialog = true;
      },
    },
  };
  </script>
  