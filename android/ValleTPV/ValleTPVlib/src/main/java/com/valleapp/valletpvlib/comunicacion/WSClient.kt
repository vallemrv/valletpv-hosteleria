package com.valleapp.valletpvlib.comunicacion

import com.valleapp.valletpvlib.interfaces.IControllerWS
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

class WSClient(
    serverUrl: String, // La URL del servidor (por ejemplo, "api.server.com")
    endpoint: String,  // El endpoint (por ejemplo, "/comunicacion/devices")
    private val controller: IControllerWS, // Interfaz para sincronizar datos perdidos
    private val viewModelScope: CoroutineScope // Inyectar el viewModelScope
) : WebSocketClient(constructUri(serverUrl, endpoint)) {

    private var reconnectAttempts = 0
    private val maxReconnectDelay = 30000L // Máximo 30 segundos entre reconexiones
    private var shouldReconnect = true // Controla si se debe intentar reconectar

    companion object {
        // Método para construir la URI WebSocket con la lógica de reemplazo de "api" por "ws"
        private fun constructUri(serverUrl: String, endpoint: String): URI {
            val protocolo = when {
                serverUrl.startsWith("http://") -> "ws://"
                serverUrl.startsWith("https://") -> "wss://"
                else -> "ws://"
            }

            val wsUrl =  when {
                serverUrl.contains("api") -> serverUrl.replace("api", "ws")
                endpoint.startsWith("/ws") -> serverUrl.replace("api", "")
                else -> "$serverUrl/ws"
            }


            val fullUrl = "$protocolo$wsUrl$endpoint" // Construye la URL completa
            return URI(fullUrl)
        }
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        println("Connected to server: $uri")
        reconnectAttempts = 0 // Reiniciamos el contador de intentos de reconexión
        controller.sincronizar() // Sincronizar datos perdidos al reconectar
    }

    override fun onMessage(message: String?) {
        val o = message?.let { JSONObject(it) }
        if (o != null) {
            controller.procesarRespose(o)
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("Connection closed with exit code $code, reason: $reason")
        if (shouldReconnect) attemptReconnect()
    }

    override fun onError(ex: Exception?) {
        println("An error occurred: ${ex?.message}")
        if (shouldReconnect) attemptReconnect()
    }

    private fun attemptReconnect() = viewModelScope.launch { // Usar viewModelScope para controlar el ciclo de vida
        reconnectAttempts++
        val reconnectDelay = calculateReconnectDelay()
        println("Attempting to reconnect in $reconnectDelay ms...")

        delay(reconnectDelay) // Utilizar corrutinas para el retraso
        if (shouldReconnect) {
            try {
                println("Reconnecting...")
                reconnect()
            } catch (e: Exception) {
                println("Reconnection failed: ${e.message}")
            }
        }
    }

    private fun calculateReconnectDelay(): Long {
        // Calcula el retraso con base en el número de intentos, sin superar el máximo
        val delay = (maxReconnectDelay.coerceAtMost((30000L * reconnectAttempts)))
        return delay.coerceAtMost(maxReconnectDelay)
    }

    fun sendMessage(message: String) {
        if (isOpen) {
            val o =  JSONObject()
            o.put("content", message)
            send(o.toString())
        } else {
            println("WebSocket is not open")
        }
    }

    // Método para detener la reconexión
    fun stopReconnection() {
        shouldReconnect = false
        close() // Cierra la conexión WebSocket
        println("Reconnection stopped and WebSocket closed")
    }
}