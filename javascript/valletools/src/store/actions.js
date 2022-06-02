import * as types  from './mutations_types'
import API from '@/api'
export default {
    selEmpresa({commit, state}, index){
        state.empresa = state.empresas[index];
    },
    cargarEmpresas({commit, state}){
        state.empresas = JSON.parse(localStorage.empresas);
        state.empresa = state.empresas[0];
    },
    addEmpresa({ commit, state}, empresa){
        commit(types.GET_REQUEST)
        let params = new FormData();
        params.append("username", empresa.user);
        params.append("password", empresa.pass);
        API.login(empresa.url, params).then( r => {
            const obj = {nombre:empresa.nombre,
                        url:empresa.url,
                        user:r.user,
                        token:r.token }
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
                 state.empresas = Object.values(v);
                 localStorage.empresas = JSON.stringify(v)
                 state.empresa = empresa;
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
    borrarVentas( {commit, state}){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("user", state.token.user)
        params.append("token", state.token.token) 
        API.borrar_ventas(params)
        .then(r => commit(types.REQUEST_SUCCESS))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },    
    modificarSecciones( {commit, state}, {item}){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("user", state.token.user)
        params.append("token", state.token.token) 
        params.append("item", JSON.stringify(item))
        API.modificarSecciones(params)
        .then(r => commit(types.MOD_SEC, {item: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    actualizar( {commit, state}){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("user", state.token.user)
        params.append("token", state.token.token) 
        params.append("isnts", JSON.stringify(state.instrucciones))
        API.actualizar(params)
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
        params.append("user", state.token.user)
        params.append("token", state.token.token)
        API.getListadoCompuesto(params)
        .then( r => commit(types.GET_LISTADOS_COMPUESTOS, {result: r}))
        .catch(error => {
            console.log(error)
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    getListado({ commit, state }, { tabla=null, params=null }){
        commit(types.GET_REQUEST)
        if (!params) params = new FormData();
        if (tabla) params.append("tb", tabla);
        params.append("user", state.token.user)
        params.append("token", state.token.token)
        API.getListado(params)
        .then( r => commit(types.GET_LISTADOS, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    addItem({commit, state}, { item, tb_name }){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("user", state.token.user)
        params.append("token", state.token.token)
        params.append("reg", JSON.stringify(item))
        params.append("tb_name", tb_name)
        API.addItem(params).then(r=> commit(types.ADD_ITEM, {item:r.reg, tb_name:tb_name}))
        .catch(error => {
           commit(types.ERROR_REQUEST, {error: error})
        })
    }
}