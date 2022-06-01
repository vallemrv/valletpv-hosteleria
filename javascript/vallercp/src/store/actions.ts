import API from "@/api"
import * as types from "./mutations_types"

export default {
    getReceptoresSel({commit, state}){
        if(localStorage.receptores){
          state.receptores_sel = JSON.parse(localStorage.receptores);
        }else{
            state.receptores_sel = [];
        }
    },
    getEmpresa({commit, state}){
      API.get_datos_empresa().then((result) => {
          commit(types.SET_DATOS_EMPRESA, result);
      })
      .catch((error)=>{
          commit(types.ERROR_REQUEST, error);
      })
    },
    recuperarPedido({ commit, state }, pedido){
        commit(types.GET_REQUEST)
        state.pedidos = Object.values(state.pedidos).filter((e: any) => {
            return e.id != pedido.id;
        });
        const params = new FormData()
        params.append("pedido", JSON.stringify(pedido))
        API.recuperar_pedido(params)
        .then( r => commit(types.RECUPERAR_PEDIDO, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
    },
    getListado({ commit, state }){
        commit(types.GET_REQUEST)
        API.get_listado()
        .then( r => commit(types.GET_LISTADO, r))
        .catch(error => {
            commit(types.ERROR_REQUEST, error)
        })
      },
    getPendientes({ commit, state }, receptores){
        commit(types.GET_REQUEST)
        const params = new FormData();
        params.append("receptores", receptores);
        API.get_pedidos(params)
        .then( r => commit(types.GET_PEDIDOS, {result: r}))
        .catch(error => {
            commit(types.ERROR_REQUEST, {error: error})
        })
      },
}