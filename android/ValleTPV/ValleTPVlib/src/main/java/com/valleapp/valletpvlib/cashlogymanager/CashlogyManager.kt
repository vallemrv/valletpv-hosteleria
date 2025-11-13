package com.valleapp.valletpvlib.cashlogymanager

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.valleapp.valletpvlib.interfaces.IControllerWS
import com.valleapp.valletpvlib.comunicacion.WSClient
import org.json.JSONException
import org.json.JSONObject

class CashlogyManager(private val socketManager: CashlogySocketManager) : IControllerWS {

    private var ws: WSClient? = null

    fun openWS(server: String) {
        if (ws == null) {
            ws = WSClient(server, "/comunicacion/cashlogy", this)
            ws?.connect()
        }
        
    }

    fun closeWS() {
        ws?.stopReconnection()
    }

    // Método para inicializar la máquina Cashlogy
    fun initialize() {
        val action = InitializeAction(socketManager)
        action.execute()
    }

    // Método para realizar un pago
    fun makePayment(amount: Double, uiHandler: Handler): PaymentAction {
        val action = PaymentAction(socketManager, uiHandler, amount)
        action.execute()
        return action
    }

    fun makeChange(uiHandler: Handler): ChangeAction {
        val action = ChangeAction(socketManager, uiHandler)
        action.execute()
        return action
    }

    fun makeArqueo(cambio: Double, uiHandler: Handler): ArqueoAction {
        val arqueoAction = ArqueoAction(socketManager, uiHandler)
        arqueoAction.setFondoCaja(cambio)
        arqueoAction.execute()
        return arqueoAction
    }

    override fun sincronizar() {
        // Método vacío
    }

    override fun procesarRespose(o: JSONObject) {
        try {
            val device = o.getString("device")
            val instructionSend = o.getString("instruccion")
            if (device == "ValleTPV") return

            socketManager.sendCommand(instructionSend, Handler(Looper.getMainLooper()) {
                val key = it.data.getString("key")
                val value = it.data.getString("value")

                if (key == "CASHLOGY_RESPONSE") {
                    try {
                        val instructionReceived = it.data.getString("instruccion")
                        Log.d("CASHLOGY", "Instruccion enviada: $instructionReceived")
                        Log.d("CASHLOGY", "Respuesta recibida: $value")

                        val res = JSONObject().apply {
                            put("device", "ValleTPV")
                            put("respuesta", value)
                            put("instruccion", instructionReceived)
                        }
                        ws?.sendMessage(res.toString())

                    } catch (e: Exception) {
                        Log.e("CASHLOGY", "Error al procesar respuesta: ${e.message}")
                    }
                }
                true
            })

        } catch (e: JSONException) {
            Log.e("CASHLOGY", "Error al procesar respuesta: ${e.message}")
        }
    }
}
