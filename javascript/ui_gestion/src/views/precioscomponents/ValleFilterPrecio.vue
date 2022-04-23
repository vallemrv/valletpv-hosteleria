<template>
  <v-expansion-panels class="pa-5">
      <v-expansion-panel>
      <v-expansion-panel-title>
         <v-row class="text-left">
            <v-col cols="12">Filtros</v-col>
            <v-col class="text-caption pa-0 ma-0" cols="12" v-if="filtros.length > 0">{{filtros}}</v-col>
         </v-row>
         </v-expansion-panel-title>
            <v-expansion-panel-text>
                <v-item-group multiple>
                <v-container>
                    <v-row>
                        <v-col
                        cols="4"
                        v-for="(s, i) in secciones"
                        :key="i"
                        >
                        <v-item v-slot="{  }">
                            <v-card
                            :color="s.isSelected ? 'primary' : $tools.rgbToHex(s.rgb)"
                            class="d-flex align-center text-caption"
                            dark
                            height="50"
                            @click="on_toggle(s)"
                            >
                            <v-scroll-y-transition>
                                <div
                                class="flex-grow-1 text-center"
                                >
                                {{ s.nombre }}
                                </div>
                            </v-scroll-y-transition>
                            </v-card>
                        </v-item>
                        </v-col>
                    </v-row>
                </v-container>
                </v-item-group>
            </v-expansion-panel-text>
        </v-expansion-panel>
      </v-expansion-panels>
</template>


<script>
import  {mapState } from 'vuex'

export default {
    data:()=>{
        return{
           filtros: []
        }
    },
    computed:{
        ...mapState(['secciones']),
    },
    methods: {
        on_toggle(s){
            s.isSelected = (s.isSelected ? !s.isSelected : true);
            if(s.isSelected){
                this.$store.state.secFilter.push(s.id);
                this.filtros.push(s.nombre)
            }else{
                this.filtros = this.filtros.filter( v => v != s.nombre)
                let filtered = this.$store.state.secFilter;
                filtered = filtered.filter( v =>  v != s.id )
                this.$store.state.secFilter = filtered;
            }
        }
    },   
}
</script>