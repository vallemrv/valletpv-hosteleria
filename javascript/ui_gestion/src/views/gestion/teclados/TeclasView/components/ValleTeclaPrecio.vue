<template>
  <v-col cols="12" class="pa-0 mt-1 pl-3 pr-3" v-if="subteclas.length > 0">
    <v-expansion-panels>
      <v-expansion-panel>
        <v-expansion-panel-title>
          <v-row>
            <v-col class="text-left text-caption">
              <valle-float-form
                :item="tecla"
                column="nombre"
                app="gestion"
                tb_name="teclas"
                :value="tecla.nombre"
              ></valle-float-form>
            </v-col>
            <v-col>
              <valle-float-form
                :item="tecla"
                column="p1"
                app="gestion"
                tb_name="teclas"
                :rules="rules"
                :value="tecla.p1"
              ></valle-float-form>
            </v-col>
            <v-col>
              <valle-float-form
                :item="tecla"
                column="p2"
                app="gestion"
                tb_name="teclas"
                :rules="rules"
                :value="tecla.p2"
                origin="auto"
                location="bottom end"
              ></valle-float-form>
            </v-col>
          </v-row>
        </v-expansion-panel-title>
        <v-expansion-panel-text class="text-center">
          <v-row v-for="(s, i) in subteclas" :key="i">
            <v-col class="text-left" cols="4">
              <valle-float-form
                :item="s"
                column="nombre"
                app="gestion"
                tb_name="subteclas"
                :value="s.nombre"
              ></valle-float-form
            ></v-col>
            <v-col cols="3">
              <valle-float-form
                :item="s"
                column="incremento"
                app="gestion"
                tb_name="subteclas"
                :rules="rules"
                :value="tecla.incremento"
                :hint="precio(s, tecla.p1)"
              ></valle-float-form>
            </v-col>
            <v-col cols="3" class="pt-5">
              {{ precio(s, tecla.p2) }}
            </v-col>
            <v-col cols="2">
              <v-btn @click="delete_subtecla(s)" icon variant="text"
                ><v-icon>mdi-delete</v-icon></v-btn
              ></v-col
            >
          </v-row>
        </v-expansion-panel-text>
      </v-expansion-panel>
    </v-expansion-panels>
  </v-col>
  <v-col cols="12" class="mt-1" v-else>
    <v-card elevation="3" class="pb-4 pt-4 mx-auto text-center">
      <v-row class="pl-3 pr-10">
        <v-col cols="4" class="text-left text-caption pl-6">
          <valle-float-form
            :item="tecla"
            column="nombre"
            app="gestion"
            tb_name="teclas"
            :value="tecla.nombre"
          ></valle-float-form>
        </v-col>
        <v-col cols="4">
          <valle-float-form
            :item="tecla"
            column="p1"
            app="gestion"
            tb_name="teclas"
            :rules="rules"
            :value="tecla.p1"
          ></valle-float-form>
        </v-col>
        <v-col cols="4">
          <valle-float-form
            :item="tecla"
            column="p2"
            app="gestion"
            tb_name="teclas"
            :rules="rules"
            :value="tecla.p2"
            origin="auto"
            location="bottom end"
          ></valle-float-form>
        </v-col>
      </v-row>
    </v-card>
  </v-col>
</template>

<script>
import { mapGetters, mapActions } from "vuex";
import ValleFloatForm from "@/components/ValleFloatForm.vue";
export default {
  components: { ValleFloatForm },
  props: ["tecla"],
  data: () => {
    return {
      rules: [
        (value) => !!value || "Requerido.",
        (value) => !isNaN(value - parseFloat(value)) || "Tiene que ser precio",
      ],
    };
  },
  computed: {
    ...mapGetters(["getItemsFiltered"]),
    subteclas() {
      var f = {
        filters: [{ tecla: this.tecla.id }],
      };
      return this.tecla ? this.getItemsFiltered(f, "subteclas") : [];
    },
  },
  methods: {
    ...mapActions(["addInstruccion"]),
    precio(s, p) {
      return (parseFloat(s.incremento) + parseFloat(p)).toFixed(2);
    },
    delete_subtecla(v) {
      let inst = {
        tb: "subteclas",
        tipo: "rm",
        id: v.id,
      };
      let ls = this.$store.state["subteclas"];
      this.$store.state["subteclas"] = ls.filter((e) => {
        return e.id != v.id;
      });
      this.addInstruccion({ inst: inst });
    },
  },
};
</script>
