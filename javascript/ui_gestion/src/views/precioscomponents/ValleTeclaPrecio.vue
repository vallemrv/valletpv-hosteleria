<template>
  <v-row class="pa-2 pl-5 pr-5">
    <v-col  cols="12" class="ma-0 pa-0 pl-3 pr-3" v-if="subteclas.length > 0">
      <v-expansion-panels >
        <v-expansion-panel>
            <v-expansion-panel-title>
                <v-row>
                  <v-col class="pt-5">
                    {{ tecla.nombre }}
                  </v-col>
                  <v-col>
                    <valle-float-form
                        :item = tecla
                        column = "p1"
                        app="gestion"
                        tb_name="teclas"
                        :rules="rules"
                        :value="tecla.p1"
                    ></valle-float-form>
                  </v-col>
                  <v-col>
                    <valle-float-form
                        :item = tecla
                        column = "p2"
                        app="gestion"
                        tb_name="teclas"
                        :rules="rules"
                        :value="tecla.p2"
                    ></valle-float-form>
                  </v-col>
                </v-row>
            </v-expansion-panel-title>
            <v-expansion-panel-text class="text-center">
                <v-row
                  v-for="(s,i) in subteclas"
                  :key="i"
                  >
                  <v-col class="text-left" cols="4">{{s.nombre}}</v-col>
                  <v-col cols="4">
                    <valle-float-form
                        :item = s
                        column = "incremento"
                        app="gestion"
                        tb_name="subteclas"
                        :rules="rules"
                        :value="precio(s, tecla.p1)"
                    ></valle-float-form>
                    </v-col>
                  <v-col cols="4"><valle-float-form
                        :item = s
                        column = "incremento"
                        app="gestion"
                        tb_name="subteclas"
                        :rules="rules"
                        :value="precio(s, tecla.p2)"
                    ></valle-float-form></v-col>
                </v-row>
            </v-expansion-panel-text>
        </v-expansion-panel>
      </v-expansion-panels> 
    </v-col>
    <v-col cols="12" class="ma-0" v-else>
      <v-card elevation="3" class="mx-auto text-center">
          <v-row class="pl-3 pr-10">
              <v-col cols="4">
                {{ tecla.nombre }}
              </v-col>
              <v-col cols="4">
                <valle-float-form
                        :item = tecla
                        column = "p1"
                        app="gestion"
                        tb_name="teclas"
                        :rules="rules"
                        :value="tecla.p1"
                    ></valle-float-form>
              </v-col>
              <v-col cols="4">
                <valle-float-form
                        :item = tecla
                        column = "p2"
                        app="gestion"
                        tb_name="teclas"
                        :rules="rules"
                        :value="tecla.p2"
                    ></valle-float-form>
              </v-col>
          </v-row>
      </v-card>
    </v-col>
  </v-row>  
</template>

<script>
import { mapGetters } from "vuex"
import ValleFloatForm from "@/components/ValleFloatForm.vue"
export default {
  components: { ValleFloatForm },
  props: [ "tecla" ],
  data: () => {
    return {
       subteclas: [],
       rules: [
          value => !!value || 'Requerido.',
          value => (!isNaN(value - parseFloat(value))) || 'Tiene que ser precio',
       ]
    }
  },
  computed:{
    ...mapGetters(["getSubTeclasByTecla"]),
  },
  methods:{
     precio(s, p){
       return (parseFloat(s.incremento) + parseFloat(p)).toFixed(2)
     },
  },
  watch: {
     tecla(v){
        this.subteclas = this.getSubTeclasByTecla(this.tecla.id)
     }
  },
  mounted() {
     this.subteclas = this.getSubTeclasByTecla(this.tecla.id)
  },

}
</script>