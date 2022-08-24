<template>
    <valle-tool-bar title="Arqueos"></valle-tool-bar>
    <v-container>
        <v-row>
        <v-col cols="12" v-for="(a, i) in listadoarqueos" :key="i">
            <v-card>
                <v-card-title>
                    <v-sheet elevation="2" color="blue" class="w-100 pa-4">
                        <v-row class="pa-3">
                            <v-col cols="6" class="pa-0 ma-0">
                                Fecha: {{ a.fecha }}
                            </v-col>
                            <v-col cols="6" class="pa-0 ma-0">
                                Hora: {{ a.hora }}
                            </v-col>
                            <v-col cols="12" class="pa-0 ma-0">
                                Descuadre: {{ parseFloat(a.descuadre).toFixed(2) }}€
                            </v-col>
                            <v-col cols="12" class="pa-0 ma-0">
                                Total efectivo: {{ parseFloat(a.totalefectivo_ticado).toFixed(2) }}€
                            </v-col>
                            <v-col cols="12" class="pa-0 ma-0">
                                Total tarjeta: {{ parseFloat(a.totaltarjeta).toFixed(2) }}€
                            </v-col>
                            <v-col cols="12" class="pa-0 ma-0">
                                Gastos: {{ parseFloat(a.gastos).toFixed(2) }}€
                            </v-col>
                        </v-row>
                    </v-sheet>
                </v-card-title>
                <v-card-text>

                </v-card-text>
            </v-card>
        </v-col>
    </v-row>
    </v-container>
</template>

<script>
import ValleToolBar from '@/components/ValleToolBar.vue'
import { mapActions, mapState } from 'vuex';
export default {
    components:{ ValleToolBar },
    computed:{
        ...mapState(["listadoarqueos"])
    },
    methods:{
        ...mapActions(["getListadoArqueos"])
    },
    mounted(){
        this.getListadoArqueos()
    }
}
</script>