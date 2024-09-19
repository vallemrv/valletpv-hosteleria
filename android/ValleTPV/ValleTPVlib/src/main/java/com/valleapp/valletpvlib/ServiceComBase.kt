package com.valleapp.valletpvlib

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import com.valleapp.valletpvlib.Interfaces.IBaseDatos
import com.valleapp.valletpvlib.Interfaces.IBaseSocket
import com.valleapp.valletpvlib.Interfaces.IControllerWS
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.comunicacion.WSClient
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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.LinkedList
import java.util.Queue
import java.util.Timer
import java.util.TimerTask

abstract class ServicioCom : Service(), IControllerWS {

    var zona: JSONObject? = null
    private var mesa_abierta: JSONObject? = null

    var server: String? = null


    var timerUpdateLow: Timer = Timer()
    var timerManejarInstrucciones: Timer = Timer()

    var exHandler: MutableMap<String, Handler> = HashMap()
    var dbs: MutableMap<String?, IBaseDatos>? = null

    val colaInstrucciones: Queue<Instrucciones> = LinkedList()
    var tbNameUpdateLow: Array<String>? = null

    var wsClient: WSClient? = null


    protected val controller_http: Handler = object : Handler(Looper.getMainLooper()) {
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
                    Log.e("SERVICE_COM", e.toString())
                }
            }
        }
    }


    private fun sync_device(tbs: Array<String>?, timeout: Long) {
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
                            controller_http
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
            Log.e("SERVICE_COM", e.toString())
        }
    }

    override fun sincronizar() {
        sync_device(arrayOf("camareros", "mesasabiertas", "lineaspedido"), 500)
    }

    override fun procesarRespose(o: JSONObject) {
        try {
            val tb = o.getString("tb")
            val op = o.getString("op")
            val db = getDb(tb) as IBaseSocket?
            if (db != null) {
                var objs = JSONArray()
                try {
                    val obj = o.getJSONObject("obj")
                    objs.put(obj)
                } catch (ignored: JSONException) {
                    objs = o.getJSONArray("obj")
                }

                for (i in 0 until objs.length()) {
                    val obj = objs.getJSONObject(i)
                    if (op == "insert") db.insert(obj)
                    if (op == "md") db.update(obj)
                    if (op == "rm") db.rm(obj)
                }

                val h = getExHandler(tb)
                if (h != null) {
                    if (tb == "lineaspedido" && op != "rm" && mesa_abierta != null) {
                        val obj_idmesa = o.getJSONObject("obj").getString("IDMesa")
                        val id_mesa_abierta = mesa_abierta!!.getString("ID")
                        if (obj_idmesa != id_mesa_abierta) return
                    }
                    h.sendEmptyMessage(0)
                }
            }
        } catch (e: Exception) {
            Log.e("SERVICE_COM", "Error en procesarRespose: $e")
        }
    }

    abstract fun startForegroundService()



    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        // Recoger parámetros del Intent
        val url = intent.getStringExtra("url")

        if (url != null) {
            server = url
            IniciarDB()

            if (wsClient == null) {
                //crearWebsocket();
                Log.d("SERVICE_COM", "Creando websocket: $server")
                wsClient = WSClient(server!!, "/comunicacion/devices", this)
                wsClient!!.connect()
            }


            // Programar la sincronización periódica
            timerUpdateLow.schedule(object : TimerTask() {
                override fun run() {
                    sync_device(tbNameUpdateLow, 1000)
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

    private fun IniciarDB() {
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

    fun setMesa_abierta(m: JSONObject?) {
        this.mesa_abierta = m
    }



}