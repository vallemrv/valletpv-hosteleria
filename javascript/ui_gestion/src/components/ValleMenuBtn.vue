<template>
  <v-row>
    <v-col cols="6"  md="3"  v-for="(item, i) in items" :key="i">
      <router-link :to="item.link" custom v-slot="{ navigate, href }">
        <v-btn plain color="#cfb6d4" :href="href" @click="navigate">{{
          item.title
        }}</v-btn>
      </router-link>
    </v-col>
  </v-row>
</template>

<script>
export default {
  props: ["items"],
};
</script>

<style scoped>
.v-btn {
  width: 100%;
  height: 100px;
}
</style>
