<template>
  <valle-editor-item
    :title="title"
    :filtro="filtro"
    :tb_name="tb_name"
    :tabla="tabla"
    :tools="tools"
    @on_click_filter="on_click_filter"
    @on_click_tools="on_click_tools"
  >
  </valle-editor-item>
</template>

<script>
import { mapGetters, mapState, mapActions } from "vuex";
import ValleEditorItem from "@/components/ValleEditorItem.vue";
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";

export default {
  components: { ValleEditorItem, ValleDialogoForm },
  computed: {
    ...mapGetters(["getItemsFiltered"]),
    ...mapState(["camareros"]),
  },
  data() {
    const col = ["nombre", "apellidos", "permisos"];
    return {
      title: "Camareros",
      tb_name: "camareros",
      localFilter: [],
      filtro: {
        caption: ["No activos", "activos"],
        tools: ["no_activos", "activos"],
        filters: [
          { activo: 1, autorizado: 0 },
          { activo: 1, autorizado: 1 },
        ],
        all: [
          { activo: 1, autorizado: 0 },
          { activo: 1, autorizado: 1 },
        ],
        multiple: false,
      },
      tabla: {
        headers: col,
        keys: col,
      },
      multiple_tools: {
        activos: [
          { op: "minus", text: "Desautorizar", icon: "mdi-account-minus" },
          {
            op: "unlock",
            text: "Desbloquear",
            icon: "mdi-lock-open",
          },
        ],
        no_activos: [
          { op: "plus", text: "Autorizar", icon: "mdi-account-plus" },
          {
            op: "unlock",
            text: "Desbloquear",
            icon: "mdi-lock-open",
          },
        ],
        all: null,
      },
      tools: [],
    };
  },
  methods: {
    ...mapActions(["getListado", "addInstruccion"]),
    cargar_reg() {
      let params = new FormData();
      params.append("tb", this.tb_name);
      this.getListado({ params: params });
    },
    on_visible_change(value) {
      this.showDialog = value;
    },
    on_click_filter(lfilter) {
      this.localFilter = lfilter;
      var selected = lfilter.selected;
      if (selected && selected.length > 0) {
        var op = this.filtro.tools[selected[0]];
        this.tools = this.multiple_tools[op];
      } else {
        this.tools = this.multiple_tools["all"];
      }
      this.$store.state.itemsFiltrados = this.getItemsFiltered(
        this.localFilter,
        this.tb_name
      );
    },

    on_click_tools(v, op) {
      var inst = {};
      switch (op) {
        case "minus":
          inst = {
            tb: this.tb_name,
            reg: { autorizado: 0 },
            tipo: "md",
            id: v.id,
          };
          v.autorizado = 0;
          break;
        case "plus":
          inst = {
            tb: this.tb_name,
            reg: { autorizado: 1 },
            tipo: "md",
            id: v.id,
          };
          v.autorizado = 1;
          break;
        case "unlock":
          inst = {
            tb: this.tb_name,
            reg: { pass_field: "" },
            tipo: "md",
            id: v.id,
          };
          v.pass_field = "";
          break;
      }
      this.addInstruccion({ inst: inst });
      this.on_click_filter(this.localFilter);
    },
  },
  watch: {
    camareros(v) {
      if (v) {
        this.on_click_filter(this.localFilter);
      } else {
        this.cargar_reg();
      }
    },
  },
  mounted() {
    this.localFilter = Object.values(this.filtro);
    this.localFilter.filters = Object.values(this.filtro.all);
    if (this.camareros) {
      this.on_click_filter(this.localFilter);
    } else {
      this.cargar_reg();
    }
  },
};
</script>
