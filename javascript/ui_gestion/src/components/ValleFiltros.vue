<template>
  <v-card elevation="3" title="Filtros" class>
    <v-divider></v-divider>
    <v-card-text>
      <v-item-group
        v-model="selected"
        selected-class="bg-primary"
        :multiple="filtro.multiple"
      >
        <v-item
          v-slot="{ isSelected, toggle }"
          v-for="(f, i) in filtro.caption"
          :key="'op_' + i"
        >
          <v-btn class="ma-2" :color="isSelected ? 'primary' : ''" @click="toggle">{{
            f
          }}</v-btn>
        </v-item>
      </v-item-group>
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
  watch: {
    selected(v) {
      let f = {
        filters: [],
        selected: [],
      };
      if (v == undefined || (typeof v == "object" && Object.keys(v).length == 0)) {
        console.log("all");
        f.filters = Object.values(this.filtro.all);
      } else {
        if (this.filtro.multiple) {
          f.filters = Object.values(this.selected).map((k) => {
            f.selected.push(k);
            return this.filtro.filters[k];
          });
        } else {
          f.selected.push(v);
          f.filters.push(Object.values(this.filtro.filters)[v]);
        }
      }

      this.$emit("on_filter", f);
    },
  },
};
</script>
