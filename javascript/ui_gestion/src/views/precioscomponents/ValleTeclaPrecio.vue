<template>
    <v-expansion-panel
      >
      <v-expansion-panel-title>
          <v-row>
            <v-col>
              {{ tecla.nombre }}
            </v-col>
            <v-col>
              {{ tecla.p1 }}
            </v-col>
            <v-col>
              {{ tecla.p2 }}
            </v-col>
          </v-row>
        </v-expansion-panel-title>
    </v-expansion-panel>
</template>

<script>
import { mapState, mapAcitons } from "vuex"
export default {
  props: [ "tecla" ],  
}
</script>