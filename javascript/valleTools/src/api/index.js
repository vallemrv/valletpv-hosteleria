//importamos todas la constantes de constantes.js endPoints
import * as endPoints from '@/constantes'
import axios from 'axios'

// Función para obtener un nuevo token
export async function getNewToken(username, password, url) {
  try {
    var params = new FormData()
    params.append('username', username);
    params.append("password", password)
    const response = await axios.post(url+'/token/new.json', params);

    if (response.status === 200 && response.data) {
      return response.data;
    }
  } catch (error) {
    console.error('Error al obtener el token:', error);
    return null;
  }
}


//funcion para update de datos
export  async function update (data) {
  const response = await axios.put(endPoints.UPDATE_REG, data)
  if (response.status === 200 && response.data) {
    return {error: null, data:response.data}
  }else{
    return {error:response.error, data:null}
  }
}

//funcion para delete de datos
export async function remove (data) {
  const response = await axios.delete(endPoints.DELETE_REG, data);
  if (response.status === 200 && response.data) {
    return {error: null, data:response.data}
  }else{
    return {error:response.error, data:null}
  }
   

}

//funcion para insertar datos
export async function insert (data) {
  const response = await axios.post(endPoints.ADD_REG, data);
  if (response.status === 200 && response.data) {
    return {error: null, data:response.data}
  }else{
    return {error:response.error, data:null}
  }
  
}

