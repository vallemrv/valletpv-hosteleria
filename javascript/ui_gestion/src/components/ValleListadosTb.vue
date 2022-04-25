<template>
  <v-table fixed-header>
    <thead>
      <tr>
        <th v-for="(h, i) in headers" :key="i">
          {{ h.toUpperCase() }}
        </th>
        <th class="text-center">HERRAMIENTAS</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(item, i) in items" :key="i">
        <td v-for="(reg, j) in columns" :key="j">{{ item[reg] }}</td>
        <td class="text-center">
          <v-menu anchor="start bottom" origin="auto">
            <template v-slot:activator="{ props }">
              <v-btn :id="'btn_' + i" v-bind="props" size="small" color="#cfb6d4"
                ><v-icon>mdi-hammer-wrench</v-icon></v-btn
              >
            </template>
            <v-list>
              <v-list-item
                v-for="(t, s) in tools"
                :key="s"
                @click="on_click_tools(item, t.op, i)"
                link
                :prepend-icon="t.icon ? t.icon : ''"
                :title="t.text"
              ></v-list-item>
            </v-list>
          </v-menu>
        </td>
      </tr>
    </tbody>
  </v-table>
</template>

<script>
export default {
  props: ["items", "columns", "headers", "tools"],
  methods: {
    on_click_tools(v, op, btn) {
      this.$emit("on_click_tools", v, op);
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
