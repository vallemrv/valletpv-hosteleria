<template>
  <v-app>
    <router-view />
    <MenuPrincipal  v-if="userStore.user"/>
  </v-app>
</template>
  
<script>
import { useUserStore } from "@/stores/userStore";
import { useEmpresasStore } from "@/stores/empresasStore";
import MenuPrincipal from "./components/tools/MenuPrincipal.vue";
import { auth } from "@/firebase";
import { watch } from "vue";
import { useRouter } from "vue-router";

export default {
  components: {
    MenuPrincipal,
  },
  setup() {
    const userStore = useUserStore();
    const empStore = useEmpresasStore();
    const router = useRouter();
   
    watch(() => userStore.user, (user) => {
      if (user) {
        empStore.userId = user.id;
        empStore.suscribirAEmpresas();
      }
    });


    //Comprobar si el usuario está logueado con firebase
    auth.onAuthStateChanged((user) => {
      if (user) {
        // Creamos un usuario con nuestra api para poder usarlo en toda la app
        userStore.set(user);
      } else {
        empStore.unsuscribirAEmpresas();
        userStore.set(null);
        router.push({ name: "Login" });
      }
    });

    return {
      userStore,
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