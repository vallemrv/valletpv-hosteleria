<template>
  <v-container>
    <v-row justify="center">
      <v-card width="400" elevation="3">
        <v-img height="200" src="https://cdn.pixabay.com/photo/2020/07/12/07/47/bee-5396362_1280.jpg" cover
          class="text-white">
          <v-toolbar color="rgba(0, 0, 0, 0)" theme="dark" flat>
            <v-toolbar-title class="text-h6">
              Registro
            </v-toolbar-title>
          </v-toolbar>
        </v-img>

        <v-card-text>
          <v-form v-model="validForm" ref="form">
            <v-text-field v-model="username" label="Nombre de usuario" required></v-text-field>
            <v-text-field v-model="email"
              :rules="[v => !!v || 'El correo es requerido', v => /.+@.+\..+/.test(v) || 'Correo inválido']" label="Email"
              required></v-text-field>
            <v-text-field :rules="[
              v => !!v || 'Campo requerido',
              v => v.length >= 8 || 'La contraseña debe tener al menos 8 caracteres',
              v => /[A-Z]/.test(v) || 'La contraseña debe contener al menos una letra mayúscula',
              v => /[0-9]/.test(v) || 'La contraseña debe contener al menos un número',
              v => /[\W]/.test(v) || 'La contraseña debe contener al menos un carácter especial'
            ]" v-model="password" label="Contraseña" type="password" required
              @input="checkPasswordStrength"></v-text-field>
            <v-progress-linear v-model="passwordStrength" :color="passwordStrengthColor"></v-progress-linear>
            <v-label>{{ passwordStrengthText }}</v-label>
            <v-text-field v-model="repeatPassword"
              :rules="[v => !!v || 'Campo requerido', v => v === password || 'Las contraseñas no coinciden']"
              label="Repetir Contraseña" type="password" required></v-text-field>
            <v-btn class="mt-3" color="indigo-darken-3" @click="register" :disabled="!validForm" block>
              Registrarse
            </v-btn>
          </v-form>
        </v-card-text>
      </v-card>
    </v-row>
  </v-container>
  <v-snackbar v-model="snackbar" color="error" timeout="3000" top>
    {{ snackbarText }}
  </v-snackbar>
</template>

<script>
import { createUserWithEmailAndPassword, updateProfile, signInWithEmailAndPassword } from "firebase/auth";
import { auth } from "@/firebase";


export default {
  data: () => ({
    validForm: true,
    username: '',
    email: '',
    password: '',
    repeatPassword: '',
    passwordStrength: 0,
    passwordStrengthText: '',
    passwordStrengthColor: '',
    snackbar: false,
    snackbarText: ''
  }),
  methods: {
    async register() {
      try {
    
        try {
          // Intenta iniciar sesión primero
            const signInResult = await signInWithEmailAndPassword(auth, this.email, this.password);
            
            // Si el inicio de sesión es exitoso, redirige al usuario a la página de inicio
            this.$router.push('/');
            console.log('El usuario ya está registrado, redirigiendo...');
          } catch (signInError) {
            // Si el inicio de sesión falla, intenta registrar al usuario
            const userCredential = await createUserWithEmailAndPassword(auth, this.email, this.password);
            const user = userCredential.user;

            await updateProfile(user, {
              displayName: this.username
            });

            this.$router.push('/');
          }

  } catch (error) {
    console.error('Error durante la registración:', error);
    this.snackbarText = error.message;
    this.snackbar = true;
  }
},
    checkPasswordStrength(password) {
      let strength = 0;

      // Al menos 8 caracteres
      if (password.length >= 8) strength++;
      // Al menos una letra mayúscula
      if (/[A-Z]/.test(password)) strength++;
      // Al menos un número
      if (/\d/.test(password)) strength++;
      // Al menos un caracter especial
      if (/[^A-Za-z0-9]/.test(password)) strength++;

      this.passwordStrength = strength * 25;

      switch (strength) {
        case 0:
        case 1:
          this.passwordStrengthText = 'Baja';
          this.passwordStrengthColor = 'red';
          break;
        case 2:
          this.passwordStrengthText = 'Media';
          this.passwordStrengthColor = 'orange';
          break;
        case 3:
        case 4:
          this.passwordStrengthText = 'Fuerte';
          this.passwordStrengthColor = 'green';
          break;
      }
    },
  },

}
</script>