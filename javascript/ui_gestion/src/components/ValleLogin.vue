<template>
<div class="home">
  <div class="text-center">
      <img alt="Vue logo" src="/assets/logo_v3.png" height="200">
  </div>
  <v-card min-width="100px" class="ma-5 pa-5">
    <v-card-title>Identificate</v-card-title>
    <form>
      <v-text-field
        v-model="usuario"
        :error-messages="userError"
        :counter="10"
        label="Usuario"
        required
      ></v-text-field>
      <v-text-field
        v-model="pass"
        :append-icon="show_pass ? 'mdi-eye' : 'mdi-eye-off'"
        :type="show_pass ? 'text' : 'password'"
        label="Contraseña"
        :error-messages="passError"
        required
        @click:append="show_pass = !show_pass"
      ></v-text-field>
    </form>
    <v-card-actions>
      <v-btn class="mr-4" @click="submit()"> enviar </v-btn>
      <v-btn @click="clear"> borrar </v-btn>
    </v-card-actions>
  </v-card>
</div>
</template>

<script>
import { mapActions, mapState } from 'vuex'
export default {
  computed:{
    ...mapState(["error"])
  },
  watch:{
    error(v){
      if (this.error){
        this.passError="Usuario o contraseña errorneos";
      }else{
        this.passError="";
      }
    }
  },
  data: ()=>{
    return {
      show_pass:false,
      usuario:"", 
      pass:"",
      userError:"",
      passError:"",
    }
  },
  methods: {
     ...mapActions(['login']),
     submit(){ 
       if(this.usuario == ""){
          this.userError = "El usuario no puede estar vacio";
       }else if(this.pass == ""){
          this.userError = ""
          this.passError = "La contraseña no puede estar vacia";
       }else{
          let form = new FormData();
          form.append("username",this.usuario)
          form.append("password", this.pass)
          this.login({params:form});
       }
     },
     clear(){
        this.usuario="" 
        this.pass=""
        this.userError=""
        this.passError=""
     }
  },
};
</script>


<style scoped>
  .home{
    width: 90%;
    max-width: 600px;
    margin-top: 30px;
    margin-left: auto;
    margin-right: auto;
  }
</style>