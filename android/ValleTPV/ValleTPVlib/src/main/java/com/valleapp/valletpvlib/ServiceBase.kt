package com.valleapp.valletpvlib

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.interfaces.IControllerWS
import com.valleapp.valletpvlib.comunicacion.WSClient
import com.valleapp.valletpvlib.db.DBCamareros
import com.valleapp.valletpvlib.db.DBCuenta
import com.valleapp.valletpvlib.db.DBMesas
import com.valleapp.valletpvlib.db.DBMesasAbiertas
import com.valleapp.valletpvlib.db.DBSecciones
import com.valleapp.valletpvlib.db.DBTeclas
import com.valleapp.valletpvlib.db.DBZonas
import com.valleapp.valletpvlib.interfaces.IBaseDatos
import com.valleapp.valletpvlib.interfaces.IBaseSocket
import com.valleapp.valletpvlib.tareas.TareaManejarInstrucciones
import com.valleapp.valletpvlib.tools.Instruccion

import org.json.JSONArray
import org.json.JSONObject
import java.util.LinkedList
import java.util.Queue
import java.util.Timer
import java.util.TimerTask

abstract class ServiceBase : Service(), IControllerWS {

    private var isSyncScheduled: Boolean = false
    var zona: JSONObject? = null // Variable encapsulada para la zona
    private var mesaAbierta: JSONObject? = null

    protected var server: String? = null

    private var timerUpdateLow: Timer = Timer()

    private var exHandler: MutableMap<String, Handler> = HashMap()
    protected var dbs: MutableMap<String?, IBaseDatos>? = null

    private val colaInstrucciones: Queue<Instruccion> = LinkedList()
    protected var tbNameUpdateLow: Array<String>? = null

    private var wsClient: WSClient? = null
    private var manejadorInstrucciones: TareaManejarInstrucciones? = null

    private var uid: String? = null // Variable encapsulada para el UID

