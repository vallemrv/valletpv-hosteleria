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
import com.valleapp.valletpv.ValleTPV
import com.valleapp.valletpvlib.cashlogymanager.ArqueoAction
import com.valleapp.valletpvlib.cashlogymanager.CashlogyManager
import com.valleapp.valletpvlib.cashlogymanager.CashlogySocketManager
import com.valleapp.valletpvlib.cashlogymanager.ChangeAction
import com.valleapp.valletpvlib.cashlogymanager.PaymentAction
import com.valleapp.valletpvlib.ServiceBase
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.tools.Instruccion
import org.json.JSONArray
import org.json.JSONObject

class ServiceTPV: ServiceBase() {

    private val myBinder: IBinder = MyBinder()

    private val channelId = "ValleTPV"

    private var urlCashlogy: String? = null
    private var usarCashlogy: Boolean = false
    private var cashlogySocketManager: CashlogySocketManager? = null
    private var cashlogyManager: CashlogyManager? = null
    private var usarTPV: Boolean = false
    private var ipTPV: String? = null

    // Método para compatibilidad con código existente
    fun getUID(): String? {
        return getUid()
    }

    override fun startForegroundService() {

        // Verifica si el canal ya existe
        val channel = NotificationChannel(
            channelId,
            "ValleTPV service",
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
            .setSmallIcon(com.valleapp.valletpvlib.R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationManager.IMPORTANCE_LOW)  // Ajusta según sea necesario
            .build()

        // Iniciar el servicio en primer plano con la notificación
        startForeground(1, notification)
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val out = super.onStartCommand(intent, flags, startId)

        urlCashlogy = intent?.getStringExtra("url_cashlogy") // Recoger URL de Cashlogy
        usarCashlogy =
            intent?.getBooleanExtra("usar_cashlogy", false) == true // Recoger estado del CheckBox
        usarTPV =
            intent?.getBooleanExtra("usar_tpvpc", false) == true // Recoger estado del CheckBox
        ipTPV = intent?.getStringExtra("ip_tpvpc") // Recoger IP del servidor TPVPC
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
                ContentValues(), "abrir_cajon", controllerHttp
            )
        }
    }

    fun getLineasTicket(mostrarLsTicket: Handler?, idTicket: String?) {
        val p = ContentValues()
        p.put("id", idTicket)
        p.put("uid", getUid())
        HTTPRequest(
            "$server/cuenta/lslineas", p,
            "get_lineas_ticket", mostrarLsTicket
        )
    }

    fun getListaTickets(hLsTicket: Handler?) {
            val p = ContentValues()
            p.put("uid", getUid())
        HTTPRequest(
            "$server/cuenta/lsticket", p,
            "get_lista_ticket", hLsTicket
        )
    }

    fun imprimirTicket(idTicket: String?) {
        val p = ContentValues()
        p.put("id", idTicket)
        p.put("abrircajon", "False")
        p.put("receptor_activo", "True")
        HTTPRequest("$server/impresion/imprimir_ticket", p, "", controllerHttp)
    }

    fun imprimirFactura(idTicket: String?) {
        val p = ContentValues()
        p.put("id", idTicket)
        p.put("abrircajon", "False")
        p.put("receptor_activo", "True")
        p.put("uid", getUid())
        HTTPRequest("$server/impresion/imprimir_factura", p, "", controllerHttp)
    }

    fun getSettings(controller: Handler?) {
        val p = ContentValues()
        p.put("uid", getUid())
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
            "set_settings", controllerHttp
        )
    }

    fun rmMesa(params: ContentValues?) {
        agregarInstruccion(params?.let { Instruccion(it, "$server/cuenta/rm") })
    }


    fun rmLinea(params: ContentValues?) {
        agregarInstruccion(params?.let { Instruccion(it, "$server/cuenta/rmlinea") })
    }

    fun cobrarCuenta(params: ContentValues?) {
        agregarInstruccion(params?.let { Instruccion(it, "$server/cuenta/cobrar") })
    }

    fun preImprimir(p: ContentValues?) {
        agregarInstruccion(p?.let { Instruccion(it, "$server/impresion/preimprimir") })
    }


    fun getCuenta(controller: Handler?, p: ContentValues) {
        HTTPRequest("$server/cuenta/get_cuenta", p, "get_cuenta", controller)
    }

    fun addCamNuevo(n: String?, a: String?) {
        val p = ContentValues()
        p.put("nombre", n)
        p.put("apellido", a)
        p.put("uid", getUid())
        HTTPRequest("$server/camareros/camarero_add", p, "add_camarero", controllerHttp) // Usando HTTPRequest directamente
    }

    fun autorizarCam(obj: JSONObject?) {
        val p = ContentValues()
        val o = JSONArray()
        o.put(obj)
        p.put("rows", o.toString())
        p.put("tb", "camareros")
        p.put("uid", getUid())

        HTTPRequest("$server/sync/update_from_devices", p, "autorizar_camarero", controllerHttp) // Usando HTTPRequest directamente
    }

    fun pedirAutorizacion(p: ContentValues) {
        HTTPRequest(
            "$server/autorizaciones/pedir_autorizacion",
            p, "", null
        )
    }


    /*
    Metodos para la integracion de TPVPC
     */
    fun usaTPV(): Boolean {
        return usarTPV
    }

    fun getIPTPV(): String? {
        return ipTPV
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
        val service: ServiceTPV
            get() = this@ServiceTPV
    }

}