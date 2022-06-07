<template>
  <v-col cols="12" class="ma-0 pa-0 pl-3 pr-3" v-if="subteclas.length > 0">
    <v-expansion-panels>
      <v-expansion-panel>
        <v-expansion-panel-title>
          <v-row>
            <v-col cols="4" class="text-left text-caption">
              <valle-float-form
                :item="tecla"
                column="nombre"
                tb_name="teclas"
                hint="Texto ticket"
              ></valle-float-form>
            </v-col>
            <v-col cols="4">
              <valle-float-form
                :item="tecla"
                column="descripcion_t"
                tb_name="teclas"
                hint="Texto ticket"
              ></valle-float-form>
            </v-col>
            <v-col cols="4" >
              <valle-float-form
                :item="tecla"
                column="descripcion_r"
                tb_name="teclas"
                hint="Texto recepcion"
                location="bottom end"
              ></valle-float-form>
            </v-col>
          </v-row>
        </v-expansion-panel-title>
        <v-expansion-panel-text class="text-center">
          <v-row v-for="(s, i) in subteclas" :key="i">
            <v-col cols="4" class="text-left">
            <valle-float-form
                :item="s"
                column="nombre"
                tb_name="teclas"
                hint="Texto ticket"
              ></valle-float-form></v-col>
            <v-col cols="4" >
              <valle-float-form
                :item="s"
                column="descripcion_t"
                app="gestion"
                tb_name="subteclas"
                hint="Texto ticket"
              ></valle-float-form>
            </v-col>
            <v-col cols="4" >
              <valle-float-form
                :item="s"
                column="descripcion_r"
                tb_name="subteclas"
                hint="Texto recepcion"
                location="bottom end"
              ></valle-float-form>
            </v-col>
          </v-row>
        </v-expansion-panel-text>
      </v-expansion-panel>
    </v-expansion-panels>
  </v-col>
  <v-col cols="12" class="ma-0" v-else>
    <v-card elevation="3" class="mx-auto text-center">
      <v-row class="pl-3 pr-10">
        <v-col cols="4" class="text-left text-caption pl-6">
          <valle-float-form
                :item="tecla"
                column="nombre"
                tb_name="teclas"
                hint="Texto ticket"
              ></valle-float-form>
        </v-col>
        <v-col cols="4" >
          <valle-float-form
            :item="tecla"
            column="descripcion_t"
            app="gestion"
            tb_name="teclas"
            hint="Texto ticket"
          ></valle-float-form>
        </v-col>
        <v-col cols="4" >
          <valle-float-form
            :item="tecla"
            column="descripcion_r"
            tb_name="teclas"
            hint="Texto recepcion"
            location="bottom end"
          ></valle-float-form>
        </v-col>
      </v-row>
    </v-card>
  </v-col>
</template>

<script>
import { mapGetters } from "vuex";
import ValleFloatForm from "@/components/ValleFloatForm.vue";
export default {
  components: { ValleFloatForm },
  props: ["tecla"],
  data: () => {
    return {};
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
    test(v) {
      console.log(v);
    },
  },
};
</script>
