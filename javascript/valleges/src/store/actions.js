import * as types  from './mutations_types'
import API from '@/api'
export default {
    addEmpresa({ commit, state }, empresa){
        commit(types.GET_REQUEST)
        let params = new FormData();
        params.append("username", empresa.user);
        params.append("password", empresa.pass);
        
        API.login(empresa.url, params).then( r => {
            const obj = {
                    nombre:empresa.nombre,
                    url:empresa.url,
                    user:r.user,
                    token:r.token
                }
            params = new FormData()
            params.append("user", r.user)
            params.append("token", r.token)
            API.get_datos_empresa(empresa.url, params)
            .then(r => {
                let v = state.empresas.filter(element => {
                    return element.nombre != empresa.nombre
                 });
                 obj.nombre_server = r.nombre;
                 obj.email = r.email;
                 v.push(obj);
                 state.empresas = [...v];
                 localStorage.empresas = JSON.stringify(v)
                 state.empresa = obj;
                 commit(types.REQUEST_SUCCESS)
            })
            .catch(error => {
                commit(types.ERROR_REQUEST, {error: error})
            })
        })
        .catch(error => {
           commit(types.ERROR_REQUEST, {error: error})
        })
    }, 
    selEmpresa({ commit, state }, index){
        const main_property = ["error","ocupado", "user", "token", "instrucciones", "empresa", "empresas"];
        for (const p in state){
            if (!main_property.includes(p)){
                state[p] = null;
            }
        }
        state.itemsFiltrados  = [];
        state.empresa = state.empresas[index];
        localStorage.empresa_index = index;
        commit(types.REQUEST_SUCCESS)
    },
    cargarEmpresas({ commit, state }){
        state.empresas = JSON.parse(localStorage.empresas);
        let index = localStorage.empresa_index;
        if (index >= 0 && index < state.empresas.length) state.empresa = state.empresas[index];
        else state.empresa = state.empresas[0];
        commit(types.REQUEST_SUCCESS)
    },
    borrarVentas( {commit, state}){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("user", state.empresa.user)
        params.append("token", state.empresa.token)
        API.borrar_ventas(state.empresa.url, params)
        .then(r => commit(types.REQUEST_SUCCESS))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },    
    modificarSecciones( {commit, state}, {item}){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("user", state.empresa.user)
        params.append("token", state.empresa.token)
        params.append("item", JSON.stringify(item))
        API.modificarSecciones(state.empresa.url, params)
        .then(r => commit(types.MOD_SEC, {item: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    actualizar( {commit, state}){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("user", state.empresa.user)
        params.append("token", state.empresa.token)
        params.append("isnts", JSON.stringify(state.instrucciones))
        API.actualizar(state.empresa.url, params)
        .then(r => commit(types.ACTUALIZAR, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    addInstruccion( {commit }, {inst}) {
        commit(types.ADD_INSTRUCTIONS, {inst:inst})
    },
    getListadoCompuesto({ commit, state }, { tablas }){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("tbs", JSON.stringify(tablas))
        params.append("user", state.empresa.user)
        params.append("token", state.empresa.token)
        API.getListadoCompuesto(state.empresa.url, params)
        .then( r => commit(types.GET_LISTADOS_COMPUESTOS, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    getListado({ commit, state }, { tabla=null, params=null }){
        commit(types.GET_REQUEST)
        if (!params) params = new FormData();
        if (tabla) params.append("tb", tabla);
        params.append("user", state.empresa.user)
        params.append("token", state.empresa.token)
        API.getListado(state.empresa.url, params)
        .then( r => commit(types.GET_LISTADOS, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    addItem({commit, state}, { item, tb_name }){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("user", state.empresa.user)
        params.append("token", state.empresa.token)
        params.append("reg", JSON.stringify(item))
        params.append("tb_name", tb_name)
        API.addItem(state.empresa.url, params)
        .then(r =>{
            commit(types.ADD_ITEM, {item:r.reg, tb_name:tb_name})
        } )
        .catch(error => {
           commit(types.ERROR_REQUEST, {error: error})
        })
    }
}