<template>
  <valle-menu-btn :items="items"></valle-menu-btn>
</template>
<script>
import ValleMenuBtn from "@/components/ValleMenuBtn";
export default {
  components: { ValleMenuBtn },
  data: () => ({
    items: [
      { title: "Receptores", link: { name: "receptores" } },
      { title: "Grupos y familias", link: { name: "familias" } },
      { title: "Menú y ofertas", link: { name: "composicion" } },
      { title: "Secciones", link: { name: "secciones" } },
      { title: "Vista teclas", link: { name: "vista" } },
      { title: "Teclados comanda", link: { name: "tecladoscom" } },
      { title: "Teclados TPV", link: { name: "tecladostpv" } },
    ],
  }),
};
</script>
