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
          <v-card-title>
            <v-alert color="primary" >
              {{ n.motivo }}
            </v-alert>
          </v-card-title>
          <v-card-text>
             <v-row>
              <v-col cols="6">
                Mesa: {{ n.nomMesa }}
              </v-col>
              <v-col cols="6">
                Hora: {{ n.hora }}
              </v-col>
              <v-col cols="2" class="text-right">
                {{ n.can }}
              </v-col>
              <v-col cols="8">
                {{ n.descripcion }}
              </v-col>
              <v-col cols="2">
                <v-btn flat icon @click="mostrar(n)"> <v-icon>mdi-eye</v-icon></v-btn>
              </v-col>
              
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
