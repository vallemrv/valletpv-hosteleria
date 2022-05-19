import * as types from "./mutations_types"
import { createStore } from 'vuex'
import axios from 'axios'


const post = (path, params) => {
  return axios.post(path, params, {}).then(r => r.data)
}

var get_listado = (server, params) =>{
  return post("http://"+server+"/api/sync/update_for_devices", params)
}

export default createStore({
  state: {
    items:[],
    receptores: [],
    isConnected: false,
    error: null,
    ocupado: false,
  },
  getters: {
  },
  mutations: {
    [types.RECEPCION_PEDIDO] (state, {pedido}){
       state.items.push(pedido)
    },
    [types.ON_CONNECT] (state){
       state.isConnected = true
    },
    [types.ON_DISCONECT] (state){
      state.isConnected = false;
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
    [types.GET_LISTADOS] (state, {result}){
        state.ocupado = false;
        state.error = null
        state[result.nombre] = Object.values(result.objs).filter((r)=>{
          return r.nombre != "Nulo"
        })
    },
  },
  actions: {
    getListado({ commit, state }, {tabla, server}){
      commit(types.GET_REQUEST)
      var params = new FormData();
      params.append("tb", tabla);
      get_listado(server, params)
      .then( r => commit(types.GET_LISTADOS, {result: r}))
      .catch(error => {
          commit(types.ERROR_REQUEST, {error: error})
      })
    },
  },
  modules: {
  }
})
