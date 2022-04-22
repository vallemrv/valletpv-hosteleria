import axios from 'axios'
//const url = "/"
const url = "http://localhost:8000/"

export default {
    getTeclados(params){
        return axios.post(url+"app/getTeclados",  params, {}).then(r => r.data);
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