<template>
    <valle-header></valle-header>
    <v-container>
       <ReactiveChart :chartData='chartSet'/>
    </v-container>
</template>

<script>
import ValleHeader from '@/components/ValleHeader.vue'
import ReactiveChart from '@/components/ReactiveChart.vue'
import { mapState } from 'vuex'
export default {
  components: { ValleHeader, ReactiveChart },
  computed:{
    ...mapState(["chartSet"])
  }
}
</script>