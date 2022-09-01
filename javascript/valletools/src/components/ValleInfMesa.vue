<template>
<v-dialog v-model="showMesa" fullscreen>
      <v-app-bar class="bg-pink-lighten-3" fixed>
        <v-row>
            <v-col class="text-h6 mt-2" cols="5"> Mesa: {{ mesa.nomMesa }} </v-col>
            <v-col class="text-h6 mt-2 text-right" cols="5"> Hora: {{ mesa.hora }} </v-col>
            <v-col cols="2"> <v-btn icon @click="$emit('close')"><v-icon>mdi-close</v-icon></v-btn></v-col>
          </v-row>
      </v-app-bar>
      <v-card class="mt-10 mb-10 pb-10 pt-10" >
        <v-card-text>
          <div class="text-center" v-if="ocupado">
            <v-progress-circular indeterminate color="primary"></v-progress-circular>
          </div>
          <v-row>
            <v-col cols="12" v-for="(p, ip) in infmesa" :key="ip">
              <v-card>
                <v-card-title class="text-center pa-3"
                  >{{ p.camarero }} --- {{ p.hora }}</v-card-title
                >
                <v-card-text>
                  <v-row class="pa-4">
                    <v-col cols="12" v-for="(l, il) in p.lineas" :key="il">
                      <v-row :class="estadoToColor(l.Estado)">
                        <v-col cols="2" class="text-right"> {{ l.Can }} </v-col>
                        <v-col cols="6">{{ l.Descripcion }}</v-col>
                        <v-col cols="1" class="text-right">
                          {{  l.Estado }} </v-col
                        >
                        <v-col cols="3" class="text-right">
                          {{ parseFloat(l.Precio).toFixed(2) }} €</v-col
                        >
                      </v-row>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn v-if="puedo_cobrar" @click="showCobrarMesa = true">Cobrar</v-btn>
          <v-btn @click="showMesa=false">Cerrar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showCobrarMesa">
      <v-card class="pa-4">
        <v-card-title> Cobrar </v-card-title>
        <v-card-text>
          <p>Mesa: {{ mesa.nomMesa }}</p>
          <p>Total: {{ parseFloat(mesa.total_pedido).toFixed(2) }} €</p>
          Elige el modo de cobro
        </v-card-text>
        <v-card-actions>
          <v-btn variant="outlined" @click="cobrarMesa(mesa.total_pedido)"
            >Efectivo</v-btn
          >
          <v-btn variant="outlined" @click="cobrarMesa(0)">Tarjeta</v-btn>
          <v-btn variant="outlined" @click="showCobrarMesa = false">Cancelar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

</template>

<script>
import { mapState, mapActions } from "vuex";
export default{
    props:["puedo_cobrar", "showMesa", "mesa"],
    emits: ["close"],
    data(){
        return {
            showCobrarMesa: false,
        }
    },
    methods:{
        ...mapActions(["sendCobrarMesa"]),
        cobrarMesa(forma) {
            this.showCobrarMesa = false;
            this.sendCobrarMesa({ entrega: forma, pk: this.mesa.PK });
        },
        estadoToColor(e) {
            if (e == "P") return "bg-primary";
            else if (e == "C") return "bg-blue-lighten-5";
            else if (e == "A") return "bg-red-lighten-1";
            else if (e == "R") return "bg-pink-lighten-5";
            else if (e == "M") return "bg-pink-lighten-3";
        }
    },
    watch:{
      showMesa(v){
        if (!v){
          this.$emit("close");
        }
      }
    },
    computed: {
        ...mapState(["infmesa", "ocupado"]),
    },
    
}
</script>