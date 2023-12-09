<template>
  <valle-main-header :items="items"></valle-main-header>
  <v-container>
    <v-row class="text-center mt-10">
      <v-col cols="12">
        <v-img height="300px" src="@/assets/logo_v3.png"></v-img>
      </v-col>
      <v-col cols="12" class="ma-0 pa-0">
        <div class="text-subtitle-1 font-weight-bold">
          Aplicacion de gestion para ValleTPV
        </div>
      </v-col>
      <v-col cols="12" class="ma-0 pa-0 mb-5">
        <div v-if="!empresa" class="text-subtitle-2 font-weight-bold">
          Crea empresas, o elige una.
        </div>
        <div v-else class="font-weight-bold">
          Estas gestionando {{ empresa.nombre_server }}
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import ValleMainHeader from '@/components/ValleMainHeader.vue';
import { mapState } from 'vuex';

export default {
  components: { ValleMainHeader },
  data: () => ({
    items: [
      {label: "Camareros", icon: "mdi-account",  value:"camareros"},
      {label: "Teclas", icon: "mdi-food-turkey",  value:"teclas"},
      {label: "Teclas por familias", icon: "mdi-family-tree",  value:"byfamilias"},
      {label: "TecladoTPV", icon: "mdi-monitor",  value:"tecladostpv"},
      {label: "TecladoCOM", icon: "mdi-cellphone",  value:"tecladoscom"},
      {label: "Composiciones", icon: "mdi-view-compact",  value:"composicion"},
      {label: "Secciones", icon: "",  value:"secciones"},
      {label: "Mesas", icon: "",  value:"mesas"},
      {label: "Zonas", icon: "",  value:"zonas"},
      {label: "Receptores", icon: "",  value:"receptores"},
      {label: "Familias", icon: "",  value:"familias"},
    ],
  }),
  computed: {
    ...mapState(["empresa"])
  },
};
</script>
