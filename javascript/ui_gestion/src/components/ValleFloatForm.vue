<template>
  <v-menu
    transition="scale-transition"
    anchor="top center"
    origin="top center"
    v-model="mostrar"
  >
    <template v-slot:activator="{ props }">
      <v-btn
        block
        :elevation="elevation"
        class="text-center"
        v-bind="props"
        :color="col_sel"
        @click.stop="() => {}"
      >
        <v-row cols="12" class="pa-0 ma-0 text-caption">
          <div class="w-100" v-if="!tipo || tipo == 'number'">
            <v-col cols="12" color="#5868ff" class="current_val"> {{ _value }} </v-col>
            <v-col cols="12" class="old_val" v-if="val_old"> {{ val_old }} </v-col>
          </div>
        </v-row>
      </v-btn>
    </template>
    <v-card v-if="tipo == 'color'">
      <v-card-text>
        <v-color-picker @change="on_enter()" v-model="color_picker"> </v-color-picker>
      </v-card-text>
    </v-card>
    <v-card width="200px" v-else>
      <v-card-header>
        <v-text-field
          v-model="val_modified"
          @click.stop=""
          hide-details="auto"
          :rules="rules"
          @keypress.enter="on_enter()"
          :hint="hint"
        ></v-text-field>
      </v-card-header>
    </v-card>
  </v-menu>
</template>

<script>
import { mapActions } from "vuex";

export default {
  props: ["item", "column", "rules", "hint", "app", "tb_name", "value", "tipo", "hint"],
  data: () => {
    return {
      elevation: 3,
      mostrar: false,
      val_modified: "",
      val_old: null,
    };
  },
  computed: {
    col_sel() {
      if (this.column == "rgb") {
        return this.$tools.rgbToHex(
          this.color_picker.r + "," + this.color_picker.g + "," + this.color_picker.b
        );
      } else {
        return "";
      }
    },
    color_picker: {
      get: function () {
        var color_sel = { r: 255, g: 0, b: 255, a: 1 };
        if (this.item[this.column] && this.item[this.column] != "") {
          var color_item = this.item[this.column].split(",");
          color_sel = { r: color_item[0], g: color_item[1], b: color_item[2], a: 1 };
        }
        return color_sel;
      },
      set: function (v) {
        this.val_modified = v.r + "," + v.g + "," + v.b;
        this.item[this.column] = this.val_modified;
      },
    },
    _value() {
      if (this.value) return this.value;
      else return this.item[this.column];
    },
  },
  methods: {
    ...mapActions(["addInstruccion"]),
    on_enter() {
      var app = this.app;
      if (!app) app = "gestion";
      this.val_old = this.item[this.column];
      this.item[this.column + "_old"] = this.val_old;
      this.item[this.column] = this.val_modified;
      this.mostrar = false;
      var id = this.item.id ? this.item.id : this.item.ID;
      let inst = {
        tb: this.tb_name,
        app: app,
        reg: {},
        tipo: "md",
        id: id,
      };
      inst["reg"][this.column] = this.val_modified;
      this.addInstruccion({ inst: inst });
      this.$emit("change", this.item);
    },
  },
  watch: {
    item(v) {
      if (v) {
        this.val_modified = this.item[this.column];
        this.val_old = this.item[this.column + "_old"];
      }
    },
    mostrar(v) {
      if (!v && this.tipo == "color") {
        this.on_enter();
      }
    },
  },
  mounted() {
    if (this.value) this.val_modified = this.value;
    else this.val_modified = this.item[this.column];
  },
};
</script>

<style>
.current_val {
  margin: 0;
  padding: 0;
}
.old_val {
  margin: 0;
  padding: 0;
  color: brown;
  font-size: smaller;
}
</style>
