<template>
  <valle-menu-btn :items="items"></valle-menu-btn>
</template>
<script>
import ValleMenuBtn from "@/components/ValleMenuBtn";
export default {
  components: { ValleMenuBtn },
  data: () => ({
    items: [
      { title: "Camareros pase", link: { name: "camarerospase" } },
      { title: "Camareros", link: { name: "camareros" } },
      { title: "Teclados", link: { name: "teclas" } },
      { title: "Precios", link: { name: "cambioprecios" } },
      { title: "Cajas", link: "/" },
    ],
  }),
};
</script>
