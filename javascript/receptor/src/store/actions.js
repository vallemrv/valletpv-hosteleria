import API from "@/api"
import * as types from "./mutations_types"

export default {
    recuperarPedido({ commit, state }, {pedido}){
        commit(types.GET_REQUEST)
        state.pedidos = Object.values(state.pedidos).filter((e) => {
            return e.id != pedido.id;
        });
        var params = new FormData()
        params.append("pedido", JSON.stringify(pedido))
        API.recuperar_pedido(params)
        .then( r => commit(types.RECUPERAR_PEDIDO, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    getListado({ commit, state }, {tabla}){
        commit(types.GET_REQUEST)
        API.get_listado()
        .then( r => commit(types.GET_LISTADO, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
      },
    getPendientes({ commit, state }, {receptores}){
        commit(types.GET_REQUEST)
        var params = new FormData();
        params.append("receptores", receptores);
        API.get_pedidos(params)
        .then( r => commit(types.GET_PEDIDOS, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
      },
    getDatosEmpresa({ commit, state}){
        API.get_datos_empresa()
        .then( r => commit(types.SET_DATOS_EMPRESA, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    }
}