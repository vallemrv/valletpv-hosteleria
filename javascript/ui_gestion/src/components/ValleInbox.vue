<template>
  <v-menu v-model="showMod" anchor="bottom end" origin="auto">
    <template v-slot:activator="{ props }">
      <v-btn stacked v-bind="props">
        <v-badge color="info" :content="num_inst">
          <v-icon icon="mdi-inbox-arrow-down"></v-icon>
        </v-badge>
      </v-btn>
    </template>

    <v-card variant="outlined" width="200px">
      <v-card-header> Actualizaciones </v-card-header>
      <v-spacer></v-spacer>
      <v-list border class="mx-auto" max-width="256">
        <v-list-item>
          <v-list-item-title class="mr-10"> Pendientes </v-list-item-title>
          <v-spacer></v-spacer>
          <template v-slot:append>
            <v-badge color="info" :content="num_inst" inline></v-badge>
          </template>
        </v-list-item>
      </v-list>
      <v-card-actions class="pl-16">
        <v-btn
          variant="outlined"
          @click="
            actualizar();
            showMod = false;
          "
          >Actualizar</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-menu>
</template>

<script>
import { mapActions } from "vuex";

export default {
  props: ["num_inst"],
  data() {
    return { showMod: false };
  },
  methods: {
    ...mapActions(["actualizar"]),
  },
};
</script>
