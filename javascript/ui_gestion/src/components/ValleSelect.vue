<template>
  <v-select v-model="localValue" :items="items" hide-details="auto"> </v-select>
</template>

<script>
export default {
  props: ["modelValue", "values", "items"],
  computed: {
    localValue: {
      get() {
        if (this.values) {
          var index = this.values.indexOf(this.modelValue);
          return this.items[index];
        }
        return this.modelValue;
      },
      set(v) {
        var aux = v;
        if (this.values) {
          var index = this.items.indexOf(aux);
          aux = this.values[index];
        }
        this.$emit("update:modelValue", aux);
      },
    },
  },
};
</script>
