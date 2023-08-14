package com.valleapp.valletpvlib.tools

import com.valleapp.valletpvlib.interfaces.IController
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.nio.ByteBuffer

class WSClient(serverUri: String, end_point: String, private val controller: IController) : WebSocketClient(URI(urlParse(serverUri) + end_point)) {

    private var isWebsocketClose = false
    private var exit = false



    companion object {
        fun urlParse(url: String): String {
            var newUrl = url
            if (newUrl.contains("/api")) newUrl = newUrl.substring(0, newUrl.indexOf("/api"))
            when {
                newUrl.startsWith("http://") -> newUrl = newUrl.replace("http://", "ws://")
                newUrl.startsWith("https://") -> newUrl = newUrl.replace("https://", "wss://")
                else -> newUrl = "ws://$newUrl"
            }
            return newUrl
        }
    }

    override fun onOpen(serverHandshake: ServerHandshake) {
        isWebsocketClose = false
        println("Websocket open.....")
        controller.sync_device(arrayOf("mesasabiertas", "lineaspedido", "camareros"), 500)
    }

    override fun onMessage(message: String) {
        try {
            val o = JSONObject(message)
            controller.updateTables(o)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onError(ex: Exception) {
        println("Error de conexion....")
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        println("Websocket close....")
        isWebsocketClose = true

        if (!exit) {
            try {
                this.reconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMessage(bytes: ByteBuffer) {
        println("socket bytebuffer bytes")
    }

    override fun onWebsocketPong(conn: WebSocket, f: Framedata) {
        super.onWebsocketPong(conn, f)
    }

    fun salir() {
        exit = true // Indica que no se debe intentar la reconexión automática
        close()
    }

    fun isWebsocketClosed(): Boolean {
        return isWebsocketClose
    }
}
