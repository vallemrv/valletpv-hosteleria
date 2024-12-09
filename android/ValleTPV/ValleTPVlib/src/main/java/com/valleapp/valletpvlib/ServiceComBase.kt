package com.valleapp.valletpvlib

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import com.valleapp.valletpvlib.interfaces.IBaseDatos
import com.valleapp.valletpvlib.interfaces.IBaseSocket
import com.valleapp.valletpvlib.interfaces.IControllerWS
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.comunicacion.WSClient
import com.valleapp.valletpvlib.db.AppDatabase
import com.valleapp.valletpvlib.db.DBCamareros
import com.valleapp.valletpvlib.db.DBCuenta
import com.valleapp.valletpvlib.db.DBMesas
import com.valleapp.valletpvlib.db.DBMesasAbiertas
import com.valleapp.valletpvlib.db.DBSecciones
import com.valleapp.valletpvlib.db.DBSubTeclas
import com.valleapp.valletpvlib.db.DBTeclas
import com.valleapp.valletpvlib.db.DBZonas
import com.valleapp.valletpvlib.tareas.TareaManejarInstrucciones
import com.valleapp.valletpvlib.tools.Instrucciones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.LinkedList
import java.util.Queue
import java.util.Timer
import java.util.TimerTask

abstract class ServiceComBase : Service(), IControllerWS {

