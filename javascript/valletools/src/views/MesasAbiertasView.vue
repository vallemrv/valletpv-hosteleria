<template>
  <valle-tool-bar title="Mesas abiertas"></valle-tool-bar>
  <v-container> </v-container>
</template>

<script>
import ValleToolBar from "@/components/ValleToolBar.vue";
import { mapState, mapActions } from "vuex";
export default {
  components: { ValleToolBar },
  data() {
    return {};
  },
  computed: {
    ...mapState(["infomesas"]),
  },
  methods: {
    ...mapActions(["getMesasAbiertas"]),
  },
  watch: {
    mesasabiertas(v) {
      if (!v) {
        this.getMesasAbiertas();
      }
    },
  },
  mounted() {
    if (!this.infmesas) {
      this.getMesasAbiertas();
    }
  },
};
</script>
