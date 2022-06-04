<template>
    <valle-toolbar title="Mensajes"></valle-toolbar>
    <v-container>
      <v-row>
      <v-col cols="12" v-for="(men, i) in mensajes" :key="i">
        <v-banner elevation="2">
         <v-banner-text>{{ men.mensaje }}</v-banner-text>
         <v-banner-actions>
            <v-btn icon @click="gestionarAlertas({idpeticion:men.idpeticion, aceptada:'0'})"><v-icon>mdi-delete</v-icon></v-btn>
            <v-btn icon @click="gestionarAlertas({idpeticion:men.idpeticion, aceptada:'1'})" v-if="men.tipo=='peticion'"><v-icon>mdi-check</v-icon> </v-btn>
         </v-banner-actions>
        </v-banner>
      </v-col>
    </v-row>
    </v-container>
    
</template>


<script>
import  ValleToolbar  from '@/components/ValleToolBar'
import { mapState, mapActions } from "vuex"
export default {
    components:{ ValleToolbar },
    computed:{
        ...mapState(['mensajes'])
    },
    methods:{
      ...mapActions(["gestionarAlertas"])
    }
}
</script>