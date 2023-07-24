import { defineStore } from "pinia"
import { buildUrl } from "@/api"
import axios from "axios"
import { USER_GET_LIST, USER_PROFILE_UPDATE, USER_CREATE, DELETE_REG } from "@/endpoints"

export const UserStore = defineStore({
    id: "user",
    state: () => ({
        modelo: "User",
        items: [],
        permisos: [],
        titulo: "Usuarios",
        headers: ["Nombre", "Email"],
        showKeys: ["username", "email"],
        is_dispositivo: false,
        fields: [
            { key: 'username', label: 'Usuario', type: 'text', rules: [v => !!v || "El usuario es requerido"], },
            { key: 'first_name', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"], },
            { key: 'last_name', label: 'Apellidos', type: 'text', rules: [v => !!v || "Los apellidos son requeridos"], },
            { key: 'email', label: 'Email', type: 'text', rules: [v => !!v || "El email es requerido", v => /.+@.+/.test(v) || 'E-mail debe ser válido'], },
            { key: 'hora_ini', label: 'Hora Inicio', type: 'time', },
            { key: 'hora_fin', label: 'Hora Fin', type: 'time', },
            { key: 'password', label: 'Contraseña', type: 'password', },
        ],
        newItem: {
            first_name: "",
            last_name: "",
            email: "",
            password: "",
            hora_ini: "00:00",
            hora_fin: "00:00",
            is_active: true,
        },
    }),
    actions: {
        async load(empresaStore) {
            let url = buildUrl(empresaStore.empresa.url, USER_GET_LIST);
            let params = empresaStore.createFormData();
            let response = await axios.post(url, params);
            let data = response.data;

            if (data.success) {
                this.items = data.regs.map(item => {
                    item.hora_ini = item.horario.hora_ini;
                    item.hora_fin = item.horario.hora_fin;
                    item.password = "";
                    return item;
                });
            }
        },

        async update(item) {

            item.is_superuser = false;
            item.is_staff = true;
            item.is_active = true;

            item.horario = { hora_ini: item.hora_ini, hora_fin: item.hora_fin };
            delete item.hora_ini;
            delete item.hora_fin;

            let url = buildUrl(this.empresaStore.empresa.url, USER_PROFILE_UPDATE);

            let params = this.empresaStore.createFormData({ tb_name: this.modelo, reg: item });
            let response = await axios.post(url, params);
            let data = response.data;
            if (data.success) {
                if (data.reg) {
                    const index = this.items.findIndex(p => p.id === item.id);
                    this.items[index] = {...data.reg, hora_ini: item.horario.hora_ini, hora_fin: item.horario.hora_fin };
                } else
                    return data
            }
        },
        async add(item) {

            item.is_superuser = false;
            item.is_staff = true;
            item.is_active = true;
            item.horario = { hora_ini: item.hora_ini, hora_fin: item.hora_fin };
            delete item.hora_ini;
            delete item.hora_fin;

            let url = buildUrl(this.empresaStore.empresa.url, USER_CREATE);
            let params = this.empresaStore.createFormData({ tb_name: this.modelo, reg: item });
            let response = await axios.post(url, params);
            let data = response.data;
            if (data.success) {
                this.items.push({...data, hora_ini: item.horario.hora_ini, hora_fin: item.horario.hora_fin });
            }else{
                return data.errors
            }
        },
        async delete(item) {
            let url = buildUrl(this.empresaStore.empresa.url, DELETE_REG);
            let params = this.empresaStore.createFormData({app:'auth', tb_name: 'User', filter: {
                id: item.id
            } });
            let response = await axios.post(url, params);
            let data = response.data;
            if (data.success) {
                const index = this.items.findIndex(p => p.id === item.id);
                this.items.splice(index, 1);
            }else{
                return data.errors
            }
        },
    }
})
