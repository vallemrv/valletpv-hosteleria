<template>
  <v-toolbar color="#cfb6d4">
    <v-toolbar-title>
      Receptor <span v-if="empresa">{{ empresa.nombre }}</span>
    </v-toolbar-title>
    <v-spacer></v-spacer>
    <v-btn icon @click="() => (showSelDialog = true)">
      <v-icon>mdi-list-status</v-icon></v-btn
    >
    <v-btn icon @click="() => (showDialog = true)"> <v-icon>mdi-cog</v-icon></v-btn>
    <template v-slot:extension>
      <v-btn @click="reload" icon class="btn-reload" bottom elevation="5">
        <v-icon>mdi-reload</v-icon>
      </v-btn>
    </template>
  </v-toolbar>
  <div class="v-100 text-center pa-5" v-show="!isConnected">
    <v-sheet elevation="3">no hay conexion con el servidor</v-sheet>
  </div>
  <v-container fluid>
    <v-row>
      <v-col cols="12" sm="6" md="3" v-for="(item, i) in items" :key="i">
        <valle-comanda :pedido="item"></valle-comanda>
      </v-col>
    </v-row>
  </v-container>
  <v-dialog v-model="showDialog">
    <v-card title="Configuracion" width="250">
      <v-card-text>
        <v-text-field v-model="server" label="Direccion servidor" hide-details="auto">
        </v-text-field>
      </v-card-text>
      <v-card-actions>
        <div class="w-100">
          <v-btn @click="server_change()" class="float-right">Aceptar</v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="showSelDialog">
    <v-card title="Selecionar receptor" width="400">
      <v-card-text>
        <v-row class="pa-4">
          <v-col class="pa-0" cols="12" v-for="(r, i) in receptores_mod" :key="i">
            {{ r.Nombre }}
            <v-switch
              v-model="r.is_sel"
              @change="on_change(r)"
              class="float-right"
              hide-details="auto"
              color="success"
            ></v-switch>
          </v-col>
        </v-row>
      </v-card-text>
      <v-card-actions>
        <div class="w-100">
          <v-btn @click="receptores_change()" class="float-right">Aceptar</v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import VWebsocket from "@/websocket";
import { mapState, mapActions } from "vuex";
import ValleComanda from "@/components/ValleComanda.vue";

export default {
  components: { ValleComanda },
  data() {
    return {
      showSelDialog: false,
      showDialog: false,
      server: "",
      ws: [],
      receptores_sel: [],
    };
  },
  computed: {
    ...mapState(["items", "isConnected", "receptores", "empresa"]),
    receptores_mod() {
      this.receptores.forEach((e) => {
        e.is_sel = this.is_sel(e.ID);
      });
      return this.receptores;
    },
  },
  methods: {
    ...mapActions(["getListado", "getDatosEmpresa", "getPendientes"]),
    is_sel(id) {
      return Object.values(this.receptores_sel).includes(id);
    },
    getReceptores() {
      if (this.receptores_sel.length > 0) {
        return Object.values(this.receptores).filter((r) => {
          return this.receptores_sel.includes(r.ID);
        });
      }
      return [];
    },
    on_change(r) {
      if (r.is_sel) {
        this.receptores_sel.push(r.ID);
      } else {
        this.receptores_sel = Object.values(this.receptores_sel).filter((e) => {
          return e != r.ID;
        });
      }
    },
    receptores_change() {
      this.showSelDialog = false;
      localStorage.receptores = JSON.stringify(this.receptores_sel);
      this.connect();
    },
    server_change() {
      this.showDialog = false;
      localStorage.server = this.server;
      if (!this.receptores || this.receptores.length <= 0) {
        this.getListado({ server: this.server, tabla: "receptores" });
      }
      this.receptores_sel = [];
      localStorage.receptores = JSON.stringify(this.receptores_sel);
      this.getDatosEmpresa();
    },
    connect() {
      this.ws.forEach((w) => {
        w.disconnect();
      });
      this.getReceptores().forEach((r) => {
        var ws_aux = new VWebsocket(this.server, r.nomimp, r.Nombre, this.$store.commit);
        this.ws.push(ws_aux);
        ws_aux.connect();
      });
    },
    reload() {
      this.getPendientes({ receptores: JSON.stringify(this.receptores_sel) });
    },
  },
  watch: {
    receptores(v) {
      if (v) {
        this.connect();
      }
    },
    items(v) {
      if (!v) {
        this.$notification.playAudio();
      }
    },
  },
  mounted() {
    if (localStorage.server) {
      this.server = localStorage.server;
      if (this.empresa == null) this.getDatosEmpresa();
      if (localStorage.receptores) {
        this.receptores_sel = JSON.parse(localStorage.receptores);
      }
      if (!this.receptores || this.receptores.length <= 0) {
        this.getListado({ tabla: "receptores" });
      } else {
        this.connect();
      }
    }
  },
};
</script>

<style>
.btn-reload {
  position: relative;
  margin-top: 30px;
  background-color: aqua;
}
.v-toolbar {
  overflow: visible;
}
</style>
