<template>
  <v-expansion-panels class="pa-5">
      <v-expansion-panel>
      <v-expansion-panel-title>Filtros</v-expansion-panel-title>
            <v-expansion-panel-text>
                <v-item-group multiple>
                <v-container>
                    <v-row>
                        <v-col
                        cols="3"
                        md="4"
                        v-for="(s, i) in secciones"
                        :key="i"
                        >
                        <v-item v-slot="{ isSelected, toggle }">
                            <v-card
                            :color="isSelected ? 'primary' : $tools.rgbToHex(s.rgb)"
                            class="d-flex align-center"
                            dark
                            height="50"
                            @click="toggle"
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
     computed:{
         ...mapState(['secciones'])
     }    
}
</script>