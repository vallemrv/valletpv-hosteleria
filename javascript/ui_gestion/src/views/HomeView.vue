<template>
<valle-main-header></valle-main-header>
<v-container>
  <v-layout class="text-center mt-10">
      <v-row>
        <v-col cols="12">
          <v-img height="300px" src="/static/assets/logo_v3.png"></v-img>
        </v-col>
        <v-col cols="12"  class="ma-0 pa-0">
          <v-label class="text-subtitle-1 font-weight-black">
              Aplicacion de gestion para ValleTPV
            </v-label>
        </v-col>
        <v-col cols="12" class="ma-0 pa-0 mb-5">
            <v-label  class="text-subtitle-2 font-weight-black">
                Crea empresas, o elige una.
            </v-label>
        </v-col>
      </v-row>
  </v-layout>
  </v-container>
</template>
<script>
import ValleMainHeader from '@/components/ValleMainHeader.vue';
export default {
  components: { ValleMainHeader },
  data: () => ({
  }),
};
</script>
