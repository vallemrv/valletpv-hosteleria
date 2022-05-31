import API from "@/api"
import * as types from "./mutations_types"

export default {
    getEmpresa({commit, state}){
      API.get_datos_empresa().then((result) => {
          commit(types.SET_EMPRESA, result);
      })
      .catch((error)=>{
          commit(types.ERROR_REQUEST, error);
      })
    }
}