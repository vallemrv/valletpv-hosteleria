export default {
    getTeclasBySec: (state) => () =>{
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
        return Object.values(state.subteclas)
        .filter(subTecla => {
            return subTecla.tecla == idtecla
        })
    }
}