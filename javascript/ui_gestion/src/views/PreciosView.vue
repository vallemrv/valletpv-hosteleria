<template>
  <div>
    <h1>
      Precios
      <v-progress-circular
        indeterminate
        color="primary"
        v-if="ocupado"
      ></v-progress-circular>
    </h1>
    <valle-filter-precio></valle-filter-precio>
    <valle-tecla-precio v-for="(tecla, i) in listTeclas" :key="i" :tecla="tecla">
    </valle-tecla-precio>
  </div>
</template>

<script>
import { mapState, mapActions, mapGetters } from "vuex";
import ValleTeclaPrecio from "./precioscomponents/ValleTeclaPrecio";
import ValleFilterPrecio from "./precioscomponents/ValleFilterPrecio";

export default {
  components: { ValleTeclaPrecio, ValleFilterPrecio },
  data() {
    return {
      tablas: ["teclas", "secciones", "subteclas"],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto"]),
  },
  computed: {
    ...mapState(["ocupado", "teclas", "subteclas", "secciones"]),
    ...mapGetters(["getTeclasBySec"]),
    listTeclas() {
      return this.getTeclasBySec();
    },
  },
  watch: {
    teclas(v) {
      if (!v) {
        this.getListadoCompuesto({ tablas: this.tablas });
      }
    },
    secciones(v) {
      if (!v) {
        this.getListadoCompuesto({ tablas: this.tablas });
      }
    },
    subteclas(v) {
      if (!v) {
        this.getListadoCompuesto({ tablas: this.tablas });
      }
    },
  },
  mounted() {
    if (!this.teclas || this.teclas.length == 0) {
      this.getListadoCompuesto({ tablas: this.tablas });
    } else {
      this.$store.state.secFilter = [];
      this.$store.state.secciones = this.secciones.map((s) => {
        s.isSelected = false;
        return s;
      });
    }
  },
};
</script>
