package com.valleapp.valletpv.tools

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.valleapp.valletpv.R
import com.valleapp.valletpv.ValleTPV
import com.valleapp.valletpvlib.cashlogymanager.ArqueoAction
import com.valleapp.valletpvlib.cashlogymanager.CashlogyManager
import com.valleapp.valletpvlib.cashlogymanager.CashlogySocketManager
import com.valleapp.valletpvlib.cashlogymanager.ChangeAction
import com.valleapp.valletpvlib.cashlogymanager.PaymentAction
import com.valleapp.valletpvlib.ServiceComBase
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.tools.Instrucciones
import org.json.JSONArray
import org.json.JSONObject

class ServiceCOM: ServiceComBase() {

    private val myBinder: IBinder = MyBinder()

    private val channelId = "ValleTPV"

    private var urlCashlogy: String? = null
    private var usarCashlogy: Boolean = false
    private var cashlogySocketManager: CashlogySocketManager? = null
    private var cashlogyManager: CashlogyManager? = null

    override fun startForegroundService() {

        // Verifica si el canal ya existe
        val channel = NotificationChannel(
            channelId,
            "ValleCASH service",
            NotificationManager.IMPORTANCE_DEFAULT  // IMPORTANCE_HIGH si necesitas más visibilidad
        )
        val manager = getSystemService(NotificationManager::class.java)
        if (manager?.getNotificationChannel(channelId) == null) {
            manager?.createNotificationChannel(channel)
        }

        // Configurar la notificación para el servicio en primer plano
        val notificationIntent = Intent(this, ValleTPV::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("WebSocket activo")
            .setContentText("Conectado a $server")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // Ajusta según sea necesario
            .build()

        // Iniciar el servicio en primer plano con la notificación
        startForeground(1, notification)
    }



    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val out = super.onStartCommand(intent, flags, startId)
        urlCashlogy = intent.getStringExtra("url_cashlogy") // Recoger URL de Cashlogy
        usarCashlogy = intent.getBooleanExtra("usar_cashlogy", false) // Recoger estado del CheckBox

        // Iniciar CashlogySocketManager si está habilitado
        if (usarCashlogy && urlCashlogy != null) {
            iniciarCashlogySocketManager(urlCashlogy)
        }
        return out
    }

    override fun onBind(intent: Intent): IBinder {
        return myBinder
    }

    override fun onDestroy() {
        cerrarCashlogySocketManager()

    }

    fun abrirCajon() {
        if (!usarCashlogy) {
            if (server != null) HTTPRequest(
                "$server/impresion/abrircajon",
                ContentValues(), "abrir_cajon", controller_http
            )
        }
    }

    fun getLineasTicket(mostrarLsTicket: Handler?, idTicket: String?) {
        val p = ContentValues()
        p.put("id", idTicket)
        HTTPRequest(
            "$server/cuenta/lslineas", p,
            "get_lineas_ticket", mostrarLsTicket
        )
    }

    fun getListaTickets(hLsTicket: Handler?) {
        HTTPRequest(
            "$server/cuenta/lsticket", ContentValues(),
            "get_lista_ticket", hLsTicket
        )
    }

    fun imprimirTicket(idTicket: String?) {
        val p = ContentValues()
        p.put("id", idTicket)
        p.put("abrircajon", "False")
        p.put("receptor_activo", "True")
        HTTPRequest("$server/impresion/imprimir_ticket", p, "", controller_http)
    }

    fun imprimirFactura(idTicket: String?) {
        val p = ContentValues()
        p.put("id", idTicket)
        p.put("abrircajon", "False")
        p.put("receptor_activo", "True")
        HTTPRequest("$server/impresion/imprimir_factura", p, "", controller_http)
    }

    fun getSettings(controller: Handler?) {
        HTTPRequest(
            "$server/receptores/get_lista", ContentValues(),
            "get_lista_receptores", controller
        )
    }

    fun setSettings(lista: String?) {
        val p = ContentValues()
        p.put("lista", lista)
        HTTPRequest(
            "$server/receptores/set_settings", p,
            "set_settings", controller_http
        )
    }

    fun rmMesa(params: ContentValues?) {
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(params, "$server/cuenta/rm"))
        }
    }


    fun rmLinea(params: ContentValues?) {
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(params, "$server/cuenta/rmlinea"))
        }
    }

    fun cobrarCuenta(params: ContentValues?) {
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(params, "$server/cuenta/cobrar"))
        }
    }

    fun preImprimir(p: ContentValues?) {
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(p, "$server/impresion/preimprimir"))
        }
    }


    fun getCuenta(controller: Handler?, p: ContentValues?) {
        HTTPRequest("$server/cuenta/get_cuenta", p, "get_cuenta", controller)
    }

    fun addCamNuevo(n: String?, a: String?) {
        val p = ContentValues()
        p.put("nombre", n)
        p.put("apellido", a)
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(p, "$server/camareros/camarero_add"))
        }
    }


    fun autorizarCam(obj: JSONObject?) {
        val p = ContentValues()
        val o = JSONArray()
        o.put(obj)
        p.put("rows", o.toString())
        p.put("tb", "camareros")
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(p, "$server/sync/update_from_devices"))
        }
    }

    fun pedirAutorizacion(p: ContentValues?) {
        HTTPRequest(
            "$server/autorizaciones/pedir_autorizacion",
            p, "", null
        )
    }

    /*
    Metodos para la integracion del cashlogy
    */
    private fun iniciarCashlogySocketManager(urlCashlogy: String?) {
        if (cashlogySocketManager == null) {
            // Inicializar el CashlogySocketManager con la URL de Cashlogy y el handler para la UI
            cashlogySocketManager = urlCashlogy?.let { CashlogySocketManager(it) }
            cashlogySocketManager!!.start() // Iniciar la conexión con Cashlogy

            // Ejecutar la acción de inicialización de Cashlogy
            cashlogyManager = CashlogyManager(cashlogySocketManager!!)
            cashlogyManager!!.initialize()
            server?.let { cashlogyManager!!.openWS(it) }
        }
    }

    private fun cerrarCashlogySocketManager() {
        if (cashlogySocketManager != null) {
            cashlogySocketManager!!.stop() // Detener el socket de Cashlogy si está en uso
            cashlogyManager!!.closeWS()
        }
    }

    fun executeCaslogy(usarCashlogy: Boolean, urlCashlogy1: String?) {
        this.usarCashlogy = usarCashlogy
        this.urlCashlogy = urlCashlogy1
        if (cashlogySocketManager != null) {
            if (usarCashlogy) {
                iniciarCashlogySocketManager(urlCashlogy)
            } else {
                cerrarCashlogySocketManager()
            }
        }
    }


    // Saber si utiliza el cashlogy
    fun usaCashlogy(): Boolean {
        return usarCashlogy
    }

    fun cashLogyPayment(amount: Double, uiHandler: Handler): PaymentAction {
        return cashlogyManager!!.makePayment(amount, uiHandler)
    }

    fun cashlogyArqueo(cambio: Double, uiHandler: Handler): ArqueoAction {
        return cashlogyManager!!.makeArqueo(cambio, uiHandler)
    }


    fun cashLogyChange(uiHandler: Handler): ChangeAction {
        return cashlogyManager!!.makeChange(uiHandler)
    }


    inner class MyBinder : Binder() {
        val service: ServiceCOM
            get() = this@ServiceCOM
    }

}