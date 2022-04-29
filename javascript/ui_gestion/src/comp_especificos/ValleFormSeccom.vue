<template>
  <div class="text-center">
    <v-dialog v-model="show">
      <v-card :title="title" width="450px">
        <v-divider></v-divider>
        <v-card-text>
          <v-row>
            <v-col v-for="(f, i) in form" :key="i" cols="12">
              <v-switch
                v-if="f.tp == 'switch'"
                v-model="item[f.col]"
                :label="f.label"
                color="success"
              ></v-switch>
              <v-select
                v-else-if="f.tp == 'select'"
                :items="choices"
                :label="f.label"
                item-text="nombre"
                item-value="id"
                v-model="_val_sel"
              ></v-select>
              <v-text-field
                v-else-if="f.col == 'descuento'"
                :disabled="!item['es_promocion']"
                v-model="item[f.col]"
                :label="f.label"
                hide-details="auto"
                :rules="rules"
                :type="f.tp"
                autocomplete="false"
              ></v-text-field>
              <v-text-field
                v-else
                v-model="item[f.col]"
                :label="f.label"
                hide-details="auto"
                :rules="rules"
                :type="f.tp"
                autocomplete="false"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-btn color="primary" @click="close_dialogo">cancelar</v-btn>
          <v-btn color="pink" @click="enviar">aceptar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import { mapActions } from "vuex";

export default {
  props: ["show", "item", "form", "title", "tb_name", "tipo"],
  data() {
    return {
      rules: [(value) => !!value || "Es necesario."],
      choices: [
        "Bar",
        "Bocadillo",
        "Carne",
        "Cocktel",
        "Copa con rodaja de limon",
        "Copa de vino",
        "Cubalibre",
        "Donut",
        "Jarra de cerveza",
        "Icono para llevar",
        "Magdalena",
        "Menu",
        "Pescado",
        "Pincho",
        "Pizza",
        "Plato humeante",
        "Plato combinado",
        "Plato sopa",
        "Plato sopa con cuchara",
        "Tarta",
        "Taza cafe",
      ],
      values: [
        "bar",
        "bocadillo",
        "carne",
        "cocktel",
        "copa_con_limon",
        "copa_vino",
        "cubalibre",
        "donut",
        "jarra_cerveza",
        "llevar",
        "magdalena",
        "menu",
        "pescado",
        "pincho",
        "pizza",
        "plato",
        "plato_combinado",
        "sopa",
        "sopa_cuchara",
        "tarta",
        "taza_cafe",
      ],
    };
  },
  computed: {
    _val_sel: {
      set(v) {
        var index = this.choices.indexOf(v);
        this.item["icono"] = this.values[index];
      },
      get() {
        var index = this.values.indexOf(this.item["icono"]);
        return this.choices[index];
      },
    },
  },
  methods: {
    ...mapActions(["addItem", "addInstruccion"]),
    close_dialogo() {
      this.$emit("close_dialogo");
    },
    enviar() {
      if (this.tipo == "add") {
        this.addItem({ item: this.item, tb_name: this.tb_name });
      } else {
        let inst = {
          tb: this.tb_name,
          reg: this.item,
          tipo: "md",
          id: this.item.id,
        };
        this.addInstruccion({ inst: inst });
      }
      this.close_dialogo();
    },
  },
  watch: {
    show(v) {
      if (!v) {
        this.$emit("close_dialogo");
      }
    },
  },
};
</script>
