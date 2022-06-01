import * as types from "./mutations_types";

export default {
   [types.RECEPCION_PEDIDO] (state, {pedido}){
      state.items.push(pedido)
   },
   [types.ON_CONNECT] (state){
      state.isWsConnected = true
   },
   [types.ON_DISCONECT] (state){
     state.isWsConnected = false;
   },
   [types.ERROR_REQUEST](state, error){
        state.ocupado = false;
        state.error = error;
        state.isHttpConnected = false;
   },
   [types.SET_DATOS_EMPRESA](state, result){
        state.ocupado = false;
        state.error = null;
        state.empresa = result;
        state.isHttpConnected = true;
   },
   [types.GET_LISTADO](state, result){
      state.ocupado = false;
      state.isHttpConneted = true;
      state.error = null
      state.receptores = Object.values(result).filter( (e: any)=>{
          return !e.Nombre.toLowerCase().includes("nulo")
      })
    },
    [types.GET_REQUEST](state){
       state.ocupado = true;
    }
}