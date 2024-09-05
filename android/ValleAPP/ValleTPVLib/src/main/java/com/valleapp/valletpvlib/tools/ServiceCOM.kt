package com.valleapp.valletpvlib.tools



import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.valleapp.valletpvlib.DBs.*
import com.valleapp.valletpvlib.interfaces.IBaseDatos
import com.valleapp.valletpvlib.interfaces.IBaseSocket
import com.valleapp.valletpvlib.tareas.TareaManejarInstrucciones
import com.valleapp.valletpvlib.tools.CashlogyManager.CashlogyManager
import com.valleapp.valletpvlib.tools.CashlogyManager.CashlogySocketManager
import com.valleapp.valletpvlib.tools.CashlogyManager.ChangeAction
import com.valleapp.valletpvlib.tools.CashlogyManager.PaymentAction
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URI
import java.nio.ByteBuffer
import java.util.*

class ServicioCom : Service() {



    private val myBinder = MyBinder()
    private var zn: JSONObject? = null
    private var mesaAbierta: JSONObject? = null

    var server: String? = null
    var urlCashlogy: String? = null
    var usarCashlogy = false
    private var cashlogySocketManager: CashlogySocketManager? = null
    private var cashlogyManager: CashlogyManager? = null

    private val timerUpdateLow = Timer()
    private val timerManejarInstrucciones = Timer()
    private val checkWebsocket = Timer()

    private val exHandler = HashMap<String, Handler>()
    private var dbs: MutableMap<String, IBaseDatos>? = null

    private val colaInstrucciones: Queue<Instrucciones> = LinkedList()
    private var tbNameUpdateLow: Array<String>? = null

    private var client: WebSocketClient? = null
    private var isWebsocketClose = false

