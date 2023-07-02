<template>
  <v-container>
    <v-row justify="center">
      <v-card width="400" elevation="3">
        <v-img height="200" src="https://cdn.pixabay.com/photo/2020/07/12/07/47/bee-5396362_1280.jpg" cover
          class="text-white">
          <v-toolbar color="rgba(0, 0, 0, 0)" theme="dark" flat>
            <v-toolbar-title class="text-h6">
              Inicio de sesión
            </v-toolbar-title>
          </v-toolbar>
        </v-img>

        <v-card-text>
          <v-form v-model="validForm" ref="form">
            <v-text-field v-model="email" label="Email" required></v-text-field>
            <v-text-field v-model="password" label="Contraseña" type="password" required></v-text-field>
            <v-btn class="mt-3" color="indigo-darken-3" @click="login" :disabled="!validForm" block>
              Iniciar sesión
            </v-btn>
            <v-btn class="mt-3" color="secondary" @click="signup" :disabled="!validForm" block>
              Registrarse
            </v-btn>
          </v-form>
          <v-divider class="mt-3"></v-divider>
          <v-btn class="mt-3" prepend-icon="mdi-google" variant='outlined' color="indigo-darken-3"
            @click="loginWithGoogle" block>
            Iniciar sesión con Google
          </v-btn>
        </v-card-text>
      </v-card>
    </v-row>
  </v-container>

  <v-snackbar v-model="snackbar" color="error" timeout="3000" top>
    {{ snackbarText }}
  </v-snackbar>
</template>
  
<script>
import { GoogleAuthProvider, signInWithPopup, signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from "@/firebase";

export default {
  data() {
    return {
      validForm: true,
      email: "",
      password: "",
      snackbar: false,
      snackbarText: "",
    };
  },
  methods: {
    mostrarMensaje(mensaje) {
      this.snackbar = true;
      this.snackbarText = mensaje;
    },
    
    async loginWithGoogle() {
      const provider = new GoogleAuthProvider();
      try {
        await signInWithPopup(auth, provider);
        // El usuario ha iniciado sesión con éxito.
        // Redirige a la página que prefieras aquí.
      
        this.$router.push("/");
        
      } catch (error) {
        this.mostrarMensaje(error.message);
      }
    },
    async login() {
      try {
        await signInWithEmailAndPassword(auth, this.email, this.password);
        // El usuario ha iniciado sesión con éxito.
        // Redirige a la página que prefieras aquí.
       
        this.$router.push("/");
        
      } catch (error) {
        this.mostrarMensaje(error.message);
      }
    },
    async signup() {
      this.$router.push("/signup");
    },
  },
  mounted() {
    auth.onAuthStateChanged((user) => {
      if (user) {
        this.$router.push("/");
      }
    });
  },
};
</script>
