import axios from "axios";

// Función para determinar el protocolo correcto
const getProtocol = (server) => {
    // Si el servidor ya incluye el protocolo, lo respetamos
    if (server.startsWith('http://') || server.startsWith('https://')) {
        return server;
    }
    
    // Si estamos en un entorno HTTPS, usamos HTTPS por defecto
    if (window.location.protocol === 'https:') {
        return `https://${server}`;
    }
    
    // Si el servidor contiene el puerto 443 o palabras clave de HTTPS, usamos HTTPS
    if (server.includes(':443') || server.includes('ssl') || server.includes('secure')) {
        return `https://${server}`;
    }
    
    // Por defecto, usamos HTTP
    return `http://${server}`;
};

const post = (path, params) => {
    if (localStorage.server){
        var server = localStorage.server;
        const fullUrl = getProtocol(server) + path;
        console.log('API Call:', fullUrl, params);
        return axios.post(fullUrl, params, {
            // Configuración adicional para HTTPS
            timeout: 10000,
            headers: {
                'Content-Type': 'multipart/form-data',
            }
        }).then(r => r.data)
    } else {
        console.error('Error: localStorage.server no está configurado');
        return Promise.reject(new Error('Servidor no configurado'));
    }
}

export default {
    recuperar_pedido(params){
        return post("/api/recuperar_pedido", params)
    },
    get_datos_empresa(){
        return post("/api/get_datos_empresa")
    },
    get_pedidos(params){
        return post("/api/get_pedidos_by_receptor", params)
    },
    get_listado(){
        return post("/api/receptores/get_lista")
    },
    
    // Función auxiliar para obtener la URL base
    getBaseUrl(){
        if (localStorage.server){
            return getProtocol(localStorage.server);
        }
        return null;
    }
}