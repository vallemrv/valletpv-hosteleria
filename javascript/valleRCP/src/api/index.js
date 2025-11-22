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
    // Verificar salud del servidor
    async checkHealth(serverUrl = null){
        const url = serverUrl || (localStorage.server ? getProtocol(localStorage.server) : null)
        if (!url) {
            throw new Error('No hay URL de servidor configurada')
        }
        
        try {
            const response = await axios.get(`${url}/api/health`, {
                timeout: 5000
            })
            return response.data
        } catch (error) {
            console.error('Error al verificar salud del servidor:', error)
            throw error
        }
    },
    
    // Crear o recuperar UID del dispositivo con alias
    async create_uid(alias){
        if (!alias) {
            throw new Error('El alias es requerido para crear el UID')
        }
        
        const formData = new FormData()
        formData.append('alias', alias)
        
        const response = await post("/api/dispositivo/create_uid", formData)
        if (response && response.uid) {
            // Guardar el UID en localStorage
            localStorage.deviceUID = response.uid
            return response.uid
        }
        throw new Error('No se pudo obtener el UID del dispositivo')
    },
    
    // Método auxiliar para agregar UID a los parámetros
    addUIDToParams(params = null){
        const uid = localStorage.deviceUID
        if (!uid) {
            console.warn('Advertencia: No hay UID disponible')
            throw new Error('No hay UID disponible. Por favor, configura el servidor primero.')
        }
        
        // Si params es FormData, agregar uid
        if (params instanceof FormData) {
            params.append('uid', uid)
            return params
        }
        
        // Crear FormData y agregar UID
        const formData = new FormData()
        formData.append('uid', uid)
        
        // Agregar otros parámetros si existen
        if (params && typeof params === 'object') {
            Object.keys(params).forEach(key => {
                formData.append(key, params[key])
            })
        }
        
        return formData
    },
    
    // Sincronizar pedidos con el servidor
    // Envía todos los pedidos que tiene en local y recibe los actualizados
    async sincronizar_pedidos(receptor, pedidosLocales, nom_receptor = null){
        const formData = this.addUIDToParams()
        formData.append('receptor', receptor)
        
        // Enviar también el nombre del receptor si está disponible
        if (nom_receptor) {
            formData.append('nom_receptor', nom_receptor)
        }
        
        // Enviar array de pedidos locales (solo IDs o pedidos completos según necesites)
        formData.append('pedidos_locales', JSON.stringify(pedidosLocales))
        
        return post("/api/sincronizar_pedidos", formData)
    },
    
    // Obtener lista de receptores (requiere UID)
    async get_listado(){
        const uid = localStorage.deviceUID
        
        
        if (!uid) {
            throw new Error('No hay UID disponible. Por favor, configura el servidor primero.')
        }
        
        return post("/api/receptores/get_lista", this.addUIDToParams())
    },
    
    // Función auxiliar para obtener la URL base
    getBaseUrl(){
        if (localStorage.server){
            return getProtocol(localStorage.server);
        }
        return null;
    }
}