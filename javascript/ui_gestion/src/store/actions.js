import * as types  from './mutations_types'
import API from '@/api'
export default {
    getListado({ commit }, { params }){
        API.getListado(params).then( r => commit(types.GET_LISTADOS_SUCCES, {result: r}))
    }
}