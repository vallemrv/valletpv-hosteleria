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

const estadoToStr = (estado)=>{
    if (estado=="P") return "pedido";
    else if (estado == "C") return "cobrado";
    else if (estado == "A") return "borrado";
}

const color = ["#D6EAF8", "#F6D6F8", "#ABEBC6"]

export default { 
    [types.SET_DATASETS] (state, data){
        state.ocupado= false;
        state.error = null;
        state.total = 0;
        state.chartSet = {
            labels: ["borrado", "cobrado", "pedido"],
            datasets: [{backgroundColor:["#D6EAF8", "#F6D6F8", "#ABEBC6"], data:[0,0,0] }]
        }
        let chartSet = state.chartSet;
        data.forEach((d) => {
            if (d.estado != "R"){
                let estado = d.estado;
                let i = chartSet.labels.indexOf(estadoToStr(estado))
                chartSet.datasets[0].backgroundColor[i] = (color[i])
                chartSet.datasets[0].data[i] = (d.total)
                if (estado == "P" || estado == "C"){
                    state.total += d.total;
            }
          }
        })
    },
    [types.ON_MENSAJE] (state, mensaje){
        
        let op = mensaje.op;
        let tb = mensaje.tb;
        if (tb == "lineaspedido"){
            if ( op == "insert" ){
                var objs = []
                if(mensaje.obj.Precio){ objs[0] = mensaje.obj }
                else objs = mensaje.obj
                for(var i=0; i < objs.length; i++){
                    let precio = objs[i].Precio;
                    state.total += precio;
                    let index = state.chartSet.labels.indexOf("pedido");
                    state.chartSet.datasets[0].data[index] += precio;
                }
            } else if ( op == "rm") {
                let op_ex = mensaje.extras.op
                let precio = mensaje.extras.precio;
                if (op_ex == "borrado"){
                     state.total -= precio;
                     state.last_accion = "Se acaba de borrar "+ parseFloat(mensaje.extras.precio).toFixed(2)+"  €";
                 
                }
                let index = state.chartSet.labels.indexOf("pedido");
                state.chartSet.datasets[0].data[index] -= precio;
                index = state.chartSet.labels.indexOf(op_ex);
                state.chartSet.datasets[0].data[index] += precio;
            } 
        }
        else if (tb == "mesasabiertas"){
            let obj = mensaje.obj;
            if (obj.abierta == 1 && obj.num == 0){
                let f = state.mesasabiertas.filter( e => e.ID == obj.ID);
                if (f.length > 0 ){
                    let i = state.mesasabiertas.indexOf(f[0]);
                    state.mesasabiertas[i] = obj;
                }  
                else state.mesasabiertas.push(obj);
            }
            else if (obj.abierta == 0) {
                state.mesasabiertas = Object.values(state.mesasabiertas).filter( e => e.ID != obj.ID)
            }
        }else if (tb == "mensajes"){
              state.last_accion = mensaje.obj.mensaje
              state.mensajes.push(mensaje.obj);
              state.new_men = true;
        }


    },
    [types.ON_CONNECT] (state){
        state.isWSConnected = true;
    },
    [types.ON_DISCONECT] (state){
        state.isWSConnected = false;
    },
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
    },
    [types.GET_LISTADO] (state, {result}){
        state.ocupado = false;
        state.error = null
        state[result.tb] = result.regs
    },
    [types.GET_LISTADOS] (state, {result}){
        state.ocupado = false;
        state.error = null
        result.forEach(e => {
            state[e.tb] = e.regs
        });
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
            state[inst.tb] = null
        })
        state.instrucciones = null
    },
    [types.ADD_ITEM] (state, {item, tb_name}){
        state.ocupado = false
        state.error = null
        var result = Object.values(state[tb_name])
        result.push(JSON.parse(item))
        state[tb_name] = result
    },
    [types.MOD_SEC] (state, {item}){
        state.ocupado = false
        state.error = null
        var result = Object.values(state.teclas)
        result.forEach((e,i) => {
            if (parseInt(e.id) == parseInt(item.id)){
                result[i] = item;
            }
        })
        state.teclas = result
    }
        
}