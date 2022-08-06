<template>
  <v-menu
    transition="scale-transition"
    :anchor="location"
    :origin="origin"
    v-model="mostrar"
  >
    <template v-slot:activator="{ props }">
      <v-btn
        block
        :elevation="elevation"
        class="h-100 text-center'"
        v-bind="props"
        height="auto"
        @click.stop="() => {}"
      >
        <v-row class="pa-0 ma-0 text-caption">
          <div class="w-100" v-if="!tipo || tipo == 'number'">
            <v-col cols="12" color="#5868ff" class="current_val"
              ><span> {{ _value }}</span>
            </v-col>
            <v-col cols="12" class="old_val" v-if="val_old"> {{ val_old }} </v-col>
          </div>
        </v-row>
      </v-btn>
    </template>
    <v-card width="200px">
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
  props: [
    "item",
    "column",
    "rules",
    "hint",
    "app",
    "tb_name",
    "value",
    "tipo",
    "hint",
    "location",
    "origin",
    "filter",
  ],
  data: () => {
    return {
      elevation: 3,
      mostrar: false,
      val_modified: "",
      val_old: null,
    };
  },
  computed: {
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
      this.item[this.column] = parseInt(this.val_modified);
      this.mostrar = false;
      var id = this.item.id ? this.item.id : this.item.ID;
      let inst = {
        tb: this.tb_name,
        app: app,
        reg: {},
        tipo: "md",
        id: id,
      };

      if (this.filter) {
        inst["filter"] = { tecla_id: id };
        inst["reg"]["orden"] = this.val_modified;
      } else {
        inst["reg"][this.column] = this.val_modified;
      }
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

<style scoped>
span {
  white-space: normal;
}
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
.v-btn {
  min-height: 40px;
}
</style>
