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
                onError(e.message ?: "Error desconocido")
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
                throw Exception("No se pudo conectar al servidor TPVPC")
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
                throw Exception("No se pudo conectar al servidor TPVPC")
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
                throw Exception("No se pudo conectar al servidor TPVPC")
            }
        }
    }

    private fun iniciarEscucha(onRespuesta: (Bundle) -> Unit) {
        thread {
            try {
                val socketActual = socket ?: throw Exception("El socket no está inicializado")
                val inputStream = socketActual.getInputStream()
                val reader = inputStream.bufferedReader()

                val buffer = CharArray(1024)
                var charsLeidos: Int
                val respuestaCompleta = StringBuilder()

                // Bucle de lectura continua mientras el socket esté conectado
                while (socketActual.isConnected) {

                    try {
                        // Leer los datos recibidos en el buffer
                        charsLeidos = reader.read(buffer)

                        if (charsLeidos > 0) {
                            // Acumular la respuesta recibida
                            respuestaCompleta.appendRange(buffer, 0, charsLeidos)

                            // Procesar la respuesta recibida
                            onRespuesta(procesarRespuesta(respuestaCompleta))

                            // Limpiar el buffer después de procesar la respuesta
                            respuestaCompleta.clear()
                        }
                    } catch (e: IOException) {
                        Log.i("SocketManager", "Error de I/O en la recepción: ${e.message}")
                        break
                    }
                }

            } catch (e: Exception) {
                Log.e("SocketManager", "Error al recibir respuesta: ${e.message}")
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
        }

        bundle.putString("estado", "acpetado")


   }

    fun cerrarConexion() {
        try {
            if (socket?.isConnected == false) return
            socket?.close()
            Log.d("SocketManager", "Conexión cerrada exitosamente")
        } catch (e: IOException) {
            Log.e("SocketManager", "Error al cerrar la conexión: ${e.message}")
        }
    }
}
