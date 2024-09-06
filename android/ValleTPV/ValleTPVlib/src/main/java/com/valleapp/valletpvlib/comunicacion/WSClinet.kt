package com.valleapp.valletpvlib.comunicacion

import android.util.Log
import com.valleapp.valletpvlib.Interfaces.IControllerWS
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.util.Timer
import kotlin.concurrent.schedule

class WSClinet(
    private val serverUrl: String, // La URL del servidor (por ejemplo, "api.server.com")
    private val endpoint: String,  // El endpoint (por ejemplo, "/comunicacion/devices")
    private val controller: IControllerWS // Interfaz para sincronizar datos perdidos
) : WebSocketClient(constructUri(serverUrl, endpoint)) {

    private var reconnectAttempts = 0
    private val maxReconnectDelay = 30000L // Máximo 30 segundos entre reconexiones
    private var shouldReconnect = true // Controla si se debe intentar reconectar

    companion object {
        // Método para construir la URI WebSocket con la lógica de reemplazo de "api" por "ws"
        private fun constructUri(serverUrl: String, endpoint: String): URI {
            val wsUrl = serverUrl.replace("api", "ws") // Reemplaza "api" por "ws"
            val fullUrl = "ws://$wsUrl$endpoint" // Construye la URL completa
            return URI(fullUrl)
        }
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        println("Connected to server: ${uri}")
        reconnectAttempts = 0 // Reiniciamos el contador de intentos de reconexión
        controller.sincronizar() // Sincronizar datos perdidos al reconectar
    }

    override fun onMessage(message: String?) {
        Log.e("WEBSOCKET_INFO", message!!)
        val o = JSONObject(message)
        controller.procesarRespose(o)
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
        reconnectAttempts++
        val reconnectDelay = calculateReconnectDelay()
        println("Attempting to reconnect in $reconnectDelay ms...")

        // Reintentar la reconexión después del retraso calculado
        Timer().schedule(reconnectDelay) {
            if (shouldReconnect) {
                try {
                    println("Reconnecting...")
                    reconnect()
                } catch (e: Exception) {
                    println("Reconnection failed: ${e.message}")
                }
            }
        }
    }

    private fun calculateReconnectDelay(): Long {
        // Calcula el retraso con base en el número de intentos, sin superar el máximo
        val delay = (Math.min(maxReconnectDelay, (1000L * reconnectAttempts)))
        return delay.coerceAtMost(maxReconnectDelay)
    }

    fun sendMessage(message: String) {
        if (isOpen) {
            send(message)
            println("Message sent: $message")
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
