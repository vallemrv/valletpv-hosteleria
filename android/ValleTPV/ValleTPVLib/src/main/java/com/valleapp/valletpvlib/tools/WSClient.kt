package com.valleapp.valletpvlib.tools

import com.valleapp.valletpvlib.interfaces.IController
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.nio.ByteBuffer

class WSClient(serverUri: String, endPoint: String, private val controller: IController) : WebSocketClient(URI(serverUri + endPoint)) {

    private var isWebsocketClose = false
    private var exit = false


    override fun onOpen(serverHandshake: ServerHandshake) {
        isWebsocketClose = false
        println("Websocket open.....")
        controller.syncDevice(listOf( "camareros", "mesas", "zonas"))
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
                Thread{
                    Thread.sleep(5000)
                    reconnect()
                }.start()


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

}
