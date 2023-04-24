import axios from 'axios';

// Configura axios con la URL base de tu servidor Django
const api = axios.create();

// Función para obtener un nuevo token
export async function getNewToken(username, password, url) {
  try {
    var params = new FormData()
    params.append('username', username);
    params.append("password", password)
    const response = await api.post(url+'/token/new.json', params);

    if (response.status === 200 && response.data) {
      return response.data;
    }
  } catch (error) {
    console.error('Error al obtener el token:', error);
    return null;
  }
}
