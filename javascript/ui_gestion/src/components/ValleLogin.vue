<template>
  <v-card min-width="100px" class="ma-5 pa-5">
    <v-card-title>Identificate</v-card-title>
    <form>
      <v-text-field
        v-model="usuario"
        :error-messages="Errors"
        :counter="10"
        label="Usuario"
        required
      ></v-text-field>
      <v-text-field
        v-model="pass"
        :error-messages="passErrors"
        label="Contraseña"
        required
      ></v-text-field>

      <v-btn class="mr-4" @click="submit"> submit </v-btn>
      <v-btn @click="clear"> clear </v-btn>
    </form>
  </v-card>
</template>

<script>
export default {};
</script>