    protected val controllerHttp: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val op = msg.data.getString("op")
            val res = msg.data.getString("RESPONSE")
            if (res != null) {
                try {
                    if ("update_socket" == op) {
                        val objs = JSONArray(res)
                        for (i in 0 until objs.length()) {
                            procesarRespose(objs.getJSONObject(i))
                        }
                    } else {
                        if (op != null && op == "camareros") {
                            val db = getDb("camareros") as DBCamareros?
                            db!!.rellenarTabla(JSONArray(res))
                        }
                    }
                } catch (e: Exception) {
                Log.e("SERVICE_COM", "CONTROLLER_HTTP  $e")
                }
            }
        }
    }

    
    private fun syncDevice(tbs: Array<String>?) {
        try {
            if (tbs == null) {
                Log.w("SERVICE_COM", "El array de tablas es null, no se realiza ninguna sincronización")
                return
            }

            for (tb in tbs) {
                val p = ContentValues()
                val db = getDb(tb)
                p.put("tb", tb)
                if (tb == "lineaspedido") {
                    p.put("reg", db?.filter("Estado != 'N'")?.toString() ?: "[]")
                } else {
                    p.put("reg", db?.filter(null)?.toString() ?: "[]")
                }

                // Enviar directamente usando HTTPRequest
                val url = "$server/sync/sync_devices"
                p.put("uid", uid)
                HTTPRequest(url, p, "update_socket", controllerHttp)
                //Parar un segundo para evitar saturación del servidor
                Thread.sleep(1000) // Evitar saturación del servidor
            }
        } catch (e: Exception) {
            Log.e("SERVICE_COM", "Error en sync_device try principal: $e")
        }
    }


    override fun sincronizar() {
        syncDevice(arrayOf("camareros", "lineaspedido",  "mesas", "mesasabiertas", "sugerencias", "receptores", "zonas", "seccionescom", "teclas"))
    }

    override fun procesarRespose(o: JSONObject) {
        try {
            val tb = o.getString("tb")
            val op = o.getString("op")
            val db = getDb(tb) as IBaseSocket?
            if (db != null) {
                val objs: JSONArray

                when (val rawObj = o.opt("obj")) { // Safely get "obj" without exception
                    is JSONObject -> {
                        objs = JSONArray().apply { put(rawObj) }
                    }
                    is JSONArray -> {
                        objs = rawObj
                    }
                    else -> {
                        // Handle unexpected cases (null, string, number, etc.)
                        objs = JSONArray() // Or whatever default makes sense
                        Log.w("JSON_CHECK", "Unexpected type for 'obj': $rawObj")
                    }
                }


                // Procesar cada objeto en el JSONArray
                for (i in 0 until objs.length()) {
                    val obj = objs.getJSONObject(i)
                    when (op) {
                        "insert" -> db.insert(obj)
                        "md" -> db.update(obj)
                        "rm" -> db.rm(obj)
                    }
                }

                // Manejo del handler si es necesario
                val h = getExHandler(tb)
                if (h != null && objs.length() > 0) {
                    if (tb == "lineaspedido" && op != "rm" && mesaAbierta != null) {
                        val idMesaAbierta = mesaAbierta!!.getString("ID")
                        for (i in 0 until objs.length()) {
                            val objIdmesa = objs.getJSONObject(i).getString("IDMesa")
                            if (objIdmesa == idMesaAbierta) {
                                h.sendEmptyMessage(0)
                                break
                            }
                        }
                    } else {
                        h.sendEmptyMessage(0)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SERVICE_COM", "Error en procesarRespose: $e")
        }
    }

    override fun onCreate() {
        super.onCreate()
        val uidActual = uid ?: ""
        manejadorInstrucciones = TareaManejarInstrucciones(colaInstrucciones, uidActual) // Pass contex
        iniciarDB()
        Log.d("ServiceComBase", "Service created")
    }

    abstract fun startForegroundService()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        if (intent == null) return START_NOT_STICKY
        val url = intent.getStringExtra("url")
        if (url != null) {
            server = url
            iniciarWebsocket()
            startForegroundService()
            //programarSincronizacion()
            Log.d("ServiceComBase", "Service started with URL: $url")
            return START_STICKY
        }
        Log.e("ServiceComBase", "URL is null in onStartCommand")
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        wsClient?.stopReconnection()
        timerUpdateLow.cancel()
        wsClient = null // Permite reconexión si el servicio se reinicia
        isSyncScheduled = false
        Log.d("ServiceComBase", "Servicio destruido")
    }

    abstract override fun onBind(intent: Intent): IBinder

    private fun iniciarWebsocket() {
        if (wsClient == null && server != null) {
            val uidParam = if (uid != null) "?uid=$uid" else ""
            val path = "/comunicacion/devices$uidParam"
            Log.d("ServiceComBase", "Creating websocket: $server$path")
            wsClient = WSClient(server!!, path, this)
            wsClient?.connect()
        }
    }

    private fun programarSincronizacion() {
        if (!isSyncScheduled) {
            timerUpdateLow.schedule(object : TimerTask() {
                override fun run() {
                    syncDevice(tbNameUpdateLow) // Asumo tbNameUpdateLow está definido
                }
            }, 1000, 180000)
            isSyncScheduled = true
            Log.d("ServiceComBase", "Sincronización programada")
        } else {
            Log.d("ServiceComBase", "Sincronización ya programada, ignorada")
        }
    }


    fun setExHandler(nombre: String, handler: Handler) {
        exHandler[nombre] = handler
    }

    fun getExHandler(nombre: String): Handler? {
        try {
            return exHandler[nombre]
        } catch (e: NullPointerException) {
            Log.e("SERVICE_COM", e.toString())
        }

        return null
    }

    open fun iniciarDB() {
        if (tbNameUpdateLow == null) {
            tbNameUpdateLow = arrayOf(
                "camareros",
                "lineaspedido",
                "mesasabiertas",
                "mesas",
                "zonas",
                "seccionescom",
                "teclas",
            )
        }
        if (dbs == null) {
            val dbMesas = DBMesas(applicationContext)
            dbs = HashMap()
            (dbs as HashMap<String?, IBaseDatos>)["camareros"] = DBCamareros(applicationContext)
            (dbs as HashMap<String?, IBaseDatos>)["mesas"] = dbMesas
            (dbs as HashMap<String?, IBaseDatos>)["zonas"] = DBZonas(applicationContext)
            (dbs as HashMap<String?, IBaseDatos>)["seccionescom"] = DBSecciones(applicationContext)
            (dbs as HashMap<String?, IBaseDatos>)["teclas"] = DBTeclas(applicationContext)
            (dbs as HashMap<String?, IBaseDatos>)["lineaspedido"] = DBCuenta(applicationContext)
            (dbs as HashMap<String?, IBaseDatos>)["mesasabiertas"] = DBMesasAbiertas(applicationContext)
            for (db in (dbs as HashMap<String?, IBaseDatos>).values) {
                db.inicializar()
            }
        }
    }

    fun getDb(nombre: String?): IBaseDatos? {
        return dbs!![nombre]
    }


    fun opMesas(params: ContentValues?, op: String) {
        val url = if (op == "juntarmesas") "/cuenta/juntarmesas" else "/cuenta/cambiarmesas"
        agregarInstruccion(params?.let { Instruccion(it, server + url) })
    }


    fun nuevoPedido(params: ContentValues?) {
        agregarInstruccion(params?.let { Instruccion(it, "$server/cuenta/add") })
    }


    fun sendMensaje(params: ContentValues?) {
        agregarInstruccion(params?.let { Instruccion(it, "$server/autorizaciones/send_informacion") })
    }

    fun mesaAbierta(m: JSONObject?) {
        this.mesaAbierta = m
    }

    // 📌 Llamar a este método cada vez que se agregue una nueva instrucción
    fun agregarInstruccion(nuevaInstruccion: Instruccion?) {
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(nuevaInstruccion)
        }
        manejadorInstrucciones?.iniciar() // 📌 Se asegura de que se procese inmediatamente
    }

    fun syncSingleTable(tableName: String) {
        try {
            val p = ContentValues()
            val db = getDb(tableName)
            p.put("tb", tableName)
            if (tableName == "lineaspedido") {
                p.put("reg", db?.filter("Estado != 'N'")?.toString() ?: "[]")
            } else {
                p.put("reg", db?.filter(null)?.toString() ?: "[]")
            }

            // Enviar directamente usando HTTPRequest
            val url = "$server/sync/sync_devices"
            p.put("uid", uid)
            HTTPRequest(url, p, "update_socket", controllerHttp)
        } catch (e: Exception) {
            Log.e("SERVICE_COM", "Error en syncSingleTable: $e")
        }
    }


    // Método para obtener el número de artículos en la cola
    protected fun obtenerNumeroDeArticulosEnCola(): Int {
        return colaInstrucciones.size
    }

    // Método para verificar si el WebSocket está conectado
    protected fun verificarEstadoWebSocket(): Boolean {
        return wsClient?.isOpen == true
    }

    fun setUid(uid: String?) {
        manejadorInstrucciones?.setUid(uid)
        val uidCambio = this.uid != uid
        this.uid = uid

        // Si el UID cambió y el WebSocket ya estaba conectado, reconectar con el nuevo UID
        if (uidCambio && wsClient != null) {
            Log.d("ServiceComBase", "UID cambió, reconectando WebSocket con nuevo UID: $uid")
            wsClient?.stopReconnection()
            wsClient = null
            iniciarWebsocket()
        }
    }

    fun getUid(): String? {
        return uid
    }

    fun setServerUrl(server: String?) {
        this.server = server
    }

    fun getServerUrl(): String? {
        return server
    }

}