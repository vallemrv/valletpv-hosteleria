import axios from "axios";

const post = (path: string, params: FormData = new FormData()) => {
    if (localStorage.server){
        const server = localStorage.server;
        return axios.post("http://"+server+path, params, {}).then(r => r.data)
    }
}

export default {
    recuperar_pedido(params: FormData){
        return post("/api/recuperar_pedido", params)
    },
    get_datos_empresa(){
        return post("/api/get_datos_empresa")
    },
    get_pedidos(params: FormData){
        return post("/api/get_pedidos_by_receptor", params)
    },
    get_listado(){
        return post("/api/receptores/get_lista")
    }
}