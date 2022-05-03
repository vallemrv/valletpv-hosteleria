<template>
  <v-row v-if="collapsed" class="pa-0 ma-0 text-center">
    <v-col cols="12">
      <v-expansion-panels>
        <v-expansion-panel>
          <v-expansion-panel-title> Botonera </v-expansion-panel-title>
          <v-expansion-panel-text>
            <v-row class="pa-2">
              <v-col cols="3" class="pa-0 ma-0" v-for="(item, i) in items" :key="i">
                <v-sheet class="pa-1">
                  <v-btn
                    block
                    class="text-caption"
                    @click="on_click(item)"
                    :color="bg_color(item)"
                  >
                    <span>{{ item.nombre }}</span>
                  </v-btn>
                </v-sheet>
              </v-col>
            </v-row>
          </v-expansion-panel-text>
        </v-expansion-panel>
      </v-expansion-panels>
    </v-col>
  </v-row>
  <v-row v-else class="pa-0 ma-0 text-center">
    <v-col :cols="cols" class="pa-0 ma-0" v-for="(item, i) in items" :key="i">
      <v-sheet class="pa-1">
        <v-btn
          block
          class="text-caption"
          @click="on_click(item)"
          :height="collapsed ? '' : '80px'"
          :color="bg_color(item)"
        >
          <span>{{ item.nombre }}</span>
        </v-btn>
      </v-sheet>
    </v-col>
  </v-row>
</template>

<script>
export default {
  props: ["title", "cols", "items", "is_collapsible"],
  computed: {
    collapsed() {
      return (
        this.is_collapsible && this.is_collapsible.includes(this.$vuetify.display.name)
      );
    },
  },
  methods: {
    bg_color(item) {
      if (item.rgb) item.RGB = item.rgb;
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
