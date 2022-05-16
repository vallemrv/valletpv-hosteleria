<template>
  <valle-menu-btn :items="items"></valle-menu-btn>
</template>
<script>
import ValleMenuBtn from "@/components/ValleMenuBtn";

export default {
  components: { ValleMenuBtn },
  data: () => ({
    items: [
      { title: "Cajas", link: { name: "" } },
      { title: "Borrar ventas", link: { name: "borrar_ventas" } },
    ],
  }),
};
</script>