    private lateinit var db: AppDatabase // Instancia de AppDatabase
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob()) // Corrutinas para Room


    var zona: JSONObject? = null
    private var mesaAbierta: JSONObject? = null

    var server: String? = null

    private var timerUpdateLow: Timer = Timer()
    private var timerManejarInstrucciones: Timer = Timer()

    private var exHandler: MutableMap<String, Handler> = HashMap()
    private var dbs: MutableMap<String?, IBaseDatos>? = null

    val colaInstrucciones: Queue<Instrucciones> = LinkedList()
    var tbNameUpdateLow: Array<String>? = null

    private var wsClient: WSClient? = null


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

    private fun syncDevice(tbs: Array<String>?, timeout: Long) {
        try {
            val t = Timer()
            t.schedule(object : TimerTask() {
                override fun run() {
                    for (tb in tbs!!) {
                        val p = ContentValues()
                        val db = getDb(tb)
                        p.put("tb", tb)
                        p.put("reg", db!!.filter(null).toString())
                        HTTPRequest(
                            "$server/sync/sync_devices",
                            p,
                            "update_socket",
                            controllerHttp
                        )
                        try {
                            Thread.sleep(timeout)
                        } catch (e: InterruptedException) {
                            Log.e("SERVICE_COM", "Error en sync_device: $e")
                        }
                    }
                }
            }, 50)
        } catch (e: Exception) {
            Log.e("SERVICE_COM", "Error en sync_device try pincipal: $e")
        }
    }

    override fun sincronizar() {
        syncDevice(arrayOf("camareros", "mesasabiertas", "lineaspedido"), 500)
    }

    override fun procesarRespose(o: JSONObject) {
        scope.launch { // Lanzamos una corrutina para operaciones de la base de datos
            try {
                val tb = o.getString("tb")
                val op = o.getString("op")

                // Obtener el DAO correspondiente
                val dao = when (tb) {
                    "camareros" -> db.camareroDao()
                    "mesas" -> db.mesaDao()
                    "cuentas" -> db.cuentaDao()
                    "secciones" -> db.seccionDao()
                    "subteclas" -> db.subTeclaDao()
                    "teclas" -> db.teclaDao()
                    "zonas" -> db.zonaDao()
                    else -> throw IllegalArgumentException("Tabla no reconocida: $tb")
                }

                var objs: JSONArray

                // Verificar si "obj" es un JSONArray o un JSONObject
                try {
                    val obj = o.getJSONObject("obj")
                    objs = JSONArray()
                    objs.put(obj)
                } catch (ignored: JSONException) {
                    // Si es un JSONArray
                    objs = o.getJSONArray("obj")
                }

                for (i in 0 until objs.length()) {
                    val obj = objs.getJSONObject(i)
                    when (op) {
                        "insert" -> dao.insert(entityFromJsonObject(obj, tb))
                        "md" -> dao.update(entityFromJsonObject(obj, tb))
                        "rm" -> dao.delete(entityFromJsonObject(obj, tb))
                    }
                }

                // ... (manejo del handler) ...

            } catch (e: Exception) {
                Log.e("SERVICE_COM", "Error en procesarRespose: $e")
            }
        }
    }

    abstract fun startForegroundService()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        // Recoger parámetros del Intent
        val url = intent.getStringExtra("url")

        if (url != null) {
            server = url
            db = AppDatabase.getDatabase(applicationContext) // Inicializar AppDatabase


            if (wsClient == null) {
                //crearWebsocket();
                Log.d("SERVICE_COM", "Creando websocket: $server")
                wsClient = WSClient(server!!, "/comunicacion/devices", this)
                wsClient!!.connect()
            }


            // Programar la sincronización periódica
            timerUpdateLow.schedule(object : TimerTask() {
                override fun run() {
                    syncDevice(tbNameUpdateLow, 1000)
                }
            }, 1000, 290000)

            // Programar el manejo de instrucciones
            timerManejarInstrucciones.schedule(
                TareaManejarInstrucciones(colaInstrucciones, 1000), 2000, 1
            )

            startForegroundService()
            return START_STICKY
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timerUpdateLow.cancel()
        timerManejarInstrucciones.cancel()
        if (wsClient != null) {
            wsClient!!.stopReconnection()
        }
        super.onDestroy()
    }

    abstract override fun onBind(intent: Intent): IBinder


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

    private fun iniciarDB() {
        if (tbNameUpdateLow == null) {
            tbNameUpdateLow = arrayOf(
                "camareros",
                "zonas",
                "mesas",
                "teclas",
                "secciones",
                "subteclas"
            )
        }
        if (dbs == null) {
            val dbMesas = DBMesas(applicationContext)
            dbs = HashMap()
            (dbs as HashMap<String?, IBaseDatos>)["camareros"] = DBCamareros(applicationContext)
            (dbs as HashMap<String?, IBaseDatos>)["mesas"] = dbMesas
            (dbs as HashMap<String?, IBaseDatos>)["zonas"] = DBZonas(applicationContext)
            (dbs as HashMap<String?, IBaseDatos>)["secciones"] = DBSecciones(applicationContext)
            (dbs as HashMap<String?, IBaseDatos>)["teclas"] = DBTeclas(applicationContext)
            (dbs as HashMap<String?, IBaseDatos>)["lineaspedido"] = DBCuenta(applicationContext)
            (dbs as HashMap<String?, IBaseDatos>)["mesasabiertas"] = DBMesasAbiertas(applicationContext)
            (dbs as HashMap<String?, IBaseDatos>)["subteclas"] = DBSubTeclas(applicationContext)
            for (db in (dbs as HashMap<String?, IBaseDatos>).values) {
                db.inicializar()
            }
        }
    }

    fun getDb(nombre: String?): IBaseDatos? {
        return dbs!![nombre]
    }


    fun opMesas(params: ContentValues?, op: String) {
        synchronized(colaInstrucciones) {
            val url = if (op == "juntarmesas") "/cuenta/juntarmesas" else "/cuenta/cambiarmesas"
            colaInstrucciones.add(Instrucciones(params, server + url))
        }
    }


    fun nuevoPedido(params: ContentValues?) {
        synchronized(colaInstrucciones) {
            colaInstrucciones.add(Instrucciones(params, "$server/cuenta/add"))
        }
    }


    fun sendMensaje(p: ContentValues?) {
        HTTPRequest(
            "$server/autorizaciones/send_informacion",
            p, "", null
        )
    }

    fun mesaAbierta(m: JSONObject?) {
        this.mesaAbierta = m
    }

}