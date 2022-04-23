import * as types  from './mutations_types'
import API from '@/api'
export default {
    addInstruccion( {commit, state }, {inst}) {
        commit(types.ADD_INSTRUCTIONS, {inst:inst})
    },
    getTeclados({ commit, state }){
        commit(types.GET_REQUEST)
        let params = new FormData()
        params.append("user", state.token.user)
        params.append("token", state.token.token)
        API.getTeclados(params)
        .then( r => commit(types.GET_TECLADOS, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    getListadoCompuesto({ commit, state }, { params }){
        commit(types.GET_REQUEST)
        params.append("user", state.token.user)
        params.append("token", state.token.token)
        API.getListado(params)
        .then( r => commit(types.GET_LISTADOS_COMPUESTOS, {result: r}))
        .catch(error => {
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
    }
}