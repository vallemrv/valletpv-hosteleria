<template>
  <v-sheet elevation="4">
    <v-card>
      <v-card-title class="pa-0">
        <v-sheet class="w-100 pa-3" :color="get_color(pedido.op)">
          <div v-if="pedido.mesa" class="float-right">Mesa: {{ pedido.mesa }}</div>
          <div v-else class="float-right">Mensajitooo</div>
          <div>{{ pedido.camarero }}</div>
          hora: {{ pedido.hora }}
        </v-sheet>
      </v-card-title>
      <v-divider></v-divider>
      <v-card-text class="content">
        <v-row>
          <v-col v-if="pedido.msg" cols="12">
            <v-card
              @click="change_linea(pedido)"
              :color="pedido.servido ? 'primary' : ''"
            >
              <v-card-text>
                {{ pedido.msg }}
              </v-card-text>
            </v-card>
          </v-col>
          <v-col
            cols="12"
            class="pa-0 ma-0 mb-1"
            v-for="(linea, i) in pedido.lineas"
            :key="i"
          >
            <v-card @click="change_linea(linea)" :color="linea.servido ? 'primary' : ''">
              <v-card-text>
                <v-row>
                  <v-col cols="2">{{ linea.can }}</v-col>
                  <v-col cols="8">{{ linea.descripcion }}</v-col>
                  <v-col cols="2">{{ linea.estado }}</v-col>
                </v-row>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-card-text>
      <v-divider></v-divider>
      <v-card-actions>
        <div class="w-100 text-right">
          <v-btn @click="servido()" variant="outlined" size="x-large"
            ><v-icon>mdi-check</v-icon></v-btn
          >
          <v-btn @click="delete_pedido()" variant="outlined" size="x-large"
            ><v-icon>mdi-delete</v-icon></v-btn
          >
        </div>
      </v-card-actions>
    </v-card>
  </v-sheet>
</template>

<script>
export default {
  props: ["pedido"],
  methods: {
    get_color(op) {
      console.log(op);
      if (op == "urgente") {
        return "#C70039";
      } else if (op == "urgente") {
        return "#B3E8E4";
      } else if (op == "mensaje") {
        return "#ABEBC6";
      }
      return "primary";
    },
    change_linea(l) {
      l.servido = l.servido ? false : true;
    },
    delete_pedido() {
      var items = this.$store.state.items;
      this.$store.state.items = Object.values(items).filter((e) => {
        return e != this.pedido;
      });
    },
    servido() {
      this.pedido.lineas.forEach((e) => {
        e.servido = true;
      });
    },
  },
};
</script>

<style>
.content {
  min-height: 400px;
}
</style>
