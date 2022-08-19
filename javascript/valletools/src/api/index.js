import axios from 'axios'

const post = (server, path, params) => {
    return axios.post("http://"+server+"/"+path, params, {}).then(r => r.data)
}

export default {
    gestionar_peticion(server, params){
        return post(server, "api/autorizaciones/gestionar_peticion", params)
    },
    get_lista_autorizaciones(server){
        return post(server, "api/autorizaciones/get_lista_autorizaciones", new FormData())
    },
    dataset(server, params){
        return post(server, "app/ventas/datasets", params)
    },
    get_datos_empresa(server, params){
        return post(server, "app/get_datos_empresa", params)
    },
    addItem(server, params){
        return post(server, "app/add_reg", params)
    },
    actualizar(server, params){
        return post(server, "app/mod_regs", params)
    },
    getListadoCompuesto(server, params){
        return post(server, "app/getListadoCompuesto",  params)
    },
    getListado(server, params){
        return post(server, "app/getListado",  params)
    },
    login(server, params){
        return post(server, "token/new.json", params)
    },
    getMesasAbiertas(server, params){
        
        return post(server, "app/ventas/mesas_abiertas", params)
    }
}