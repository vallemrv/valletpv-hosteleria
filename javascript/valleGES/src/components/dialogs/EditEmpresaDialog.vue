<!-- EditEmpresaDialog.vue -->
<template>
    <v-dialog v-model="dialog" class="w-lg-75">
        <FormEmpresa :title="title" :tipo="tipo" @close="handleClose" />
    </v-dialog>
</template>
  
<script>
import FormEmpresa from "@/components/appMain/FormEmpresa.vue";

export default {
    components: {
      FormEmpresa,
    },
    props: {
    title: {
      type: String,
      required: true,
    },
    tipo: {
      type: String,
      required: true,
      validator: (value) => ["nuevo", "editar"].includes(value),
    },
  },
  data() {
      return {
        dialog: false,
      };
    },
  methods: {
      openDialog() {
        this.dialog = true;
      },
      handleClose() {
        this.dialog = false;
      },
    },
  };
</script>
  