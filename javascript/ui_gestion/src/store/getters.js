export default {
    getFilters: (state) => (tb_name, field, v) =>{
        if (!state[tb_name]) return []
        var filters = []
        Object.values(state[tb_name]).forEach((e)=>{
            var filter = {}
            filter[v] = e[field]
            filters.push(filter)
        })
        
        return filters;
    },
    getListValues: (state) => (tb_name, field) =>{
        if (!state[tb_name]) return []
        var lista = []
        Object.values(state[tb_name]).forEach((e)=>{
            lista.push(e[field])
        })
        return lista;
    },
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

        if (filter.include){
            return Object.values(state[tb_name])
            .filter( o => {
                var is_corret = false
            
                f.forEach((q) => {
                    if (!is_corret){
                        is_corret = true
                        Object.keys(q).map( (k) => {
                            is_corret = is_corret && o[k].includes(q[k])
                        });
                    } 
                });
                
                return is_corret
            })
        }
        return Object.values(state[tb_name])
        .filter( o => {
            var is_corret = false
           
            f.forEach((q) => {
                if (!is_corret){
                    is_corret = true
                    Object.keys(q).map( (k) => {
                        is_corret = is_corret && o[k] == q[k]
                    });
                } 
            });
            
            return is_corret
        })
    }
}