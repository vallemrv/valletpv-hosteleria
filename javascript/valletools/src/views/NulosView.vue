<template>
  <valle-tool-bar title="Historial nulos"></valle-tool-bar>
  <v-container>
    <v-row>
      <v-col cols="12" v-if="ocupado">
        <div class="text-center" v-if="ocupado">
          <v-progress-circular indeterminate color="primary"></v-progress-circular>
        </div>
      </v-col>
      <v-col cols="12" v-for="(n, i) in nulos" :key="i">
        <v-card>
          <v-card-text>
            <v-sheet color="primary" class="pa-5" elevation="3" >
              <v-row>
                <v-col cols="9" class="mt-4 pl-6">
                    <v-row>
                    <v-col cols="12" class="pa-0 ma-0">
                      Mesa: {{ n.nomMesa }} --- Hora: {{ n.hora }}
                    </v-col>
                    <v-col cols="12" class="pa-0 ma-0">
                      Camarero: {{ n.camarero}}
                    </v-col>
                  </v-row>
                </v-col>
                 
                <v-col cols="3" class="text-right">
                   <v-btn icon flat color="primary" @click="mostrar(n)">
                    <v-icon color="white">mdi-eye</v-icon>
                  </v-btn>
                </v-col>
              </v-row>
            </v-sheet>
             <v-row v-for="(l, r) in n.lineas" :key="r" class="pt-4 pr-3 pl-3">
                <v-col cols="2" class="text-right"> {{ l.can }}</v-col>
                <v-col cols="6" class="text-left"> {{ l.descripcion }}</v-col>
                <v-col cols="4" class="text-right"> {{ parseFloat(l.precio).toFixed(2) }} €</v-col>
             </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
   
   <ValleInfMesa :mesa="mesaSel" :showMesa="showMesa" @close="showMesa=false" />

  </v-container>
</template>

<script>
import ValleToolBar from "@/components/ValleToolBar.vue";
import ValleInfMesa from "@/components/ValleInfMesa.vue";
import { mapState, mapActions } from "vuex";

export default {
  components: { ValleToolBar, ValleInfMesa },
  data(){
    return {
      showMesa: false,
      mesaSel: null,
    }
  },
  methods: {
    ...mapActions(["getNulos", "getInfMesa"]),
    mostrar(v){
      this.showMesa = true;
      this.mesaSel = v;
      this.getInfMesa(this.mesaSel.PK);
    }
  },
  computed: {
    ...mapState(["nulos", "ocupado"]),
  },
  mounted() {
      this.getNulos();
  },
};
</script>
