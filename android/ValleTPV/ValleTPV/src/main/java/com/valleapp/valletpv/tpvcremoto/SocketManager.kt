package com.valleapp.valletpv.tpvcremoto

import android.os.Bundle
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

class SocketManager(private val ip: String, private val port: Int) {

    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    var cancelado: Boolean = false

    fun iniciarConexionSocket(onSuccess: () -> Unit, onError: (String) -> Unit, onRespuesta: (Bundle) -> Unit) {
        thread {
            try {
                Log.d("SocketManager", "Iniciando conexión con el servidor TPVPC")
                socket = Socket()
                val socketAddress = InetSocketAddress(ip, port)
                socket?.connect(socketAddress, 5000)
                writer = socket?.getOutputStream()?.let { PrintWriter(it, true) }
                onSuccess()
                iniciarEscucha(onRespuesta)
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Error desconocido"
                Log.e("SocketManager", "Error al iniciar conexión: $errorMsg")
                onError(errorMsg)
            }
        }
    }

    fun iniciarCobro(totalToCobrar: Double) {
        thread {
            if (socket?.isConnected == true) {
                cancelado = false
                val mensajeCobro = "{\"comando\": \"iniciar_cobro\", \"importe\": $totalToCobrar}"
                writer?.println(mensajeCobro)
            } else {
                Log.e("SocketManager", "No se pudo conectar al servidor TPVPC para iniciar cobro")
            }
        }
    }

    fun cancelarCobro() {
        thread {
            if (socket?.isConnected == true) {
                cancelado = true
                val mensajeCancelacion = "{\"comando\": \"cancelar_cobro\"}"
                writer?.println(mensajeCancelacion)
            } else {
                Log.e("SocketManager", "No se pudo conectar al servidor TPVPC para cancelar cobro")
            }
        }
    }

    fun iniciarPinpad() {
        thread {
            if (socket?.isConnected == true) {
                cancelado = false
                val mensajeInicioPinpad = "{\"comando\": \"iniciar_sesion\"}"
                writer?.println(mensajeInicioPinpad)
            } else {
                Log.e("SocketManager", "No se pudo conectar al servidor TPVPC para iniciar pinpad")
            }
        }
    }

    private fun iniciarEscucha(onRespuesta: (Bundle) -> Unit) {
        thread {
            try {
                val socketActual = socket
                if (socketActual == null) {
                    Log.e("SocketManager", "El socket no está inicializado")
                    return@thread
                }
                val inputStream = socketActual.getInputStream()
                val reader = inputStream.bufferedReader()

                val buffer = CharArray(1024)
                var charsLeidos: Int
                val respuestaCompleta = StringBuilder()

                while (socketActual.isConnected && !socketActual.isClosed) {
                    try {
                        charsLeidos = reader.read(buffer)
                        if (charsLeidos > 0) {
                            respuestaCompleta.appendRange(buffer, 0, charsLeidos)
                            onRespuesta(procesarRespuesta(respuestaCompleta))
                            respuestaCompleta.clear()
                        } else if (charsLeidos == -1) {
                            Log.d("SocketManager", "Fin del stream alcanzado")
                            break // El servidor cerró la conexión limpiamente
                        }
                    } catch (e: IOException) {
                        Log.e("SocketManager", "Socket cerrado por el usuario" )
                        break
                    }
                }


            } catch (e: Exception) {
                Log.e("SocketManager", "Error al recibir respuesta: ${e.message}", e)
            }
        }
    }
    private fun procesarRespuesta(respuestaCompleta: StringBuilder): Bundle {
        val bundle = Bundle()
        return when {
            respuestaCompleta.contains("cancelado") -> {
                bundle.putString("estado", "cancelado")
                bundle
            }
            respuestaCompleta.contains("denegada") -> {
                bundle.putString("estado", "denegada")
                bundle
            }
            respuestaCompleta.contains("iniciado") -> {
                bundle.putString("estado", "iniciado")
                bundle
            }
            respuestaCompleta.contains("fallo") -> {
                bundle.putString("estado", "fallo")
                bundle
            }
            respuestaCompleta.contains("error") -> {
                bundle.putString("estado", "error")
                bundle
            }
            respuestaCompleta.contains("acpetado") -> {
                procesarAceptacion(respuestaCompleta, bundle)
                bundle
            }
            respuestaCompleta.contains("esperando") -> {
                bundle.putString("estado", "esperando")
                bundle
            }
            respuestaCompleta.contains("iniciando") -> {
                bundle.putString("estado", "iniciando")
                bundle
            }
            else -> { bundle }
        }
    }

    private fun procesarAceptacion(respuestaCompleta: StringBuilder, bundle: Bundle) {
        try {
            val res = JSONObject(respuestaCompleta.toString())
            bundle.putString("recibo", res.getString("recibo"))
        } catch (ignored: JSONException) {
            bundle.putString("recibo", "")
            Log.e("SocketManager", "Error al procesar aceptación JSON: ${ignored.message}", ignored)
        }

        bundle.putString("estado", "acpetado")
    }

    fun cerrarConexion() {
        try {
            if (socket?.isConnected == false) return
            socket?.close()
            Log.d("SocketManager", "Conexión cerrada exitosamente")
        } catch (e: IOException) {
            Log.e("SocketManager", "Error al cerrar la conexión: ${e.message}", e)
        }
    }
}