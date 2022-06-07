<template>
 <valle-tecla-precio v-for="(tecla, i) in listTeclas" :key="i" :tecla="tecla">
 </valle-tecla-precio>
</template>


<script>
import ValleTeclaPrecio from './ValleTeclaPrecio.vue'
export default {
    props: ["listTeclas"],
    components:{ ValleTeclaPrecio }
}
</script>