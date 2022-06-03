<template>
  <v-app>
    <v-main>
      <router-view></router-view>
      <valle-footer></valle-footer>
    </v-main>
  </v-app>
</template>

<script>
import ValleFooter from '@/components/ValleFooter.vue'
export default {
  name: 'App',
  components:{ValleFooter },
  data: () => ({
    //
  }),
}
</script>
