<template>
  <v-app>
    <router-view />
    <MenuPrincipal  v-if="userStore.user"/>
  </v-app>
</template>
  
<script>

import MenuPrincipal from "./components/tools/MenuPrincipal.vue";
import { useRouter } from "vue-router";
import { loginsStore } from "./stores/loginsStore";
import { ref } from "vue";

export default {
  components: {
    MenuPrincipal,
  },
  setup() {
    const logins = loginsStore();
    const router = useRouter();
    const isLogin = ref(logins.empresa && logins.empresa.user && logins.empresa.token);
    if (!isLogin.value) router.push("/login");

    return {
      logins,
      isLogin
    };
  },

};
</script>
  

<style >
.div-fixed {
  position: fixed;
  bottom: 10px;
  right: 20px;
  width: 50px;
  height: 50px;
  text-align: center;
}

.dash-board {
  width: 100%;
  max-height: 300px;
  margin-bottom: 5px;
  overflow: scroll;
}

text-center th,
.text-center td {
  text-align: center;
}

.bg-color {
  background-color: #f2f2f2;
}

.column-authorize {
  width: 5%;
  /* Ajusta este valor según lo que necesites */
}

.column-authorize>.v-input__control {
  display: flex;
  justify-content: center;
}

/* CSS para pantallas pequeñas (mobile) */
@media (max-width: 600px) {
  .desktop {
    display: none;
  }

  .mobile {
    display: table-cell;
  }
}

/* CSS para pantallas grandes (desktop) */
@media (min-width: 601px) {
  .desktop {
    display: table-cell;
  }

  .mobile {
    display: none;
  }
}
</style>