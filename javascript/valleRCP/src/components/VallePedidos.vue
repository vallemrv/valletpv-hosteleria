<template>
  <v-dialog v-model="show" fullscreen transition="dialog-bottom-transition">
    <v-toolbar>
      <v-btn icon dark @click="salir()">
        <v-icon>mdi-close</v-icon>
      </v-btn>
      <v-toolbar-title>Pedidos</v-toolbar-title>
    </v-toolbar>
    <v-card>
      <v-card-text>
        <v-row>
          <v-col cols="12" v-for="(item, i) in pedidos" :key="i">
            <v-card elevation="2" class="pa-2" @click="recuperar(item)">
              <v-card-text>
                <v-row>
                  <v-col cols="6">{{ item.camarero }}</v-col>
                  <v-col cols="3">Hora: {{ item.hora }}</v-col>
                  <v-col cols="3">Mesa: {{ item.mesa }}</v-col>
                </v-row>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
import { mapState, mapActions } from "vuex";
export default {
  props: ["show"],
  computed: {
    ...mapState(["pedidos"]),
  },
  methods: {
    ...mapActions(["recuperarPedido"]),
    salir() {
      this.$emit("close");
    },
    recuperar(p) {
      this.recuperarPedido({ pedido: p });
    },
  },
};
</script>
