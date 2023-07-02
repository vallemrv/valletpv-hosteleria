<template>
    <v-form v-model="valid" ref="form" @submit.prevent="submitForm">
        <v-text-field v-model="empresa" label="Nombre de la empresa" required></v-text-field>
        <v-text-field v-model="email" label="Email" required></v-text-field>
        <v-text-field v-model="razonSocial" label="Razón Social" required></v-text-field>
        <v-text-field v-model="nif" label="NIF" required></v-text-field>
        <v-text-field v-model="direccion" label="Dirección" required></v-text-field>
        <v-text-field v-model="telefono" label="Teléfono" required></v-text-field>
        <v-text-field v-model="poblacion" label="Población" required></v-text-field>
        <v-text-field v-model="provincia" label="Provincia" required></v-text-field>
        <v-text-field v-model="cp" label="Código Postal" required></v-text-field>
        <v-file-input v-model="logoImpresora" label="Logo de Impresora" prepend-icon="mdi-upload"></v-file-input>
        <v-file-input v-model="logoCorporacion" label="Logo de Corporación" prepend-icon="mdi-upload"></v-file-input>
        <v-btn color="success" :disabled="!valid" @click="submitForm">Submit</v-btn>
    </v-form>
</template>
  
<script>
import { ref } from "vue";
import { db } from "@/firebase"; // Asegúrate de importar tu base de datos de Firebase

export default {
    props: {
        user: {
            type: Object,
            required: true,
        },
    },
    setup() {
        const valid = ref(false);
        const empresa = ref("");
        const email = ref("");
        const razonSocial = ref("");
        const nif = ref("");
        const direccion = ref("");
        const telefono = ref("");
        const poblacion = ref("");
        const provincia = ref("");
        const cp = ref("");
        const logoImpresora = ref(null);
        const logoCorporacion = ref(null);

        async function submitForm() {
            if (!valid.value) return;

            // Asegúrate de tener un lugar en tu base de datos para guardar los datos de la empresa
            await db.collection("empresas").add({
                empresa: empresa.value,
                email: email.value,
                razonSocial: razonSocial.value,
                nif: nif.value,
                direccion: direccion.value,
                telefono: telefono.value,
                poblacion: poblacion.value,
                provincia: provincia.value,
                cp: cp.value,
                logoImpresora: logoImpresora.value,
                logoCorporacion: logoCorporacion.value,
                userId: user.uid,
            });

            // Limpia el formulario después de enviarlo
            empresa.value = "";
            email.value = "";
            razonSocial.value = "";
            nif.value = "";
            direccion.value = "";
            telefono.value = "";
            poblacion.value = "";
            provincia.value = "";
            cp.value = "";
            logoImpresora.value = null;
            logoCorporacion.value = null;
            valid.value = false;
        }

        return {
            valid,
            empresa,
            email,
            razonSocial,
            nif,
            direccion,
            telefono,
            poblacion,
            provincia,
            cp,
            logoImpresora,
            logoCorporacion,
            submitForm,
        };
    },
};
</script>
  