    private val controllerHttp = Handler(Looper.getMainLooper()) { msg ->
        val op = msg.data.getString("op")
        val res = msg.data.getString("RESPONSE")
        if (res != null) {
            try {
                if (op == "update_socket") {
                    val objs = JSONArray(res)
                    for (i in 0 until objs.length()) {
                        updateTables(objs.getJSONObject(i))
                    }
                } else if (op == "camareros") {
                    val db = getDb("camareros") as DBCamareros
                    db.rellenarTabla(JSONArray(res))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        true
    }

    override fun onBind(intent: Intent?): IBinder {
        return myBinder
    }

    fun crearWebsocket() {
        try {
            client = object : WebSocketClient(URI("ws://${server!!.replace("api", "ws")}/comunicacion/devices")) {

                override fun onWebsocketPong(conn: WebSocket?, f: Framedata?) {
                    super.onWebsocketPong(conn, f)
                }

                override fun onOpen(serverHandshake: ServerHandshake) {
                    isWebsocketClose = false
                    Log.i("WEBSOCKET_INFO", "Websocket open.....")
                    syncDevice(arrayOf("mesasabiertas", "lineaspedido", "camareros"), 500)
                }

                override fun onMessage(message: String) {
                    try {
                        Log.e("WEBSOCKET_INFO", message)
                        val o = JSONObject(message)
                        updateTables(o)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onError(ex: Exception) {
                    Log.i("WEBSOCKET_INFO", "Error de conexion....")
                    isWebsocketClose = true
                }

                override fun onClose(code: Int, reason: String, remote: Boolean) {
                    Log.i("WEBSOCKET_INFO", "Websocket close....")
                    isWebsocketClose = true
                }

                override fun onMessage(bytes: ByteBuffer) {
                    Log.i("WEBSOCKET_INFO", "socket bytebuffer bytes")
                }
            }
            client!!.connect()
        } catch (ignored: Exception) {
        }
    }

    private fun syncDevice(tbs: Array<String>, timeout: Long) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                for (tb in tbs) {
                    val p = ContentValues()
                    val db = getDb(tb)
                    p.put("tb", tb)
                    p.put("reg", db!!.filter(null).toString())
                    HTTPRequest(
                        server + "/sync/sync_devices",
                        p,
                        "update_socket",
                        controllerHttp
                    )
                    try {
                        Thread.sleep(timeout)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }, 50)
    }

    private fun updateTables(o: JSONObject) {
        try {
            val tb = o.getString("tb")
            val op = o.getString("op")
            val db = getDb(tb) as IBaseSocket?
            db?.let {
                var objs = JSONArray()
                try {
                    val obj = o.getJSONObject("obj")
                    objs.put(obj)
                } catch (ignored: JSONException) {
                    objs = o.getJSONArray("obj")
                }

                for (i in 0 until objs.length()) {
                    val obj = objs.getJSONObject(i)
                    when (op) {
                        "insert" -> db.insert(obj)
                        "md" -> db.update(obj)
                        "rm" -> db.rm(obj)
                    }

                    val h = getExHandler(tb)
                    if (h != null) {
                        if (tb == "lineaspedido" && op != "rm" && mesaAbierta != null) {
                            val objIdMesa = o.getJSONObject("obj").getString("IDMesa")
                            val idMesaAbierta = mesaAbierta!!.getString("ID")
                            if (objIdMesa != idMesaAbierta) return
                        }
                        h.sendEmptyMessage(0)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_NOT_STICKY
        server = intent.getStringExtra("url")
        urlCashlogy = intent.getStringExtra("url_cashlogy")
        usarCashlogy = intent.getBooleanExtra("usar_cashlogy", false)

        server?.let {
            iniciarDB()
            crearWebsocket()

            if (usarCashlogy && urlCashlogy != null) {
                iniciarCashlogySocketManager(urlCashlogy!!)
            }

            timerUpdateLow.schedule(object : TimerTask() {
                override fun run() {
                    syncDevice(tbNameUpdateLow!!, 1000)
                }
            }, 1000, 290000)

            timerManejarInstrucciones.schedule(
                TareaManejarInstrucciones(colaInstrucciones, 1000), 2000, 1
            )

            checkWebsocket.schedule(object : TimerTask() {
                override fun run() {
                    if (isWebsocketClose && client != null) {
                        client!!.reconnect()
                    }
                }
            }, 2000, 5000)

            return START_STICKY
        }
        return START_NOT_STICKY
    }

    private fun TareaManejarInstrucciones(colaInstrucciones: Queue<Instrucciones>, l: Long): TareaManejarInstrucciones? {
       return null
    }

    private fun iniciarCashlogySocketManager(urlCashlogy: String) {
        cashlogySocketManager = CashlogySocketManager(urlCashlogy)
        cashlogySocketManager!!.start()
        cashlogyManager = CashlogyManager(cashlogySocketManager!!)
        cashlogyManager!!.initialize()
    }

    override fun onDestroy() {
        timerUpdateLow.cancel()
        timerManejarInstrucciones.cancel()
        checkWebsocket.cancel()

        cashlogySocketManager?.stop()
        super.onDestroy()
    }

    fun setExHandler(nombre: String, handler: Handler) {
        exHandler[nombre] = handler
    }

    fun getExHandler(nombre: String): Handler? {
        return exHandler[nombre]
    }

    private fun iniciarDB() {
        if (tbNameUpdateLow == null) {
            tbNameUpdateLow = arrayOf(
                "camareros", "zonas", "mesas", "teclas", "secciones", "subteclas"
            )
        }
        if (dbs == null) {
            dbs = HashMap()
            dbs!!["camareros"] = DBCamareros(applicationContext)
            dbs!!["mesas"] = DBMesas(applicationContext)
            dbs!!["zonas"] = DBZonas(applicationContext)
            dbs!!["secciones"] = DBSecciones(applicationContext)
            dbs!!["teclas"] = DBTeclas(applicationContext)
            dbs!!["lineaspedido"] = DBCuenta(applicationContext)
            dbs!!["mesasabiertas"] = DBMesasAbiertas(applicationContext)
            dbs!!["subteclas"] = DBSubTeclas(applicationContext)
            for (db in dbs!!.values) {
                db.inicializar()
            }
        }
    }

    fun getDb(nombre: String): IBaseDatos? {
        return dbs!![nombre]
    }

    fun abrirCajon() {
        if (!usarCashlogy) {
            server?.let {
                HTTPRequest(
                    it + "/impresion/abrircajon",
                    ContentValues(),
                    "abrir_cajon",
                    controllerHttp
                )
            }
        }
    }

    fun getLineasTicket(mostrarLsTicket: Handler, IDTicket: String) {
        val p = ContentValues()
        p.put("id", IDTicket)
        HTTPRequest(
            server + "/cuenta/lslineas",
            p,
            "get_lineas_ticket",
            mostrarLsTicket
        )
    }

    fun getListaTickets(hLsTicket: Handler) {
        HTTPRequest(
            server + "/cuenta/lsticket",
            ContentValues(),
            "get_lista_ticket",
            hLsTicket
        )
    }

    fun imprimirTicket(idTicket: String) {
        val p = ContentValues()
        p.put("id", idTicket)
        p.put("abrircajon", "False")
        p.put("receptor_activo", "True")
        HTTPRequest(
            server + "/impresion/imprimir_ticket",
            p,
            "",
            controllerHttp
        )
    }

    fun imprimirFactura(idTicket: String) {
        val p = ContentValues()
        p.put("id", idTicket)
        p.put("abrircajon", "False")
        p.put("receptor_activo", "True")
        HTTPRequest(
            server + "/impresion/imprimir_factura",
            p,
            "",
            controllerHttp
        )
    }

    fun getSettings(controller: Handler) {
        HTTPRequest(
            server + "/receptores/get_lista",
            ContentValues(),
            "get_lista_receptores",
            controller
        )
    }

    fun setSettings(lista: String) {
        val p = ContentValues()
        p.put("lista", lista)
        HTTPRequest(
            server + "/receptores/set_settings",
            p,
            "set_settings",
            controllerHttp
        )
    }

    fun rmMesa(params: ContentValues) {
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(params, server + "/cuenta/rm"))
        }
    }

    fun opMesas(params: ContentValues, op: String) {
        synchronized(colaInstrucciones) {
            val url = if (op == "juntarmesas") "/cuenta/juntarmesas" else "/cuenta/cambiarmesas"
            colaInstrucciones.add(Instrucciones(params, server + url))
        }
    }

    fun rmLinea(params: ContentValues) {
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(params, server + "/cuenta/rmlinea"))
        }
    }

    fun nuevoPedido(params: ContentValues) {
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(params, server + "/cuenta/add"))
        }
    }

    fun cobrarCuenta(params: ContentValues) {
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(params, server + "/cuenta/cobrar"))
        }
    }

