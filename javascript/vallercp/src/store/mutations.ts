import * as types from "./mutations_types";

export default {
   [types.ERROR_REQUEST]({ state }, error){
       console.log(state);
        state.ocupado = false;
        state.error = error;
   },
   [types.SET_EMPRESA]({ state }, result){
        state.ocupado = false;
        state.error = null;
        state.empresa = result;
   },
   [types.GET_REQUEST]({ state }){
       state.ocupado = true;
   }
}