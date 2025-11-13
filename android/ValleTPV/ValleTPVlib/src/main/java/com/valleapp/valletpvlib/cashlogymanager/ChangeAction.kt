package com.valleapp.valletpvlib.cashlogymanager

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.valleapp.valletpvlib.tools.round

class ChangeAction(
    socketManager: CashlogySocketManager,
    uiHandler: Handler
) : CashlogyAction(socketManager, uiHandler) {

    private var importeAdmitido = 0.0
    private var isCancel = false
    private var isAceptar = true
    private var isSending = false
    private var denominacionMinima = 0
    private var esBloqueado = false
    private val denominacionesDisponibles = mutableMapOf<Int, Int>()

    private val handler: Handler = Handler(Looper.getMainLooper()) { msg ->
        val key = msg.data.getString("key")
        if (key == null  || key != "CASHLOGY_RESPONSE") return@Handler true

        val comando = msg.data.getString("instruccion")
        val response = msg.data.getString("value")

        if (comando != null && response != null ) {
            when {
                comando.startsWith("#Y") -> manejarRespuestaDenominaciones(response)
                comando.startsWith("#B") -> sendQCommand()
                comando.startsWith("#Q#") -> manejarRespuestaQ(response)
                comando.startsWith("#J#") -> manejarRespuestaJ(response)
                comando.startsWith("#U") -> manejarDispensacionCambio()
                comando.startsWith("#P") -> manejarRespuestaP()
            }
        }
        true
    }

    override fun execute() {
        importeAdmitido = 0.0
        denominacionMinima = 0
        isAceptar = true
        isCancel = false
        esBloqueado = false
        isSending = false
        socketManager.sendCommand("#Y#", handler)
    }

    private fun sendQCommand() {
        Handler(Looper.getMainLooper()).postDelayed({
                socketManager.sendCommand("#Q#", handler)
        }, 200)
    }

    private fun sendJCommand() {
        Handler(Looper.getMainLooper()).postDelayed({
                socketManager.sendCommand("#J#", handler)
        }, 200)
    }

    private fun sendPCommand(importe: Double) {
        val centimosADevolver = (importe * 100).toInt()
        val comandoDispensar = "#P#$centimosADevolver#0#0#0#"
        Handler(Looper.getMainLooper()).postDelayed({
                socketManager.sendCommand(comandoDispensar, handler)
        }, 200)
    }

    private fun manejarRespuestaDenominaciones(response: String) {
        try {
            Log.d("CASHLOGY", "Respuesta de denominaciones: $response")
            val parts = response.split("#")
            if (parts.size < 3) {
                Log.e("CASHLOGY", "Respuesta de denominaciones no válida.")
                notifyUI("CASHLOGY_ERR", "Respuesta de denominaciones no válida.")
                return
            }

            val recicladoresParte = parts[2].split(";")[0]
            val billetesParte = parts[2].split(";")[1]
            denominacionesDisponibles.clear()

            recicladoresParte.split(",").forEach { reciclador ->
                    val (valorEnCentimos, cantidad) = reciclador.split(":").map { it.toInt() }
                denominacionesDisponibles[valorEnCentimos] = cantidad
            }

            billetesParte.split(",").forEach { billete ->
                    val (valorEnCentimos, cantidad) = billete.split(":").map { it.toInt() }
                denominacionesDisponibles[valorEnCentimos] = cantidad
            }

            socketManager.sendCommand("#B#0#0#0#", handler)


        } catch (e: Exception) {
            Log.e("CASHLOGY", "Error al manejar la respuesta de denominaciones: $response Error: ${e.message}", e)
            notifyUI("CASHLOGY_ERR", "Error al manejar la respuesta de denominaciones.")
        }
    }

    private fun manejarRespuestaQ(response: String) {
        manejarRespuestaImporteAdmitido(response)
        if (isAceptar) {
            sendQCommand()
        } else {
            sendJCommand()
        }
    }

    private fun manejarRespuestaJ(response: String) {
        manejarRespuestaImporteAdmitido(response)
        calcularYDispensarCambio()
    }

    private fun manejarRespuestaImporteAdmitido(response: String) {
        val parts = response.split("#")
        if (parts.size >= 3) {
            val nuevoImporteAdmitido = (parts[2].toInt() / 100.0).round(2)

            if (nuevoImporteAdmitido != importeAdmitido) {
                importeAdmitido = nuevoImporteAdmitido
                notifyUI("CASHLOGY_IMPORTE_ADMITIDO", importeAdmitido.toString())

                val importeRestante = (importeAdmitido * 100).toInt()
                val denominacionesFiltradas = denominacionesDisponibles.filter { it.key <= importeRestante && it.value > 0 }
                notificarDenominacionesDisponiblesUI(denominacionesFiltradas)
            }
        }
    }

    private fun notificarDenominacionesDisponiblesUI(denominacionesDisponibles: Map<Int, Int>) {
        val cantidadesDisponibles = denominacionesDisponibles.entries.joinToString(",") { "${it.key}:${it.value}" }
        notifyUI("CASHLOGY_DENOMINACIONES_DISPONIBLES", cantidadesDisponibles)
    }

    private fun calcularYDispensarCambio() {
        if (isSending) return
        isSending = true

        if (isCancel) {
            if (importeAdmitido > 0) {
                sendPCommand(importeAdmitido)
            } else {
                notifyUI("CASHLOGY_CAMBIO", "Operación cancelada.")
            }
        } else {
            sendUCommand()
        }

        if (isCancel) {
            notifyUI("CASHLOGY_CAMBIO", "Operación cancelada.")
        } else {
            notifyUI("CASHLOGY_CAMBIO", "Operación finalizada.")
        }
    }

    private fun manejarRespuestaP() {
        notifyUI("CASHLOGY_CAMBIO", "Maquina lista para usaar")
    }

    private fun manejarDispensacionCambio() {
        val importeRestante = (importeAdmitido - denominacionMinima / 100.0).round(2)
        sendPCommand(importeRestante)
        notifyUI("CASHLOGY_CAMBIO", "Operación finalizada.")
    }

    private fun sendUCommand() {
        val comandoBuilder = StringBuilder("#U#")
        if (denominacionMinima < 500) {
            comandoBuilder.append("$denominacionMinima:1;")
        } else {
            comandoBuilder.append(";").append("$denominacionMinima:1")
        }
        comandoBuilder.append("#0#0#0#")
        socketManager.sendCommand(comandoBuilder.toString(), handler)
    }

    fun cancelar() {
        if (!esBloqueado) {
            esBloqueado = true
            isCancel = true
            isAceptar = false
        }
    }

    fun cambiar(denominacionMinima: Int) {
        if (importeAdmitido > 0 && !esBloqueado) {
            esBloqueado = true
            isCancel = false
            isAceptar = false
            this.denominacionMinima = denominacionMinima
        }
    }
}
