<template>
  <v-row class="pa-0 ma-0 text-center">
    <v-col :cols="cols" class="pa-0 ma-0" v-for="(item, i) in items" :key="i">
      <v-sheet class="pa-1">
        <v-btn
          block
          class="text-caption"
          @click="on_click(item)"
          height="80px"
          :color="bg_color(item)"
        >
          <span c>{{ item.nombre }}</span>
        </v-btn>
      </v-sheet>
    </v-col>
  </v-row>
</template>

<script>
export default {
  props: ["title", "cols", "items"],
  computed: {},
  methods: {
    bg_color(item) {
      if (item.RGB) {
        return this.$tools.rgbToHex(item.RGB);
      } else {
        return "#cfb6d4";
      }
    },
    on_click(item) {
      this.$emit("click_tecla", item);
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

<style scoped>
span {
  white-space: normal;
}
</style>
