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
            f.text
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
        is_and: this.filtro.is_and ? true : false,
        filters: [],
      };
      if (v != undefined) {
        if (this.filtro.multiple) {
          f.filters = Object.keys(this.selected).map((k) => {
            return this.filtro.filters[k];
          });
        } else {
          f.filters.push(this.filtro.filters[v]);
        }
      } else {
        f.filters = this.filtro.all;
      }

      this.$emit("on_filter", f);
    },
  },
};
</script>
