<template>
  <v-toolbar color="#cfb6d4">
    <v-toolbar-title>Camareros pase</v-toolbar-title>
  </v-toolbar>

  <v-row class="pa-5">
    <v-col cols="12" v-for="(item, i) in items" :key="i">
      <v-card class="pa-0">
        <v-card-text>
          <v-row>
            <v-col class="pl-4 mt-4" cols="4">
              {{ item.nombre }}
            </v-col>
            <v-col class="mt-4" cols="4">
              {{ item.apellidos }}
            </v-col>
            <v-col cols="4">
              <valle-switch
                v-model="item.autorizado"
                @change_value="on_change"
                :item="item"
                color="success"
                label="activo"
              ></valle-switch>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>
    </v-col>
  </v-row>
</template>

<script>
import { mapState, mapActions } from "vuex";
import ValleSwitch from "@/components/ValleSwitch";

export default {
  components: { ValleSwitch },
  methods: {
    ...mapActions(["getListado", "addInstruccion"]),
    on_change(v) {
      var inst = {
        tb: "camareros",
        tipo: "md",
        reg: { autorizado: v.autorizado },
        id: v.id,
      };
      this.addInstruccion({ inst: inst });
    },
  },
  computed: {
    ...mapState(["camareros"]),
    items() {
      if (!this.camareros) return [];
      return Object.values(this.camareros).filter((e) => {
        return e.activo == 1;
      });
    },
  },
  watch: {
    camareros(v) {
      if (!v) this.getListado({ tabla: "camareros" });
    },
  },
  mounted() {
    if (!this.camareros) {
      this.getListado({ tabla: "camareros" });
    }
  },
};
</script>
