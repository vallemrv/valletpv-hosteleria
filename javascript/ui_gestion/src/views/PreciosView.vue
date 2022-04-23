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
        <valle-tecla-precio
            v-for="(tecla, i) in listTeclas"
            :key="i"
            :tecla="tecla">
        </valle-tecla-precio>
         
   </div>
</template>

<script>
import { mapState, mapActions, mapGetters } from "vuex"
import ValleTeclaPrecio from './precioscomponents/ValleTeclaPrecio' 
import ValleFilterPrecio from './precioscomponents/ValleFilterPrecio'

export default {
    components: { ValleTeclaPrecio, ValleFilterPrecio },
    methods: {
        ...mapActions(["getTeclados"])
    },
    computed:{
        ...mapState(["ocupado", "teclas", "subteclas", "secciones"]),
        ...mapGetters(["getTeclasBySec"]),
        listTeclas(){
            return this.getTeclasBySec()
        }
    },
    mounted() {
        if (this.teclas.length == 0){
            this.getTeclados()
        }else{
            this.$store.state.secFilter = []
            this.$store.state.secciones = this.secciones.map( s => {
                s.isSelected = false
                return s
            })
        }
    },
}
</script>