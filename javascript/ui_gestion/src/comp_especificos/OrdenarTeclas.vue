<template>
  <v-card>
    <v-card-title>
      <v-toolbar class="w-100" dark color="primary">
        <v-btn icon dark @click="$emit('close')">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>Ordenar</v-toolbar-title>
        <v-spacer></v-spacer>
      </v-toolbar>
    </v-card-title>
    <v-card-text>
      <v-row class="pa-2">
        <v-col class="ma-1" cols="12" v-for="(item, i) in items" :key="i">
          <v-card>
            <v-row class="pa-3">
              <v-col cols="8">{{ item.nombre }}</v-col>
              <v-col cols="4">
                <valle-float-form-vue
                  :item="item"
                  :column="column"
                  :tb_name="tb_name"
                  @change="on_click_ordenar"
                  location="bottom end"
                ></valle-float-form-vue>
              </v-col>
            </v-row>
          </v-card>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>
<script>
import ValleFloatFormVue from "@/components/ValleFloatForm.vue";
export default {
  components: { ValleFloatFormVue },
  props: ["items", "tb_name", "column"],
  methods: {
    on_click_ordenar() {
      this.$emit("change");
    },
  },
};
</script>
