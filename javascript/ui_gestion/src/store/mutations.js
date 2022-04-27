import * as types from "./mutations_types"
import * as tools from "./tools"

export default {  
    [types.GET_REQUEST] (state){
        state.ocupado= true;
        state.error = null;
    },
    [types.ERROR_REQUEST] (state, {error}){
        state.ocupado= false;
        state.error = error;
    },
    [types.GET_LISTADOS] (state, {result}){
        state.ocupado = false;
        state.error = null
        state[result.tb] = result.regs
    },
    [types.GET_LISTADOS_COMPUESTOS] (state, {result}){
        state.ocupado = false;
        state.error = null
        result.forEach(e => {
            state[e.tb] = e.regs
        });
    },
    [types.GET_TOKEN] (state, {token}){
        state.ocupado = false;
        state.error = null;
        state.token = token;
        localStorage.token = JSON.stringify(state.token)
    },
    [types.ADD_INSTRUCTIONS] (state, {inst}){
        if (!state.instrucciones) state.instrucciones = []
        state.ocupado = false;
        state.error = null
        let contains = false;
        if (inst.tipo == "md"){
            contains = tools.contains(state.instrucciones, inst, true);
        }else if ( inst.tipo == "rm"){
            contains = tools.contains(state.instrucciones, inst, false);
        }
       
        if( !contains ){
            state.instrucciones.push(inst)
        }      
    },
    [types.ACTUALIZAR] (state){
        state.ocupado = false
        state.error = null
        state.instrucciones.forEach( (inst) => {
            state[inst.tb] = null
        })
        state.instrucciones = null
    },
    [types.ADD_ITEM] (state, {item, tb_name}){
        state.ocupado = false
        state.error = null
        let result = Object.values(state[tb_name])
        result.push(JSON.parse(item))
        state[tb_name] = result
    },
    [types.MOD_SEC] (state, {item}){
        console.log(item)
        state.ocupado = false
        state.error = null
        let result = Object.values(state.teclas)
        result.forEach(e => {
            if (e.id == item.id){
                e = item;
            }
        })
        state.teclas = result
    }
        
}