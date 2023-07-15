<template>
    <v-form ref="form" v-model="valid" lazy-validation>
      <v-text-field
        v-model="name"
        :rules="nameRules"
        label="Nombre"
        required
      ></v-text-field>
  
      <v-text-field
        v-model="email"
        :rules="emailRules"
        label="E-mail"
        required
      ></v-text-field>
  
      <v-text-field
        v-model="password"
        :rules="passwordRules"
        label="Contraseña"
        type="password"
        required
      ></v-text-field>
  
      <v-btn :disabled="!valid" @click="submit">Actualizar</v-btn>
  
      <v-btn @click="clear">Limpiar</v-btn>
    </v-form>
  </template>
  
  <script>
  export default {
    data: () => ({
      valid: true,
      name: '',
      nameRules: [
        v => !!v || 'El nombre es requerido',
      ],
      email: '',
      emailRules: [
        v => !!v || 'El e-mail es requerido',
        v => /.+@.+\..+/.test(v) || 'E-mail debe ser válido',
      ],
      password: '',
      passwordRules: [
        v => !!v || 'La contraseña es requerida',
        v => v.length >= 8 || 'La contraseña debe tener al menos 8 caracteres',
      ],
    }),
  
    methods: {
      submit() {
        if (this.$refs.form.validate()) {
          // Aquí debes implementar la lógica para actualizar la información del usuario
          // con tu backend Django.
        }
      },
  
      clear() {
        this.$refs.form.reset();
      },
    },
  };
  </script>
  