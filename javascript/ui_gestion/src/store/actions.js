import * as types  from './mutations_types'
import API from '@/api'
export default {
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
    getListado({ commit, state }, { params }){
        commit(types.GET_REQUEST)
        params.append("user", state.token.user)
        params.append("token", state.token.token)
        API.getListado(params)
        .then( r => commit(types.GET_LISTADOS, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    login({ commit }, { params }){
        commit(types.GET_REQUEST)
        API.login(params).then( r => commit(types.GET_TOKEN, {token: r}))
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