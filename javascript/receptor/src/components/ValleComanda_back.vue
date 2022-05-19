<template>
  <v-sheet elevation="3">
    <v-card color="#cfb6d4">
      <v-card-text>
        <div class="float-right">mesa: {{ pedido.mesa }}</div>
        {{ pedido.camarero }}

        <div>hora: {{ pedido.hora }}</div>
      </v-card-text>
    </v-card>
    <v-container>
      <v-row>
        <v-col cols="12" v-for="(linea, i) in pedido.lineas" :key="i">
          <v-card @click="change_linea(linea)" :color="linea.servido ? primary : ''">
            <v-card-text>
              <v-row color="primary">
                <v-col cols="2">{{ linea.can }}</v-col>
                <v-col cols="8">{{ linea.descripcion }}</v-col>
                <v-col cols="2">{{ linea.estado }}</v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
    <v-divider></v-divider>
    <div class="h-100 pa-3">
      <v-btn class="float-right"><v-icon>mdi-delete</v-icon></v-btn>
      <dir class="clearfix"></dir>
    </div>
  </v-sheet>
</template>

<script>
export default {
  props: ["pedido"],
  methods: {
    change_linea(l) {
      l.servido = l.servido ? false : true;
    },
  },
};
</script>

<style scoped>
.v-container {
  min-height: 350px;
}
.clearfix {
  clear: both;
}
</style>
