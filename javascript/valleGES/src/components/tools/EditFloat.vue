<template>
  <div>
    <v-row class="pa-2">
      <v-col cols="12" class="text-caption pa-0 ma-0">{{displayName}}</v-col>
      <v-col cols="12" class="pa-0 ma-0">
        <v-menu v-model="menuVisible" :close-on-content-click="false" width="200px">
          <template #activator="{ props }">
            <v-btn text block v-bind="props" class="text-none" >
              {{ tempValue }}</v-btn>
          </template>
          <v-card>
            <v-card-text>
              <v-text-field v-model="tempValue" :type="type" :label="displayName"
                @keydown.enter="updateValue"></v-text-field>
            </v-card-text>
          </v-card>
        </v-menu>
      </v-col>
    </v-row>
  </div>
</template>
  
<script>
export default {
  props: {
    titulo: {
      type: String,
      default: null,
    },  
    value: {
      type: Object,
      required: true,
    },
    field: {
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
      tempValue: this.value[this.field],
    };
  },
  computed: {
    displayName() {
      return this.titulo || this.field.toUpperCase();
    },
  },
  methods: {
    updateValue() {
      this.value[this.field] = this.tempValue;
      this.menuVisible = false;
      this.$emit("change", this.value);
    },
  },
  watch: {
    value: {
      handler(newVal) {
        this.tempValue = newVal[this.field];
      },
      deep: true,
    },
  },
};
</script>
  