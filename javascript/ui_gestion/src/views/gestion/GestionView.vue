<template>
  <valle-menu-btn :items="items"></valle-menu-btn>
</template>
<script>
import ValleMenuBtn from "@/components/ValleMenuBtn";
export default {
  components: { ValleMenuBtn },
  data: () => ({
    items: [
      { title: "Secciones", link: { name: "secciones" } },
      { title: "Vista teclas", link: { name: "vista" } },
    ],
  }),
};
</script>
