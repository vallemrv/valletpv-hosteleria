<template>
  <div class="text-center">
    <v-dialog v-model="show" content-class="v-dialog--custom">
      <v-card :title="title">
        <v-divider></v-divider>
        <v-card-text>
          <v-row>
            <v-col v-for="(f, i) in form" :key="i" cols="12">
              <valle-color-input-vue
                v-if="f.tp == 'color'"
                v-model="item[f.col]"
                :label="f.label"
              >
              </valle-color-input-vue>
              <v-switch
                v-else-if="f.tp == 'switch'"
                v-model="item[f.col]"
                :label="f.label"
                color="success"
                hide-details="auto"
              ></v-switch>
              <v-combobox
                v-else-if="f.tp == 'multiple'"
                v-model="item[f.col]"
                :items="f.choices"
                :label="f.label"
                :multiple="f.tp"
                :values="f.keys"
                hide-details="auto"
                outlined
                dense
              ></v-combobox>
              <valle-select-vue
                v-else-if="f.tp == 'select'"
                :items="f.choices"
                :label="f.label"
                :values="f.keys"
                v-model="item[f.col]"
              ></valle-select-vue>
              <v-text-field
                v-else
                v-model="item[f.col]"
                :label="f.label"
                hide-details="auto"
                :type="f.tp"
                autocomplete="false"
              ></v-text-field>
            </v-col>
            <v-col>
              <v-alert v-show="textAlert" color="warning">{{ textAlert }} </v-alert>
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
import ValleSelectVue from "./ValleSelect.vue";
import ValleColorInputVue from "./ValleColorInput.vue";

export default {
  components: { ValleSelectVue, ValleColorInputVue },
  props: ["show", "item", "form", "title", "tb_name", "tipo"],
  data() {
    return { textAlert: null };
  },
  methods: {
    ...mapActions(["addItem", "addInstruccion"]),
    close_dialogo() {
      this.textAlert = null;
      this.$emit("close", null);
    },
    enviar() {
      this.textAlert = this.$tools.valid_form(this.item, this.form);
      if (!this.textAlert) {
        if (this.tipo == "add") {
          this.addItem({ item: this.item, tb_name: this.tb_name });
        } else if (this.tipo == "md") {
          var id = this.item.id ? this.item.id : this.item.ID;
          let inst = {
            tb: this.tb_name,
            reg: this.item,
            tipo: "md",
            id: id,
          };
          this.addInstruccion({ inst: inst });
        } else if (this.tipo == "md_teclados") {
          let inst = {
            tb: this.tb_name,
            tb_mod: this.item.tb_name,
            reg: this.item,
            tipo: "md_teclados",
            filter: this.item.filter,
          };
          this.addInstruccion({ inst: inst });
        } else if (this.tipo == "add_empresa"){
           this.$emit("close", this.item);
        }
        this.close_dialogo();
      }
    },
  },
  watch: {
    show(v) {
      if (!v) {
        this.$emit("close");
      }
    },
  },
};
</script>

<style>
.v-dialog--custom {
  width: 85%;
}
</style>
