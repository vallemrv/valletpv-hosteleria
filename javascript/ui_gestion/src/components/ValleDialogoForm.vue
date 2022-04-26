<template>
  <div class="text-center">
    <v-dialog v-model="show">
      <v-card :title="title" width="450px">
        <v-divider></v-divider>
        <v-card-text>
          <v-row>
            <v-col v-for="(f, i) in form" :key="i" cols="12">
              <v-combobox
                v-if="f.tp == 'multiple'"
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
          <v-btn color="primary" @click="handlerShow">cancelar</v-btn>
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
    };
  },
  methods: {
    ...mapActions(["addItem", "addInstruccion"]),
    handlerShow() {
      this.$emit("on_visible_change", !this.show);
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
      this.handlerShow();
    },
  },
  watch: {
    show(v) {
      this.$emit("on_visible_change", v);
    },
  },
};
</script>
