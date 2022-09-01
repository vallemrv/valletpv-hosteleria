<template>
   <v-dialog fullscreen v-model="show">
       
       <v-card>
            <v-toolbar fixed color="blue">
                <v-toolbar-title>Arqueo {{ arqueo ? arqueo.fecha  : ""}}</v-toolbar-title>            
                <v-spacer></v-spacer>
                <v-btn @click="show=false"><v-icon>mdi-close</v-icon></v-btn>
            </v-toolbar>
           <v-card-text>
                <v-row>
                    <v-col cols="12" class="text-center text-h4">
                        {{ arqueo ? arqueo.fecha  : ""}} --- {{ arqueo ? arqueo.hora  : ""}}
                    </v-col>
                    <v-col cols="12">
                <v-sheet elevation="2" class="mt-2">
                    <v-row class="pl-5 pa-3">    
                        <v-col cols="12" class="pa-0 ma-0">
                            Total efectivo: {{ parseFloat(arqueo.totalefectivo).toFixed(2) }}€
                        </v-col>
                        <v-col cols="12" class="pa-0 ma-0">
                            Total tarjeta: {{ parseFloat(arqueo.totaltarjeta).toFixed(2) }}€
                        </v-col>
                        <v-col cols="12" class="pa-0 ma-0">
                            Gastos: {{ parseFloat(arqueo.gastos).toFixed(2) }}€
                        </v-col>
                        <v-col cols="12" class="pa-0 ma-0">
                            Total ticado: {{ parseFloat(arqueo.totalefectivo_ticado).toFixed(2) }} €
                        </v-col>
                        <v-col cols="12" class="pa-0 ma-0">
                            Descuadre: {{ parseFloat(arqueo.descuadre).toFixed(2) }} €
                        </v-col>
                    </v-row>    
                </v-sheet>
               </v-col>
                <v-col cols="12">
                    <v-card>
                        <v-card-title>Cambio</v-card-title>
                        <v-card-text>
                            <v-row>
                                <v-col cols="12" v-for="(c, i) in arqueo.desglose_efectivo.lineas_cambio" :key="i">
                                     <v-row>
                                        <v-col cols="2" class="text-right">
                                            {{c.can}}
                                        </v-col>
                                        <v-col cols="3" class="text-left">
                                            {{c.texto_tipo}}
                                        </v-col>
                                        <v-col cols="2" class="text-right">
                                            {{c.tipo}}
                                        </v-col>
                                        <v-col cols="4" class="text-right">
                                            {{c.can * c.tipo}}€
                                        </v-col>
                                     </v-row>
                                </v-col>
                                <v-col cols="12" class="text-center text-h5">
                                   Total cambio {{ getTotalSum( arqueo.desglose_efectivo.lineas_cambio) }} €
                                </v-col>
                            </v-row>
                        </v-card-text>
                    </v-card>
                </v-col>

                <v-col cols="12">
                    <v-card>
                        <v-card-title>Retirar</v-card-title>
                        <v-card-text>
                            <v-row>
                                <v-col cols="12" v-for="(r, ri) in arqueo.desglose_efectivo.lineas_retirar" :key="ri">
                                     <v-row>
                                        <v-col cols="2" class="text-right">
                                            {{r.can}}
                                        </v-col>
                                        <v-col cols="3" class="text-left">
                                            {{r.texto_tipo}}
                                        </v-col>
                                        <v-col cols="2" class="text-right">
                                            {{r.tipo}}
                                        </v-col>
                                        <v-col cols="4" class="text-right">
                                            {{r.can * r.tipo}} €
                                        </v-col>
                                     </v-row>
                                </v-col>
                                <v-col cols="12" class="text-center text-h5">
                                    Total retirar {{ getTotalSum( arqueo.desglose_efectivo.lineas_retirar) }} €
                                </v-col>
                            </v-row>
                        </v-card-text>
                    </v-card>
                </v-col>

                <v-col cols="12">
                    <v-card>
                        <v-card-title>Gastos</v-card-title>
                        <v-card-text>
                            <v-row>
                                <v-col cols="12" v-for="(g, gi) in arqueo.des_gastos" :key="gi">
                                     <v-row>
                                        <v-col cols="7" class="text-left">
                                            {{g.Descripcion}}
                                        </v-col>
                                        <v-col cols="5" class="text-right">
                                            {{g.Importe}} €
                                        </v-col>
                                     </v-row>
                                </v-col>
                                
                            </v-row>
                        </v-card-text>
                    </v-card>
                </v-col>
               </v-row>
           </v-card-text>
       </v-card>
   </v-dialog>

</template>
<script>

export default{
    props:["arqueo", "show"],
    emits:["close"],
    
    watch:{
        show(v){
            if (!v){
                this.$emit("close")
            }
        }
    },
    methods:{
        getTotalSum(l){
            let subtotal = 0
            for(let i=0;i < l.length; i++){
                subtotal += l[i].can * l[i].tipo
            }  
            return parseFloat(subtotal).toFixed(2);      
        }
    }
}
</script>