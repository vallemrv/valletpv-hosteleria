<template>
  <v-dialog v-model="show">
    <v-card width="550px" class="pa-5">
      <v-card-title>
        {{ data.title }}
      </v-card-title>
      <v-card-text>
        <v-row>
          <v-col :cols="data.cols" v-for="(item, i) in data.items" :key="i">
            <v-card height="100px" :color="bg_color(item)">
              <v-card-text>
                {{ item.nombre }}
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  props: ["show", "data"],
  computed: {},
  methods: {
    bg_color(item) {
      return this.$tools.rgbToHex(item.RGB);
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
