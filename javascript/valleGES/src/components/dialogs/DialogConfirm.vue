<!-- ConfirmationDialog.vue -->
<template>
    <v-dialog v-model="dialog" max-width="290">
      <v-card>
        <v-card-title class="text-h5">{{title}}</v-card-title>
        <v-card-text v-if="message">
          {{ message }}
        </v-card-text>
        <v-card-text v-else>
          {{formated_message}}
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="green darken-1" text @click="onConfirm">{{text_ok}}</v-btn>
          <v-btn color="red darken-1" text @click="onCancel">{{text_cancel}}</v-btn>
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
      text_ok: {
        type: String,
        default: 'SI',
      },
      text_cancel: {
        type: String,
        default: 'NO',
      },
      title: {
        type: String,
        default: 'Confirmación',
      },
    },
    data() {
      return {
        dialog: false,
        formated_message: null,
        tag: null
      };
    },
    methods: {
      onConfirm() {
        this.$emit('result', true, this.tag);
        this.dialog = false;
      },
      onCancel() {
        this.$emit('result', false, this.tag);
        this.dialog = false;
      },
      openDialog(message=null, tag=null) 
      {
        this.tag = tag;
        if (message) {
          this.formated_message = message;
        }
        this.dialog = true;
      },
    },
  };
  </script>
  