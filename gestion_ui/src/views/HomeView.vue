<template>
  <valle-menu-btn :items="items"></valle-menu-btn>
</template>
<script>
import ValleMenuBtn from "@/components/ValleMenuBtn";

export default {
  components: { ValleMenuBtn },
  data: () => ({
    items: [
      { title: "Camareros", link: "/camareros" },
      { title: "Click Me", link: "/camareros" },
      { title: "Click Me", link: "/camareros" },
      { title: "Click Me 2", link: "/camareros" },
    ],
  }),
};
</script>
