<template>
   <div>
    <h1>Precios
        <v-progress-circular
        indeterminate
        color="primary"
        v-if="ocupado"
        ></v-progress-circular>
    </h1>

       <valle-filter-precio></valle-filter-precio>
    
      <v-expansion-panels class="pa-5">
            <valle-tecla-precio
            v-for="(tecla, i) in teclas"
            :key="i"
            :tecla="tecla"></valle-tecla-precio>
         
      </v-expansion-panels>
    </div>
</template>

<script>
import { mapState, mapActions } from "vuex"
import ValleTeclaPrecio from './precioscomponents/ValleTeclaPrecio' 
import ValleFilterPrecio from './precioscomponents/ValleFilterPrecio'

export default {
    components: { ValleTeclaPrecio, ValleFilterPrecio },
    methods: {
        ...mapActions(["getTeclados"])
    },
    computed:{
        
        ...mapState(["ocupado", "teclas", "subteclas"])
    },
    mounted() {
        if (this.teclas == null){
           this.getTeclados()
        }
    },
}
</script>