import { createStore } from "vuex";
import actions from "./actions";
import mutations from "./mutations";


export default  createStore({
    state:{
        empresa: null,
        error: null,
        ocupado: false
    },
    actions,
    mutations
})