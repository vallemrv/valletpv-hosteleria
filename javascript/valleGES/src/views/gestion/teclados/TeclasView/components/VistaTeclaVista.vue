<template>
    <v-col cols="12">
      <v-card>
        <v-card-text>
          <v-row class="text-center">
            <v-col cols="4" class="text-left">Texto tecla</v-col>
            <v-col cols="4">Texto ticket</v-col>
            <v-col cols="4">Texto recepcion</v-col>
          </v-row>
        </v-card-text>
      </v-card>
    </v-col>

    <valle-listado-vista-tecla v-for="(tecla, i) in listTeclas" :key="i" :tecla="tecla">
    </valle-listado-vista-tecla>
  
</template>

<script>
import ValleListadoVistaTecla from "./ValleListadoVistaTecla";
export default {
    props:["listTeclas"],
    components:{ ValleListadoVistaTecla}
}
</script>