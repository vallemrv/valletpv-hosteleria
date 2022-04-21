import * as types from "./mutations_types"
export default {
    [types.GET_REQUEST] (state){
        state.ocupado= true;
        state.error = null;
    },
    [types.ERROR_REQUEST] (state, {error}){
        state.ocupado= false;
        state.error = error;
    },
    [types.GET_LISTADOS_SUCCES] (state, {result}){
        state.ocupado = false;
        state.error = null
        state[result.tb] = result.regs
    },
    [types.GET_TOKEN] (state, {token}){
        state.ocupado = false;
        state.error = null;
        state.token = token;
        localStorage.token = JSON.stringify(state.token)
    }
}