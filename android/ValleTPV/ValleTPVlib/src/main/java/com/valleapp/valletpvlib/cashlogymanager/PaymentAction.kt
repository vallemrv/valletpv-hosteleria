package com.valleapp.valletpvlib.cashlogymanager

import android.os.Handler
import android.os.Looper
import java.util.Locale

class PaymentAction(
        socketManager: CashlogySocketManager,
        iuHandler: Handler,
        private val amountToCollect: Double
) : CashlogyAction(socketManager, iuHandler) {

        private var admittedAmount = 0.0
        private var isCancel = false
        private var isAceptar = true
        private var esBloqueado = false
        private var isPCommandSent = false

        init {
            this.admittedAmount = String.format(Locale.UK, "%.2f", amountToCollect).toDouble()
        }

        private val handler: Handler = Handler(Looper.getMainLooper()) { msg ->
            val key = msg.data.getString("key")
            if (key == null  || key != "CASHLOGY_RESPONSE") return@Handler true

            val command = msg.data.getString("instruccion")
            val response = msg.data.getString("value")
            if (command != null && response != null) {
                when {
                    command.startsWith("#B") -> sendQCommand()
                    command.startsWith("#Q#") -> handleQResponse(response)
                    command.startsWith("#J#") -> handleJResponse(response)
                    command.startsWith("#P") -> handlePResponse()
                }
            }
            true
        }

        override fun execute() {
            admittedAmount = 0.0
            isAceptar = true
            esBloqueado = false
            isPCommandSent = false
            sendBCommand()
        }

        private fun sendBCommand() {
            socketManager.sendCommand("#B#0#0#0#", handler)
        }

        private fun sendQCommand() {
            Handler(Looper.getMainLooper()).postDelayed({
                    socketManager.sendCommand("#Q#", handler)
            }, 200)
        }

        private fun handleQResponse(response: String) {
            val parts = response.split("#")
            if (parts.size >= 3) {
                val importeRecibido = String.format(Locale.UK, "%.2f", parts[2].toDouble() / 100.0).toDouble()
                if (importeRecibido != admittedAmount) {
                    admittedAmount = importeRecibido
                    notifyUI("CASHLOGY_IMPORTE_ADMITIDO", admittedAmount.toString())
                }
            }
            if (isAceptar) {
                sendQCommand()
            } else {
                sendJCommand()
            }
        }

        private fun sendJCommand() {
            Handler(Looper.getMainLooper()).postDelayed({
                    socketManager.sendCommand("#J#", handler)
            }, 200)
        }

        private fun handleJResponse(response: String) {
            val parts = response.split("#")
            if (parts.size >= 3) {
                val importeRecibido = String.format(Locale.UK, "%.2f", parts[2].toDouble() / 100.0).toDouble()
                if (importeRecibido != admittedAmount) {
                    admittedAmount = importeRecibido
                    notifyUI("CASHLOGY_IMPORTE_ADMITIDO", admittedAmount.toString())
                }
            }
            sendPCommand()
        }

        private fun sendPCommand() {
            if (!isPCommandSent) {
                isPCommandSent = true
                val importeADevolver = if (!isCancel) {
                    Math.round((admittedAmount - amountToCollect) * 100.0)

                } else {
                    Math.round(admittedAmount * 100.0)
                }

                if (importeADevolver > 0) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val comandoP = "#P#$importeADevolver#0#0#0#"
                        socketManager.sendCommand(comandoP, handler)
                    }, 200)
                }
                if (!isCancel) {
                    notifyUI("CASHLOGY_COBRO_COMPLETADO", "Cobro completado.")
                } else {
                    notifyUI("CASHLOGY_COBRO_COMPLETADO", "Cobro cancelado.")
                }
            }
        }

        private fun handlePResponse() {
           notifyUI("CASHLOGY_COBRO_COMPLETADO", "Maquina lista de nuevo.")
        }

        fun cancelarCobro(): Boolean {
            return if (!esBloqueado) {
                esBloqueado = true
                isCancel = true
                isAceptar = false
                true
            } else {
                false
            }
        }

        fun cobrar() {
            if (!esBloqueado) {
                esBloqueado = true
                isCancel = false
                isAceptar = false
            }
        }

        fun sePuedeCobrar(): Boolean {
            val cambio = String.format(Locale.UK, "%.2f", admittedAmount - amountToCollect).toDouble()
            return cambio >= 0
        }
}
