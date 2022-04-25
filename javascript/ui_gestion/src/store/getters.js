export default {
    getTeclasBySec: (state) => () =>{
        if (!state.teclas) return [];
        return Object.values(state.teclas)
        .filter(tecla => {
            if (state.secFilter.length <= 0){
                return tecla.IDSeccion == -1
            }else{
                return state.secFilter.includes(tecla.IDSeccion)
            }
        })
    },
    getSubTeclasByTecla: (state) => (idtecla) =>{
        if (!state.subteclas) return [];
        return Object.values(state.subteclas)
        .filter(subTecla => {
            return subTecla.tecla == idtecla
        })
    },
    getfilterCamareros: (state) => (filter) => {
       if (!state.camareros) return [];
       return Object.values(state.camareros)
       .filter( c => {
           if (filter=="activos"){
               return c.autorizado == 1 && c.activo == 1
           }else if (filter=="borrados") {
               return c.activo == 0 
           }
           return c.activo == 1
       })

    },
    getItemsFiltered: (state) => (filter, tb_name) =>{
        var f = filter.filters
        if (!state[tb_name]) return []
        if (f.length == 0) return state[tb_name]
        return Object.values(state[tb_name])
        .filter( o => {
            var is_corret = false
           
            f.forEach((q) => {
                if (!is_corret){
                    is_corret = true
                    Object.keys(q).map( (k) => {
                        is_corret = is_corret && o[k] == q[k]
                        //console.log(o.nombre, o[k], q[k], is_corret)
                    });
                } 
            });
            
            return is_corret
        })
    }
}