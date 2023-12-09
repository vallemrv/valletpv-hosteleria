<template>
  <v-table fixed-header>
    <thead v-if="items && items.length > 0">
      <tr>
        <th class="text-center" v-for="(h, i) in headers" :key="i" v-show="isVisible()">
          {{ h.toUpperCase() }}
        </th>
        <th class="text-center pa-0 ma-0" v-if="tools">HERRAMIENTAS</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(item, i) in items" :key="i">
        <td class="text-center pa-0 ma-0" v-for="(col, j) in columns" :key="j" v-show="isVisible(item)">
          <v-container v-if="col.float">
            <valle-float-form :item="item" :column="col.col" :tipo="col.tipo" :rules="col.rules"
              :tb_name="tb_name"></valle-float-form>
          </v-container>
          <v-container v-else>{{ col.key ? getName(col, item) : item[col] }}</v-container>
        </td>
        <td class="text-center" v-if="tools">
          <v-menu anchor="start bottom" origin="auto">
            <template v-slot:activator="{ props }">
              <v-btn :id="'btn_' + i" v-bind="props" size="small"
                color="#cfb6d4"><v-icon>mdi-hammer-wrench</v-icon></v-btn>
            </template>
            <v-list>
              <v-list-item v-for="(t, s) in tools" :key="s" @click="on_click_tools(item, t.op, i)" link
                :prepend-icon="t.icon ? t.icon : ''" :title="t.text"></v-list-item>
            </v-list>
          </v-menu>
        </td>
      </tr>
    </tbody>
  </v-table>
</template>

<script>
import ValleFloatForm from "@/components/ValleFloatForm.vue";
import { mapGetters } from "vuex";
export default {
  components: { ValleFloatForm },
  props: ["items", "columns", "headers", "tools", "tb_name"],
  computed: {
    ...mapGetters(["getItemsFiltered"]),
    
  },
  methods: {
    isVisible(show) {
      console.log(show)
      return window.innerWidth <= 768; // Por ejemplo, considera móvil si el ancho es de 768px o menos
    },
    getName(col, item) {
      var f = {
        filters: [],
      };
      var obj = {};
      obj[col.key] = item[col.col];
      f.filters.push(obj);
      var v = this.getItemsFiltered(f, col.tb_name);
      if (v.length > 0) return v[0][col.value];
      return "";
    },
    on_click_tools(v, op, btn) {
      this.$emit("click_tools", v, op);
      document.getElementById("btn_" + btn).click();
    },
  },
};
</script>

<style scoped>
.v-btn {
  padding: 0px;
  margin: 0px;
  width: 28px;
}
</style>
