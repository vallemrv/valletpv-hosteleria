<template>
  <valle-menu-btn :items="items"></valle-menu-btn>
</template>
<script>
import ValleMenuBtn from "@/components/ValleMenuBtn";
export default {
  components: { ValleMenuBtn },
  data: () => ({
    items: [
      { title: "Camareros pase", link: "/camareros_pase" },
      { title: "Camareros", link: "/camareros" },
      { title: "Teclados", link: "/teclados" },
      { title: "Precios", link: "/precios" },
      { title: "Cajas", link: "/cierres_caja" },
    ],
  }),
};
</script>
