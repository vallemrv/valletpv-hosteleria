import axios from 'axios'
const url = "/"
//const url = "http://tpvsl.valletpv.es/"
//const url = "http://localhost:8000/"

const post = (path, params) => {
    return axios.post(url+path, params, {}).then(r => r.data)
}

export default {
    borrar_ventas(params){
        return post("app/reset_db", params)
    },
    modificarSecciones(params){
        return post("app/mod_sec", params)
    },
    addItem(params){
        return post("app/add_reg", params)
    },
    actualizar(params){
        return post("app/mod_regs", params)
    },
    getListadoCompuesto(params){
        return post("app/getListadoCompuesto",  params)
    },
    getListado(params){
        return post("app/getListado",  params)
    },
    login(params){
        return post("token/new.json", params)
    }
}