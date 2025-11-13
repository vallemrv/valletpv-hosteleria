<template>
  <v-dialog v-model="dialog" max-width="500px">
    <v-card>
      <v-card-title>
        <span class="text-h6">{{ isEditing ? 'Editar Empresa' : 'Agregar Empresa' }}</span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form" v-model="valid">
          <v-text-field v-model="newCompany.name" label="Nombre de la Empresa" :rules="[rules.required]"
            required></v-text-field>
          <v-text-field v-model="newCompany.url" label="URL" :rules="[rules.required, rules.url]"
            required></v-text-field>
          <v-text-field v-model="newCompany.username" label="Usuario" :rules="[rules.required]" required></v-text-field>
          <v-text-field v-model="newCompany.password" label="Contraseña" :rules="[rules.required]" type="password"
            required></v-text-field>
        </v-form>
        <!-- Mensaje de error -->
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue darken-1" text @click="closeDialog">Cancelar</v-btn>
        <v-btn color="blue darken-1" text @click="saveCompany">{{ isEditing ? 'Actualizar' : 'Guardar' }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import { useCompanyStore } from '@/stores/companyStore';

export default {
  data() {
    return {
      dialog: false,
      valid: false,
      isEditing: false,
      editingCompanyId: null,
      newCompany: {
        name: '',
        url: '',
        username: '',
        password: '',
      },
      errorMessage: '', // Mensaje de error
      rules: {
        required: (value) => !!value || 'Este campo es obligatorio.',
        url: (value) =>
          /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/.test(value) ||
          'Debe ser una URL válida.',
      },
    };
  },
  methods: {
    openDialog(companyToEdit = null) {
      this.dialog = true;
      if (companyToEdit) {
        this.isEditing = true;
        this.editingCompanyId = companyToEdit.id;
        this.newCompany = {
          name: companyToEdit.name,
          url: companyToEdit.url,
          username: '', // No rellenar por seguridad
          password: '', // No rellenar por seguridad
        };
      } else {
        this.isEditing = false;
        this.editingCompanyId = null;
        this.resetForm();
      }
    },
    closeDialog() {
      this.dialog = false;
      this.isEditing = false;
      this.editingCompanyId = null;
      this.resetForm();
    },
    async saveCompany() {
      if (this.$refs.form.validate()) {
        try {
          let formattedUrl = this.newCompany.url;
          if (!/^https?:\/\//.test(formattedUrl)) {
            formattedUrl = `http://${formattedUrl}`;
          }

          console.log('Intentando conectar con:', `${formattedUrl}/token/new.json`);

          const response = await fetch(`${formattedUrl}/token/new.json`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
              username: this.newCompany.username,
              password: this.newCompany.password,
            }),
          });

          if (!response.ok) {
            throw new Error(`Error al obtener el token: ${response.statusText}`);
          }

          const data = await response.json();

          if (data.success !== true) {
            this.errorMessage = 'Usuario o contraseña incorrectos. Por favor, inténtalo de nuevo.';
            return; // No cerrar el diálogo
          }

          const companyData = {
            id: this.isEditing ? this.editingCompanyId : Date.now(), // Mantener ID si está editando
            name: this.newCompany.name,
            user: data.user,
            token: data.token,
            url: formattedUrl,
          };

          console.log(`${this.isEditing ? 'Editando' : 'Creando'} empresa con ID:`, companyData.id);
          console.log('Datos de la empresa:', companyData);

          // Usar el store para guardar la empresa
          const companyStore = useCompanyStore();
          companyStore.addCompany(companyData);
          companyStore.setActiveCompany(companyData);

          console.log(this.isEditing ? 'Empresa actualizada:' : 'Empresa guardada:', companyData);

          this.closeDialog();
        } catch (error) {
          console.error('Error al guardar la empresa:', error);
          this.errorMessage = this.isEditing
            ? 'No se pudo actualizar la empresa. Verifica los datos e inténtalo de nuevo.'
            : 'No se pudo guardar la empresa. Verifica los datos e inténtalo de nuevo.';
        }
      }
    },
    resetForm() {
      this.newCompany = {
        name: '',
        url: '',
        username: '',
        password: '',
      };
      this.errorMessage = ''; // Resetea el mensaje de error
      this.isEditing = false;
      this.editingCompanyId = null;
      if (this.$refs.form) {
        this.$refs.form.reset();
      }
    },
  },
};
</script>

<style scoped>
.error-message {
  color: red;
  font-size: 0.9rem;
  margin-top: 10px;
}
</style>