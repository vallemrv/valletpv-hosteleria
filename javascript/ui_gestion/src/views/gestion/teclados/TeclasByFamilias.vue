<template>

 <ValleHeader title="Teclas por familia" anchor="bottom end"/>
 <v-container>
  <v-row>
    
    <v-col cols="12">
      <v-card class="mt-5">
        <v-card-title> Familias </v-card-title>
        <v-card-text>
          <valle-select-vue
            :items="choices"
            :values="values"
            v-model="idfamilia"
          ></valle-select-vue>
        </v-card-text>
      </v-card>
    </v-col>
    <v-col cols="6" sm="4" v-for="(item, i) in items" :key="i">
      <v-card class="h-100">
        <v-card-text>
          <v-row>
            <v-col cols="6"
              ><span>{{ item.nombre }}</span> <br/><span>{{ parseFloat(item.p1).toFixed(2) }} €</span></v-col
            >
            <v-col cols="6" class="text-right">
              <v-btn size="x-small" variant="text" icon>
                <v-icon @click="edit(item)">mdi-pencil</v-icon>
              </v-btn>
              <v-btn size="x-small" variant="text" icon>
                <v-icon @click="borrar(item)">mdi-delete</v-icon>
              </v-btn>
            </v-col>
            <v-col cols="12" v-if="item.descripcion_r">{{ item.descripcion_r }}</v-col>
          </v-row>
        </v-card-text>
      </v-card>
    </v-col>
  </v-row>
  <valle-dialogo-form-vue
    @close="() => (showDialogo = false)"
    :show="showDialogo"
    :form="form"
    :item="itemSel"
    title="Editar tecla"
    tipo="md"
    tb_name="teclas"
  ></valle-dialogo-form-vue>
  </v-container>
</template>

<script>
import ValleHeader from "@/components/ValleHeader.vue";
import ValleSelectVue from "@/components/ValleSelect.vue";
import ValleDialogoFormVue from "@/components/ValleDialogoForm.vue";
import { mapGetters, mapActions, mapState } from "vuex";

export default {
  components: { ValleSelectVue, ValleDialogoFormVue, ValleHeader },
  data() {
    return {
      idfamilia: null,
      showDialogo: false,
      itemSel: null,
    };
  },
  computed: {
    ...mapState(["teclas", "familias"]),
    ...mapGetters(["getListValues"]),
    form() {
      return [
        { col: "nombre", label: "Nombre", tp: "text" },
        {
          col: "descripcion_r",
          label: "Descripcion recepcion",
          tp: "text",
        },
        {
          col: "familia",
          tp: "select",
          choices: this.choices,
          keys: this.values,
        },
      ];
    },
    choices() {
      return this.getListValues("familias", "nombre");
    },
    values() {
      return this.getListValues("familias", "id");
    },
    items() {
      if (this.idfamilia != null && this.teclas) {
        return Object.values(this.teclas).filter((e) => {
          return e.familia == this.idfamilia;
        });
      } else {
        this.getListadoCompuesto({ tablas: ["teclas"] });
        return [];
      }
    },
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    edit(item) {
      this.itemSel = item;
      this.showDialogo = true;
    },
    borrar(item) {
      var inst = {
        tb: "teclas",
        tipo: "rm",
        id: item.id,
      };
      let ls = this.$store.state["teclas"];
      this.$store.state["teclas"] = ls.filter((e) => {
        return e.id != item.id;
      });
      this.addInstruccion({ inst: inst });
    },
  },
  mounted() {
    this.getListadoCompuesto({ tablas: ["teclas", "subteclas", "familias"] });
  },
};
</script>