    fun preImprimir(p: ContentValues) {
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(p, server + "/impresion/preimprimir"))
        }
    }

    fun getCuenta(controller: Handler, mesaId: String) {
        val p = ContentValues()
        p.put("mesa_id", mesaId)
        HTTPRequest(
            server + "/cuenta/get_cuenta",
            p,
            "",
            controller
        )
    }

    fun addCamNuevo(n: String, a: String) {
        val p = ContentValues()
        p.put("nombre", n)
        p.put("apellido", a)
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(p, server + "/camareros/camarero_add"))
        }
    }

    fun autorizarCam(obj: JSONObject) {
        val p = ContentValues()
        val o = JSONArray().put(obj)
        p.put("rows", o.toString())
        p.put("tb", "camareros")
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(p, server + "/sync/update_from_devices"))
        }
    }

    fun pedirAutorizacion(p: ContentValues) {
        HTTPRequest(
            server + "/autorizaciones/pedir_autorizacion",
            p,
            "",
            null
        )
    }

    fun sendMensaje(p: ContentValues) {
        HTTPRequest(
            server + "/autorizaciones/send_informacion",
            p,
            "",
            null
        )
    }

    fun getZona(): JSONObject? {
        return zn
    }

    fun setZona(zn: JSONObject) {
        this.zn = zn
    }

    fun setMesaAbierta(m: JSONObject) {
        this.mesaAbierta = m
    }

    /*
     Métodos para la integración del Cashlogy
     */

    fun setUiHandlerCashlogy(handler: Handler) {
        cashlogySocketManager?.setUiHandler(handler)
    }

    fun usaCashlogy(): Boolean {
        return usarCashlogy
    }

    fun cashLogyPayment(amount: Double, uiHandler: Handler): PaymentAction {
        return cashlogyManager!!.makePayment(amount, uiHandler)
    }

    fun cashLogyChange(uiHandler: Handler): ChangeAction {
        return cashlogyManager!!.makeChange(uiHandler)
    }

    fun get_cuenta(handlerHttp: Handler, ID: String) {

    }

    inner class MyBinder : Binder() {
        val service: ServicioCom
            get() = this@ServicioCom
    }
}
