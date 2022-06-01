import { createStore } from "vuex";
import actions from "./actions";
import mutations from "./mutations";


export default  createStore({
    state:{
        items:[],
        receptores: [],
        receptores_sel: [],
        isWsConnected: false,
        isHttpConnected: false,
        error: null,
        ocupado: false,
        empresa: null,
    },
    actions,
    mutations
})