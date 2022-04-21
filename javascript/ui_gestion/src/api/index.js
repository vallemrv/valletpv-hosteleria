import axios from 'axios'
const url = "http://localhost:8000/"


export default {
    getListado(params){
        return axios.post(url+"getlistado",  params, {}).then(r => r.data);
    }
}