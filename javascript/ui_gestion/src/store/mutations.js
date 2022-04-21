import * as types from "./mutations_types"
export default {
    [types.GET_LISTADOS_SUCCES] (state, {result}){
        state.ocupado = false;
        state.error = null
        console.log(result)
    }
}