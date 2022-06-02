import axios from 'axios'

const post = (server, path, params) => {
    return axios.post("http://"+server+"/"+path, params, {}).then(r => r.data)
}

export default {
    get_datos_empresa(server, params){
        return post(server, "app/get_datos_empresa", params)
    },
    borrar_ventas(server, params){
        return post("app/reset_db", params)
    },
    modificarSecciones(server, params){
        return post(server, "app/mod_sec", params)
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
    }
}