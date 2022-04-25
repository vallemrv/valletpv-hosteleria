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
        if (!state[tb_name]) return []
        if (filter.filters.length == 0) return state[tb_name]
        return Object.values(state[tb_name])
        .filter( o => {
            let is_corret = false
            if (filter.is_and){
                is_corret = true
                Object.values(filter.filters).forEach((e, i) => {
                    var k = Object.keys(e)[0];
                    is_corret = is_corret && o[k] == filter.filters[i][k]
                })
            }else{
                Object.values(filter.filters).forEach((e, i) => {
                    var k = Object.keys(e)[0];
                    is_corret = is_corret || o[k] == filter.filters[i][k]
                })
            }
            
            return is_corret
        })
    }
}