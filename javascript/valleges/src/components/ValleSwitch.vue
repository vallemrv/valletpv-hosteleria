<template>
  <v-switch v-model="localValue" color="succes" hide-details :label="label"> </v-switch>
</template>

<script>
export default {
  props: ["modelValue", "item", "label"],
  computed: {
    localValue: {
      get() {
        return this.modelValue == 1 ? true : false;
      },
      set(v) {
        var aux = v ? 1 : 0;
        this.$emit("update:modelValue", aux);
        this.$emit("change_value", this.item);
      },
    },
  },
};
</script>
