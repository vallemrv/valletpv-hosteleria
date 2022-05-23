import axios from "axios";

const post = (path, params) => {
    if (localStorage.server){
        var server = localStorage.server;
        return axios.post("http://"+server+path, params, {}).then(r => r.data)
    }
}

export default {
    get_datos_empresa(){
        return post("/api/get_datos_empresa")
    },
    get_pedidos(params){
        return post("/api/get_pedidos_by_receptor", params)
    },
    get_listado(){
        return post("/api/receptores/get_lista")
    }
}