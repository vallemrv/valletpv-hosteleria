<template>
  <valle-editor-item
    :title="title"
    :filtro="filtro"
    :tb_name="tb_name"
  ></valle-editor-item>
</template>

<script>
import ValleEditorItem from "@/components/ValleEditorItem.vue";
export default {
  components: { ValleEditorItem },
  data() {
    return {
      title: "Camareros",
      tb_name: "camareros",
      filtro: {
        caption: [{ text: "borrados" }, { text: "activos" }],
        filters: [{ activo: 0 }, { autorizado: 1 }],
        all: [{ activo: 1 }],
        multiple: false,
      },
    };
  },
};
</script>
