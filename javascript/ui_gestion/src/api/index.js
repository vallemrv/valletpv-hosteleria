import axios from 'axios'
const url = "http://localhost:8000/"


export default {
    getListado(params){
        return axios.post(url+"getlistado",  params, {}).then(r => r.data);
    },
    login(params){
        return axios.post(url+"token/new.json", params, {}).then(r=>r.data)
    }
}