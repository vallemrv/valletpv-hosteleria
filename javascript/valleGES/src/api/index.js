import axios from 'axios';
import { NEWTOKEN } from '@/endpoints';

export function buildUrl(url, endpoint) {
  // Agrega 'http://' al inicio si no tiene protocolo
  if (!/^https?:\/\//i.test(url)) {
      url = 'http://' + url;
  }
  return url + endpoint;
}

// Función para obtener un nuevo token
export async function getNewToken(username, password, url) {
  try {
    url = buildUrl(url, NEWTOKEN);
    var params = new FormData()
    params.append('username', username);
    params.append("password", password)
    const response = await axios.post(url, params);

    if (response.status === 200 && response.data) {
      return response.data;
    }
  } catch (error) {
    console.error('Error al obtener el token:', error);
    return null;
  }
}
