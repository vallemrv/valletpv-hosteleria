import { reactive } from "vue";

export const UserStore = () => {
    const state = reactive({
        modelo: "User",
        items: [],
        permisos: [],
        titulo: 'Usuarios',
        headers: ["Nombre", "Email"],
        showKeys: ["username", "email"],
        switchKey: "is_active",
        fields: [
            { key: 'first_name', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"], },
            { key: 'last_name', label: 'Apellidos', type: 'text', rules: [v => !!v || "Los apellidos son requeridos"], },
            { key: 'email', label: 'Email', type: 'text', rules: [v => !!v || "El email es requerido", v => /.+@.+/.test(v) || 'E-mail debe ser válido'], },
            { key: 'is_staff', label: 'Staff', type: 'boolean', },
            { key: 'is_superuser', label: 'Superusuario', type: 'boolean', },
            { key: 'groups', label: 'Grupos', multiple: true, type: 'select', options: [] },
        ],
        extraAcitons: [
            { icon: "mdi-key", action: "resetPassword" },
        ],
        newItem: {
            first_name: "",
            last_name: "",
            email: "",
            is_active: false,
            is_staff: false,
            is_superuser: false,
            groups: [],
        },
    })

    return state
}
