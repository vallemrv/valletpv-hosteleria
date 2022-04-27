import axios from 'axios'
//const url = "/"
const url = "http://localhost:8000/"

const post = (path, params) => {
    return axios.post(url+path, params, {}).then(r => r.data)
}

export default {
    modificarSecciones(params){
        return post("app/mod_sec", params)
    },
    addItem(params){
        return axios.post(url+"app/add_reg", params, {}).then(r => r.data)
    },
    actualizar(params){
        return axios.post(url+"app/mod_regs", params, {}).then(r => r.data)
    },
    getListadoCompuesto(params){
        return axios.post(url+"app/getListadoCompuesto",  params, {}).then(r => r.data);
    },
    getListado(params){
        return axios.post(url+"app/getListado",  params, {}).then(r => r.data);
    },
    login(params){
        return axios.post(url+"token/new.json", params, {}).then(r=>r.data)
    }
}