package com.valleapp.valletpvlib.comunicacion

import android.os.Handler
import android.os.Looper
import com.valleapp.valletpvlib.interfaces.IControllerWS
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

class WSClient(
    serverUrl: String, // La URL del servidor (por ejemplo, "api.server.com")
    endpoint: String,  // El endpoint (por ejemplo, "/comunicacion/devices")
    private val controller: IControllerWS // Interfaz para sincronizar datos perdidos
) : WebSocketClient(constructUri(serverUrl, endpoint)) {

    private val reconnectHandler = Handler(Looper.getMainLooper())

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
            // Eliminar http:// o https:// del serverUrl
            val cleanServerUrl = serverUrl.removePrefix("http://").removePrefix("https://")
            val wsUrl = when {
                serverUrl.contains("api") -> cleanServerUrl.replace("api", "ws")
                endpoint.startsWith("/ws") -> cleanServerUrl.replace("api", "")
                else -> "$cleanServerUrl/ws"
            }
            val fullUrl = "$protocolo$wsUrl$endpoint" // Construye la URL completa
            println("Constructed URI: $fullUrl")
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
        if (shouldReconnect) {
            attemptReconnect() // Intentamos reconectar en caso de desconexión si está permitido
        }
    }

    override fun onError(ex: Exception?) {
        println("An error occurred: ${ex?.message}")
        if (shouldReconnect) {
            attemptReconnect() // Intentamos reconectar en caso de error si está permitido
        }
    }

    private fun attemptReconnect() {
        if (!shouldReconnect) return // Salir si no debemos reconectar

        reconnectAttempts++
        val reconnectDelay = calculateReconnectDelay()
        println("Attempting to reconnect in $reconnectDelay ms...")

        // 2. Usa el handler en lugar de un Timer nuevo
        reconnectHandler.postDelayed({
            try {
                if (shouldReconnect) {
                    println("Reconnecting...")
                    reconnect()
                }
            } catch (e: Exception) {
                println("Reconnection failed: ${e.message}")
            }
        }, reconnectDelay)
    }

    // Asegúrate de limpiar el handler cuando el socket se cierre definitivamente
    fun stopReconnection() {
        shouldReconnect = false
        reconnectHandler.removeCallbacksAndMessages(null) // Limpia las reconexiones pendientes
        close()
        println("Reconnection stopped and WebSocket closed")
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



}
