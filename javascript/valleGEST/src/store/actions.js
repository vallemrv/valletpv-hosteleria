import * as types from './mutations_types';
import API from '@/api';

async function handleApiRequest(commit, apiCall, payload = {}) {
    commit(types.GET_REQUEST);
    try {
        const response = await apiCall(payload);
        return response;
    } catch (error) {
        commit(types.ERROR_REQUEST, { error });
        throw error;
    }
}

export default {
    async addEmpresa({ commit, state }, empresa) {
        try {
            let params = new FormData();
            params.append("username", empresa.user);
            params.append("password", empresa.pass);

            const loginResponse = await handleApiRequest(commit, () => API.login(empresa.url, params));
            const userData = {
                nombre: empresa.nombre,
                url: empresa.url,
                user: loginResponse.user,
                token: loginResponse.token
            };

            params = new FormData();
            params.append("user", loginResponse.user);
            params.append("token", loginResponse.token);

            const empresaData = await handleApiRequest(commit, () => API.get_datos_empresa(empresa.url, params));
            userData.nombre_server = empresaData.nombre;
            userData.email = empresaData.email;

            const updatedEmpresas = state.empresas.filter(el => el.nombre !== empresa.nombre).concat(userData);
            state.empresas = updatedEmpresas;
            localStorage.setItem('empresas', JSON.stringify(updatedEmpresas));
            state.empresa = userData;
            commit(types.REQUEST_SUCCESS);
        } catch (error) {
            commit(types.ERROR_REQUEST, { error });
        }
    },

    selEmpresa({ commit, state }, index) {
        const mainProperties = ["error", "ocupado", "user", "token", "instrucciones", "empresa", "empresas"];
        Object.keys(state).forEach(key => {
            if (!mainProperties.includes(key)) state[key] = null;
        });
        state.itemsFiltrados = [];
        state.empresa = state.empresas[index];
        localStorage.setItem('empresa_index', index);
        commit(types.REQUEST_SUCCESS);
    },

    cargarEmpresas({ commit, state }) {
        state.empresas = JSON.parse(localStorage.getItem('empresas') || '[]');
        const index = parseInt(localStorage.getItem('empresa_index'), 10);
        state.empresa = (index >= 0 && index < state.empresas.length) ? state.empresas[index] : state.empresas[0];
        commit(types.REQUEST_SUCCESS);
    },

    borrarEmpresa({ commit, state }, index) {
        const updatedEmpresas = state.empresas.filter((_, i) => i !== index);
        state.empresas = updatedEmpresas;
        localStorage.setItem('empresas', JSON.stringify(updatedEmpresas));

        if (updatedEmpresas.length > 0 && state.empresa.nombre === state.empresas[index].nombre) {
            state.empresa = updatedEmpresas[0];
            localStorage.setItem('empresa_index', 0);
        } else {
            state.empresa = null;
            localStorage.setItem('empresa_index', -1);
        }
        commit(types.REQUEST_SUCCESS);
    },

    async borrarVentas({ commit, state }) {
        try {
            let params = new FormData();
            params.append("user", state.empresa.user);
            params.append("token", state.empresa.token);
            await handleApiRequest(commit, () => API.borrar_ventas(state.empresa.url, params));
            commit(types.REQUEST_SUCCESS);
        } catch (error) {
            commit(types.ERROR_REQUEST, { error });
        }
    },

    // Continuación de las acciones Vuex
    async modificarSecciones({ commit, state }, { item }) {
        try {
            let params = new FormData();
            params.append("user", state.empresa.user);
            params.append("token", state.empresa.token);
            params.append("item", JSON.stringify(item));

            const result = await handleApiRequest(commit, () => API.modificarSecciones(state.empresa.url, params));
            commit(types.MOD_SEC, { item: result });
        } catch (error) {
            commit(types.ERROR_REQUEST, { error });
        }
    },

    async actualizar({ commit, state }) {
        try {
            let params = new FormData();
            params.append("user", state.empresa.user);
            params.append("token", state.empresa.token);
            params.append("isnts", JSON.stringify(state.instrucciones));

            const result = await handleApiRequest(commit, () => API.actualizar(state.empresa.url, params));
            commit(types.ACTUALIZAR, { result });
        } catch (error) {
            commit(types.ERROR_REQUEST, { error });
        }
    },

    addInstruccion({ commit }, { inst }) {
        commit(types.ADD_INSTRUCTIONS, { inst });
    },

    async getListadoCompuesto({ commit, state }, { tablas }) {
        try {
            let params = new FormData();
            params.append("tbs", JSON.stringify(tablas));
            params.append("user", state.empresa.user);
            params.append("token", state.empresa.token);

            const result = await handleApiRequest(commit, () => API.getListadoCompuesto(state.empresa.url, params));
            commit(types.GET_LISTADOS_COMPUESTOS, { result });
        } catch (error) {
            commit(types.ERROR_REQUEST, { error });
        }
    },

    async getListado({ commit, state }, { tabla = null, params = null }) {
        try {
            if (!params) params = new FormData();
            if (tabla) params.append("tb", tabla);
            params.append("user", state.empresa.user);
            params.append("token", state.empresa.token);

            const result = await handleApiRequest(commit, () => API.getListado(state.empresa.url, params));
            commit(types.GET_LISTADOS, { result });
        } catch (error) {
            commit(types.ERROR_REQUEST, { error });
        }
    },

    async addItem({ commit, state }, { item, tb_name }) {
        try {
            let params = new FormData();
            params.append("user", state.empresa.user);
            params.append("token", state.empresa.token);
            params.append("reg", JSON.stringify(item));
            params.append("tb_name", tb_name);

            const result = await handleApiRequest(commit, () => API.addItem(state.empresa.url, params));
            commit(types.ADD_ITEM, { item: result.reg, tb_name });
        } catch (error) {
            commit(types.ERROR_REQUEST, { error });
        }
    }
};


