<template>
  <div class="text-center">
    <v-dialog v-model="show">
      <v-card :title="title" width="450px">
        <v-divider></v-divider>
        <v-card-text>
          <v-row>
            <v-col v-for="(f, i) in form" :key="i" cols="12">
              <v-menu v-if="f.tp == 'color'">
                <template v-slot:activator="{ props }">
                  <v-btn :color="col_sel" v-bind="props"> {{ f.label }} </v-btn>
                </template>
                <v-list>
                  <v-list-item>
                    <v-list-item-title>
                      <v-color-picker
                        :ref="color_key(f.col, i)"
                        v-if="f.tp == 'color'"
                        v-model="color_picker"
                      >
                      </v-color-picker>
                    </v-list-item-title>
                  </v-list-item>
                </v-list>
              </v-menu>

              <v-combobox
                v-else-if="f.tp == 'multiple'"
                v-model="item[f.col]"
                :items="f.choices"
                :label="f.label"
                :multiple="f.tp"
                outlined
                dense
              ></v-combobox>
              <v-select
                v-else-if="f.tp == 'select'"
                :items="f.choices"
                :label="f.label"
                item-text="nombre"
                item-value="id"
                v-model="item[f.col]"
              ></v-select>
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
      field_color: {},
    };
  },
  computed: {
    col_sel() {
      return this.$tools.rgbToHex(
        this.color_picker.r + "," + this.color_picker.g + "," + this.color_picker.b
      );
    },
    color_picker: {
      get: function () {
        var col_name = this.field_color;
        var color_sel = { r: 255, g: 0, b: 255, a: 1 };
        if (this.item[col_name] && this.item[col_name] != "") {
          var color_item = this.item[col_name].split(",");
          color_sel = { r: color_item[0], g: color_item[1], b: color_item[2], a: 1 };
        }
        return color_sel;
      },
      set: function (v) {
        var col_name = this.field_color;
        this.item[col_name] = v.r + "," + v.g + "," + v.b;
      },
    },
  },
  methods: {
    ...mapActions(["addItem", "addInstruccion"]),
    color_key(col, i) {
      this.field_color = col;
      return col + "_" + i;
    },
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
