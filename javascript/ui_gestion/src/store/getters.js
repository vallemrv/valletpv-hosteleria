export default {
     getItemById: (state) => (tb_name, id) =>{
        if (!state[tb_name]) return null
        var filter = Object.values(state[tb_name]).filter( (e) =>{
            return e.id == id
        });
        return filter.length> 0 ? filter[0] : null
    },
    getFilters: (state) => (tb_name, field, values) =>{
        if (!state[tb_name]) return []
        var filters = []
        Object.values(state[tb_name]).forEach((e)=>{
            if(values.forEach){
                var or = []
                values.forEach(v => {
                    var filter = {}
                    filter[v] = e[field]
                    or.push(filter)
                })
                filters.push(or)
            }else{
                var filter = {}
                filter[values] = e[field]
                filters.push(filter)
            }
            
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
    getItemsOrdered: () => (items, column)  =>{
        items.sort((a, b) => {
            if (a[column] > b[column]) {
            return -1;
            }
            if (a[column] < b[column]) {
            return 1;
            }
            // a debe ser igual b
            return 0;
        });
        return items;
    },
    getItemsFiltered: (state) => (filter, tb_name) =>{
        if (!filter) return Object.values(state[tb_name])
        var f = filter.filters
        if (!f || f.length == 0 || !state[tb_name]) return []
        
        if (filter.include){
            return Object.values( state[tb_name])
            .filter( o => {
                var is_corret = false
            
                f.forEach((q) => {
                    if (!is_corret){
                        is_corret = true
                        Object.keys(q).map( (k) => {
                            is_corret = is_corret && o[k].toLowerCase().includes(q[k].toLowerCase())
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