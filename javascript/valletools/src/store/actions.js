import * as types  from './mutations_types'
import API from '@/api'
import WS from '@/websocket'

const connet_ws = (commit, state) => {
    state.ws.forEach(w =>{
        w.disconnect();
    });
    state.ws = [];
    state.ws.push(new WS(state.empresa.url, "devices", commit));
    state.ws[0].connect();
}


export default {
    gestionarAlertas( {commit, state}, {idpeticion, aceptada}){
        commit(types.GET_REQUEST)
        let params = new FormData();
        params.append("idpeticion", idpeticion);
        params.append("aceptada", aceptada)
        API.gestionar_peticion(state.empresa.url, params).then( r=>{
            commit(types.REQUEST_SUCCESS)
            state.mensajes = state.mensajes.filter(e =>{
                return e.idpeticion != idpeticion
            });
        })
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    getAlertas( {commit, state}){
       commit(types.GET_REQUEST)
       API.get_lista_autorizaciones(state.empresa.url).then( r =>{
           state.mensajes = r
           commit(types.REQUEST_SUCCESS)
        })
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    selEmpresa({ commit, state }, index){
        state.empresa = state.empresas[index];
        localStorage.empresa_index = index;
        connet_ws(commit, state);
    },
    cargarEmpresas({ commit, state }){
        state.empresas = JSON.parse(localStorage.empresas);
        let index = localStorage.empresa_index;
        if (index) state.empresa = state.empresas[index];
        else state.empresa = state.empresas[0];
        connet_ws(commit, state);
    },
    getDatasets({ commit, state }){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("user", state.empresa.user)
        params.append("token", state.empresa.token)
        API.dataset(state.empresa.url, params).then( r=>{
            commit(types.SET_DATASETS, r)
        })
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
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
    getListados({ commit, state }, tablas ){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("tbs", JSON.stringify(tablas))
        params.append("user", state.empresa.user)
        params.append("token", state.empresa.token)
        API.getListadoCompuesto(state.empresa.url, params)
        .then( r => commit(types.GET_LISTADOS, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    getListado({ commit, state },  tabla=null, filter=null ){
        commit(types.GET_REQUEST)
        let params = new FormData();
        if (tabla) params.append("tb", tabla);
        if (filter) params.append("filter", filter);
        params.append("user", state.empresa.user)
        params.append("token", state.empresa.token)
        API.getListado(state.empresa.url, params)
        .then( r => commit(types.GET_LISTADO, {result: r}))
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