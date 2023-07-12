<template>
    <div>
      <v-menu offset-y>
        <template #activator="{ props }">
          <v-btn text v-bind="props" @click="menuVisible = true">{{ item[key] }}</v-btn>
        </template>
        <v-card>
          <v-card-text>
            <v-text-field
              v-model="tempValue"
              :type="type"
              label="Editar"
              @keydown.enter="updateValue"
            ></v-text-field>
          </v-card-text>
        </v-card>
      </v-menu>
    </div>
  </template>
  
  <script>
  export default {
    props: {
      value: {
        type: Object,
        required: true,
      },
      key: {
        type: String,
        required: true,
      },
      type: {
        type: String,
        default: "text",
        validator: (value) => {
          return ["text", "number"].includes(value);
        },
      },
    },
    data() {
      return {
        menuVisible: false,
        tempValue: this.value[this.key],
      };
    },
    methods: {
      updateValue() {
        this.value[this.key] = this.tempValue;
        this.menuVisible = false;
        this.$emit("change", this.value);
      },
    },
    watch: {
      value: {
        handler(newVal) {
          this.tempValue = newVal[this.key];
        },
        deep: true,
      },
    },
  };
  </script>
  