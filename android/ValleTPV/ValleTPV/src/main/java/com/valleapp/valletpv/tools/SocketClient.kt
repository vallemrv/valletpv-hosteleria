import android.os.Handler
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class SocketCliente(private val ip: String, private val port: Int, private val handler: Handler) {

    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    // Iniciar la conexión al servidor
    fun conectar() {
        thread {
            try {
                // Establecer la conexión
                socket = Socket(ip, port)
                writer = PrintWriter(OutputStreamWriter(socket!!.getOutputStream()), true)
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))

                Log.d("SocketCliente", "Conectado al servidor $ip:$port")

                // Iniciar la recepción de mensajes en un hilo separado
                recibirMensajes()

            } catch (e: Exception) {
                Log.e("SocketCliente", "Error al conectar: ${e.message}")
                handler.post {
                    // Enviar error a la UI
                    handler.obtainMessage(0, "Error al conectar").sendToTarget()
                }
            }
        }
    }

    // Método para enviar un cobro
    fun enviarCobro(monto: Double) {
        thread {
            try {
                val mensaje = JSONObject().apply {
                    put("comando", "iniciar_cobro")
                    put("importe", monto)
                }
                writer?.println(mensaje.toString())
                Log.d("SocketCliente", "Cobro enviado: $monto")
            } catch (e: Exception) {
                Log.e("SocketCliente", "Error al enviar cobro: ${e.message}")
                handler.post {
                    // Enviar error a la UI
                    handler.obtainMessage(0, "Error al enviar cobro").sendToTarget()
                }
            }
        }
    }

    // Método para cancelar un cobro
    fun cancelarCobro() {
        thread {
            try {
                val mensaje = JSONObject().apply {
                    put("comando", "cancelar_cobro")
                }
                writer?.println(mensaje.toString())
                Log.d("SocketCliente", "Cobro cancelado")
            } catch (e: Exception) {
                Log.e("SocketCliente", "Error al cancelar cobro: ${e.message}")
                handler.post {
                    // Enviar error a la UI
                    handler.obtainMessage(0, "Error al cancelar cobro").sendToTarget()
                }
            }
        }
    }

    // Método para recibir mensajes del servidor
    private fun recibirMensajes() {
        thread {
            try {
                var mensaje: String?
                while (socket != null && !socket!!.isClosed) {
                    mensaje = reader?.readLine()
                    if (mensaje != null) {
                        Log.d("SocketCliente", "Mensaje recibido: $mensaje")
                        // Enviar el mensaje a la UI usando el handler
                        handler.post {
                            handler.obtainMessage(1, mensaje).sendToTarget()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SocketCliente", "Error al recibir mensajes: ${e.message}")
                handler.post {
                    // Enviar error a la UI
                    handler.obtainMessage(0, "Error al recibir mensajes").sendToTarget()
                }
            }
        }
    }

    // Método para cerrar la conexión
    fun cerrarConexion() {
        try {
            socket?.close()
            writer?.close()
            reader?.close()
            Log.d("SocketCliente", "Conexión cerrada")
        } catch (e: Exception) {
            Log.e("SocketCliente", "Error al cerrar la conexión: ${e.message}")
        }
    }
}
