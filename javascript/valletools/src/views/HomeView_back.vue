<template>
  <v-app>
    <valle-login v-if="token == null"></valle-login>
    <div v-else>
      <valle-header></valle-header>
      <v-main app>
        <v-container><router-view></router-view></v-container>
      </v-main>
      <valle-footer></valle-footer>
    </div>
  </v-app>
</template>

<script>
import { defineComponent } from 'vue';
import ValleHeader from '../components/ValleHeader';
import ValleFooter from '../components/ValleFooter';
import ValleLogin from '../components/ValleLogin';

export default defineComponent({
  name: 'HomeView',

  components: {
   ValleHeader,
   ValleFooter,
   ValleLogin
  },
});
</script>
