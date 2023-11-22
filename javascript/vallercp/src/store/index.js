import { createStore } from 'vuex'
import mutations from './mutations'
import actions from './actions'


export default createStore({
  state: {
    items:[],
    receptores: [],
    isWsConnected: false,
    isHttpConnected: false,
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
