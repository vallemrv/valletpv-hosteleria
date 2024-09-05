<template>
  <v-menu>
    <template v-slot:activator="{ props }">
      <v-btn :color="col_sel" v-bind="props"> {{ label }} </v-btn>
    </template>
    <v-list>
      <v-list-item>
        <v-list-item-title>
          <v-color-picker v-model="color_picker"> </v-color-picker>
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script>
export default {
  props: ["modelValue", "label"],
  computed: {
    col_sel() {
      return this.$tools.rgbToHex(
        this.color_picker.r + "," + this.color_picker.g + "," + this.color_picker.b
      );
    },
    color_picker: {
      get: function () {
        var color_sel = { r: 255, g: 0, b: 255, a: 1 };
        if (this.modelValue && this.modelValue != "") {
          var color_item = this.modelValue.split(",");
          color_sel = { r: color_item[0], g: color_item[1], b: color_item[2], a: 1 };
        }
        return color_sel;
      },
      set: function (v) {
        var str_color = v.r + "," + v.g + "," + v.b;
        this.$emit("update:modelValue", str_color);
      },
    },
  },
};
</script>
