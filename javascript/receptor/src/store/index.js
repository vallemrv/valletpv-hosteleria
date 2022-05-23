import { createStore } from 'vuex'
import mutations from './mutations'
import actions from './actions'


export default createStore({
  state: {
    items:[],
    receptores: [],
    isConnected: false,
    error: null,
    ocupado: false,
    empresa: null,
  },
  getters: {
  },
  mutations,
  actions,
  modules: {
  }
})
