import * as types from "./mutations_types"

const contains_func = (instrucciones, inst, reg) => {
    let contains = false;
    instrucciones.forEach( (obj) => {
        if (obj.id == inst.id){
            if (reg){
                var col_obj = Object.keys(obj.reg)[0]
                var col_inst = Object.keys(inst.reg)[0]
                if (col_obj == col_inst) {
                    obj.reg = inst.reg
                    contains = true;
                }
            } else{       
            contains = true;
            }
        }
    });
    return contains;
}   

export default {  
    [types.GET_REQUEST] (state){
        state.ocupado= true;
        state.error = null;
    },
    [types.REQUEST_SUCCESS] (state){
        state.ocupado= false;
        state.error = null;
    },
    [types.ERROR_REQUEST] (state, {error}){
        state.ocupado= false;
        state.error = error;
        if (error.response) {
           if (error.response.status == 401){
               state.error = "Credenciales erroneas o usuarion no autorizado."
           }else if (error.response.status == 500){
             state.error = "Error en el servidor.";
           }
          } else if (error.request) {
            state.error = "No se ha podido alcanzar el servidor o no hay conexion"
          } else {
            state.error = error.message;
          }
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
    [types.GET_EMPRESA] (state, {result}){
        state.ocupado = false;
        state.error = null;
        state.empresa = result;
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
            contains = contains_func(state.instrucciones, inst, true);
        }else if ( inst.tipo == "rm"){
            contains = contains_func(state.instrucciones, inst, false);
        }
       
        if( !contains ){
            state.instrucciones.push(inst)
        }      
    },
    [types.ACTUALIZAR] (state){
        state.ocupado = false
        state.error = null
        state.instrucciones.forEach( (inst) => {
            if (inst.tb == "teclascom"){
                state["teclas"] = null
            }else state[inst.tb] = null
        })
        state.instrucciones = null
    },
    [types.ADD_ITEM] (state, {item, tb_name}){
        state.ocupado = false
        state.error = null
        var result = [...state[tb_name]]
        result.push(JSON.parse(item))
        state[tb_name] = result
    },
    [types.MOD_SEC] (state, {item}){
        state.ocupado = false
        state.error = null
        var result = [...state.teclas]
        result.forEach((e,i) => {
            if (parseInt(e.id) == parseInt(item.id)){
                result[i] = item;
            }
        })
        state.teclas = result
        
    }
        
}