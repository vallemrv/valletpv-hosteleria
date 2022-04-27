<template>
  <v-card elevation="3" title="Filtros" class>
    <v-divider></v-divider>
    <v-card-text>
      <v-row v-if="filtro && filtro.text_filters">
        <v-col cols="12">
          <v-expansion-panels>
            <v-expansion-panel>
              <v-expansion-panel-title>
                Fitros de texto o buscadores
              </v-expansion-panel-title>
              <v-expansion-panel-text>
                <v-col cols="12" v-for="(f, i) in filtro.text_filters" :key="i">
                  <v-text-field
                    :label="f.label"
                    @change="on_text_change"
                    :key_fields="i"
                    hint="@ Para conicidencas exactas"
                  ></v-text-field>
                </v-col>
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
        </v-col>
      </v-row>

      <v-row v-if="filtro && filtro.filters">
        <v-col cols="12">
          <v-expansion-panels>
            <v-expansion-panel>
              <v-expansion-panel-title> Filtros preestablecidos </v-expansion-panel-title>
              <v-expansion-panel-text>
                <v-item-group
                  v-model="selected"
                  selected-class="bg-primary"
                  :multiple="filtro.multiple"
                  class="pa-2"
                >
                  <v-row class="pa-0 ma-0">
                    <v-col
                      class="pa-1 ma-0"
                      cols="4"
                      v-for="(f, i) in filtro.caption"
                      :key="i"
                    >
                      <v-item v-slot="{ isSelected, toggle }">
                        <v-btn
                          class="w-100 text-caption"
                          :color="isSelected ? 'primary' : '#cfb6d4'"
                          @click="toggle"
                          >{{ f }}</v-btn
                        >
                      </v-item>
                    </v-col>
                  </v-row>
                </v-item-group>
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  props: ["filtro"],
  data() {
    return {
      selected: [],
    };
  },
  methods: {
    on_text_change(e) {
      var txt = e.target.value;
      var f = {
        filters: [],
        selected: [],
        include: !txt.includes("@"),
      };
      if (!f.include) txt = txt.replace("@", "");
      if (txt != "") {
        var key = e.target.getAttribute("key_fields");
        var fields = this.filtro.text_filters[key].fields;

        fields.forEach((field) => {
          var obj_f = {};
          obj_f[field] = txt;
          f.filters.push(obj_f);
        });
      } else {
        f.filters = this.filtro.all;
        f.include = false;
      }

      this.$emit("on_filter", f);
    },
    add_to_filter(f, obj_f) {
      if (obj_f.forEach) {
        obj_f.forEach((e) => {
          f.filters.push(e);
        });
      } else {
        f.filters.push(obj_f);
      }
    },
  },
  watch: {
    selected(v) {
      let f = {
        filters: [],
        selected: [],
      };
      if (v == undefined || (typeof v == "object" && Object.keys(v).length == 0)) {
        f.filters = Object.values(this.filtro.all);
      } else {
        if (this.filtro.multiple) {
          Object.values(this.selected).map((k) => {
            f.selected.push(k);
            this.add_to_filter(f, Object.values(this.filtro.filters)[k]);
          });
        } else {
          f.selected.push(v);
          this.add_to_filter(f, Object.values(this.filtro.filters)[v]);
        }
      }

      this.$emit("on_filter", f);
    },
  },
};
</script>
