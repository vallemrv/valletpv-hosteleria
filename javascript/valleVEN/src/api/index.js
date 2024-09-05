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
    borrarMesa(server, params){
        return post(server, "app/ventas/borrar_mesa", params)
    },
    getInfMesa(server, params){
        return post(server, "app/ventas/get_infomesa", params)
    },
    sendCobrarMesa(server, params){
        return post(server, "app/ventas/send_cobrar_mesa", params)
    },
    getNulos(server, params){
        return post(server, "app/ventas/get_nulos", params)
    },
    getListdoMesas(server, params){
        return post(server, "app/ventas/get_list_mesas", params)
    },
    getListadoArqueos(server, params){
        return post(server, "app/ventas/get_list_arqueos", params)
    }
}