<template>
  <h1>Ventas</h1>
</template>
<script>
import ValleMenuBtn from "@/components/ValleMenuBtn";
export default {
  components: { ValleMenuBtn },
  data: () => ({
    items: [
      { title: "Camareros pase" },
      { title: "Camareros" },
      { title: "Teclados" },
      { title: "Precios" },
      { title: "Cajas" },
    ],
  }),
};
</script>
