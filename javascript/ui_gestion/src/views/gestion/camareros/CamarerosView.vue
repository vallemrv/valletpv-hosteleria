<template>
  <valle-editor-item
    :title="title"
    :filtro="filtro"
    :tb_name="tb_name"
    :form="form"
    :tabla="tabla"
    :tools="tools"
    @click_filter="on_click_filter"
    @click_tools="on_click_tools"
  >
  </valle-editor-item>
  <valle-dialogo-form
    @close="on_close_dialogo"
    :show="showDialog"
    title="Editar"
    :item="itemSel"
    :form="form"
    :tb_name="tb_name"
    tipo="md"
  >
  </valle-dialogo-form>
</template>

<script>
import { mapGetters, mapState, mapActions } from "vuex";
import ValleEditorItem from "@/components/ValleEditorItem.vue";
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";

export default {
  components: { ValleEditorItem, ValleDialogoForm },
  computed: {
    ...mapGetters(["getItemsFiltered", "getListValues"]),
    ...mapState(["camareros", "permisoschoices"]),
    form() {
      return [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "apellidos", label: "1º Apellido", tp: "text" },
        {
          col: "permisos",
          label: "Permisos",
          choices: this.getListValues("permisoschoices", "choices"),
          tp: "multiple",
        },
      ];
    },
  },
  data() {
    const col = ["nombre", "apellidos", "permisos"];
    return {
      title: "Camareros",
      tb_name: "camareros",
      localFilter: [],
      showDialog: false,
      itemSel: null,
      filtro: {
        caption: ["borrados"],
        filters: [{ activo: 0 }],
        all: [{ activo: 1 }],
        multiple: false,
      },
      tabla: {
        headers: col,
        keys: col,
      },
      multiple_tools: {
        activos: [
          { op: "edit", text: "Editar", icon: "mdi-account-edit" },
          { op: "minus", text: "Borrar", icon: "mdi-account-minus" },
          {
            op: "unlock",
            text: "Desbloquear",
            icon: "mdi-lock-open",
          },
          { op: "pase", text: "Pase", icon: "mdi-room-service" },
        ],
        borrados: [
          { op: "activar", text: "Incorparar", icon: "mdi-account-arrow-left" },
          { op: "rm", text: "Borrado definitivo", icon: "mdi-delete" },
        ],
      },
      tools: [],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    cargar_reg() {
      this.getListadoCompuesto({ tablas: ["camareros", "permisoschoices"] });
    },
    on_close_dialogo() {
      this.showDialog = false;
    },
    on_click_filter(lfilter) {
      this.localFilter = lfilter;
      var selected = lfilter.selected;
      if (selected && selected.length > 0) {
        var op = this.filtro.caption[selected[0]];
        this.tools = this.multiple_tools[op];
      } else {
        this.tools = this.multiple_tools["activos"];
      }
      this.$store.state.itemsFiltrados = this.getItemsFiltered(
        this.localFilter,
        this.tb_name
      );
    },

    on_click_tools(v, op) {
      var inst = {};
      switch (op) {
        case "pase":
          this.$router.push({ name: "camarerospase" });
          break;
        case "edit":
          this.titleDialogo = "Editar";
          v.permisos = this.$tools.stringToArray(v.permisos);
          this.itemSel = v;
          this.showDialog = true;
          this.tipo = "md";
          break;
        case "minus":
          inst = {
            tb: this.tb_name,
            reg: { activo: 0, autorizado: 0 },
            tipo: "md",
            id: v.id,
          };
          v.activo = 0;
          v.autorizado = 0;

          break;
        case "activar":
          inst = {
            tb: this.tb_name,
            reg: { activo: 1 },
            tipo: "md",
            id: v.id,
          };
          v.activo = 1;

          break;
        case "unlock":
          inst = {
            tb: this.tb_name,
            reg: { pass_field: "" },
            tipo: "md",
            id: v.id,
          };
          break;
        case "rm":
          inst = {
            tb: this.tb_name,
            tipo: "rm",
            id: v.id,
          };
          let cam = this.$store.state.camareros;
          this.$store.state.camareros = cam.filter((e) => {
            return e.id != v.id;
          });
          break;
      }
      if (op != "edit" && op != "pase") {
        this.addInstruccion({ inst: inst });
        this.on_click_filter(this.localFilter);
      }
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
    if (this.camareros && this.permisoschoices) {
      this.on_click_filter(this.localFilter);
    } else {
      this.cargar_reg();
    }
  },
};
</script>
