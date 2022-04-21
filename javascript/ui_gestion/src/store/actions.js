import * as types  from './mutations_types'
import API from '@/api'
export default {
    getListado({ commit }, { params }){
        commit(types.GET_REQUEST)
        API.getListado(params).then( r => commit(types.GET_LISTADOS_SUCCES, {result: r}))
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