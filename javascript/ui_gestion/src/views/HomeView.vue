<template>
<valle-main-header :items="items"></valle-main-header>
<v-container>
  <v-layout class="text-center mt-10">
      <v-row>
        <v-col cols="12">
          <v-img height="300px" src="/assets/logo_v3.png"></v-img>
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
    items: [
           {label: "Camareros", icon: "mdi-account",  value:"camareros"},
           {label: "Teclas", icon: "mdi-food-turkey",  value:"teclas"},
           {label: "Teclas por familias", icon: "mdi-family-tree",  value:"byfamilias"},
           {label: "TecladoTPV", icon: "mdi-monitor",  value:"tecladostpv"},
           {label: "TecladoCOM", icon: "mdi-cellphone",  value:"tecladoscom"},
           {label: "Secciones", icon: "",  value:"secciones"},
           {label: "Mesas", icon: "",  value:"mesas"},
           {label: "Zonas", icon: "",  value:"zonas"},
           {label: "Receptores", icon: "",  value:"receptores"},
           {label: "Familias", icon: "",  value:"familias"},

        ],
  }),
};
</script>
