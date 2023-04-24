<template>
  <v-container fluid>
    <v-row class="ma-auto" :class="(empresaStore.empresa ? 'w-100' : 'w-lg-75')" justify="center">
      <v-col cols="12" sm="8" md="6">
        <v-card>
          <v-card-title >
            <v-card color="blue">
              <v-card-text class="text-h5">
                <v-row>
                  <v-col cols="8" class="pt-12" justify="center"> {{ title }}</v-col>
                  <v-col cols="4">
                    <v-img
                        src="./src/assets/logo_v3.png"
                        aspect-ratio="2"
                        max-height="80"
                      ></v-img>
                    </v-col>
                </v-row>
            </v-card-text>
          </v-card>
          </v-card-title>
          <v-card-text>
            <v-form ref="form">
              <v-text-field label="Nombre de la empresa" v-model="companyName"></v-text-field>
              <v-text-field label="URL" v-model="url" type="url"></v-text-field>
              <v-text-field label="Nombre de usuario" v-model="username"></v-text-field>
              <v-text-field label="Contraseña" v-model="password" type="password"></v-text-field>
            </v-form>
            <v-alert v-if="empresaStore.error" type="error">
              {{ empresaStore.error }}
            </v-alert>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="red" @click="cancel" v-if="empresaStore.empresa">Cancelar</v-btn>
            <v-btn color="green" @click="submit">Enviar</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { defineComponent } from "vue";
import { useEmpresaStore } from "@/stores/empresaStore";

export default defineComponent({
  props: {
    title: {
      type: String,
      required: true,
    },
    tipo: {
      type: String,
      required: true,
      validator: (value) => ["nuevo", "editar"].includes(value),
    },
  },
  setup() {
    const empresaStore = useEmpresaStore();
    return { empresaStore };
  },
  data() {
    return {
      myCompanyName: "",
      myUrl: "",
      username: "",
      password: "",
    };
  },
  computed:{
    companyName: {
      get() {
        return this.tipo === "editar" ? this.empresaStore.empresa.nombre : this.myCompanyName;
      },
      set(value) {
        if (this.tipo === "editar") {
          this.empresaStore.empresa.nombre = value;
        }else this.myCompanyName = value;
      },
    },
    url: {
      get() {
        return this.tipo === "editar" ? this.empresaStore.empresa.url : this.myUrl;
      },
      set(value) {
        if (this.tipo === "editar") {
          this.empresaStore.empresa.url = value;
        }else this.myUrl = value
      },
    },
  },
  methods: {
    submit() {
      if (this.tipo === "nuevo") {
        const newEmpresa = {
          id: Date.now(),
          nombre: this.myCompanyName,
          url: this.myUrl,
        };
        this.empresaStore.addEmpresa(newEmpresa, this.username, this.password);
      } else if (this.tipo === "editar") {
        this.empresaStore.upEmpresa(this.username, this.password);
      }
      if (!this.empresaStore.error) this.$emit("close");
    },
    cancel() {
      this.$emit("close");
    },
  },
  mounted(){

  }
});
</script>