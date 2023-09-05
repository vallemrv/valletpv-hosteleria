package com.valleapp.valletpvlib.tools

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.valleapp.valletpvlib.db.AppDatabase
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.IBaseDao
import com.valleapp.valletpvlib.db.IBaseEntity
import com.valleapp.valletpvlib.db.LineaPedido
import com.valleapp.valletpvlib.db.Mesa
import com.valleapp.valletpvlib.db.Seccion
import com.valleapp.valletpvlib.db.Tecla
import com.valleapp.valletpvlib.db.Zona
import com.valleapp.valletpvlib.interfaces.IController
import com.valleapp.valletpvlib.interfaces.IServiceState
import com.valleapp.valletpvlib.tools.tareas.InstruccionesManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ServiceCom : Service(), IController {

    private var validador: IServiceState? = null
    private val binder = LocalBinder()
    private var serverConfig: ServerConfig? = null
    private var appDatabase: AppDatabase? = null
    private val procesarCola = InstruccionesManager()
    private var wsClient: WSClient? = null

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        appDatabase = AppDatabase.getDatabase(this)
        intent?.let {
            val resId = it.getIntExtra("res_id", 0)
            val chanelId = it.getStringExtra("chanel_id")
            val titulo = it.getStringExtra("titulo")
            val texto = it.getStringExtra("texto")


            if (chanelId != null && texto != null && titulo != null && resId != 0) {
                createNotificationChannel(chanelId, texto)
                val notification = NotificationCompat.Builder(this, chanelId)
                    .setContentTitle(titulo)
                    .setContentText(texto)
                    .setSmallIcon(resId)
                    .build()

                startForeground(1, notification)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        procesarCola.stopProcesarCola()
        wsClient?.salir()
    }

    private fun createNotificationChannel(id: String, name: String) {
        val serviceChannel = NotificationChannel(
            id,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }


    override fun updateTables(o: JSONObject?) {
        if (o != null) {
            val tbName = o.getString("tb")
            val tb = getDB(tbName)
            if (tb == null) {
                println("Tabla no encontrada: $tbName")
                return
            }
            handleSync(tb, o, tbName)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun syncDevice(lista: List<String>) {
        GlobalScope.launch {
            val gson = Gson()

            for (item in lista) {
                val tb = getDB(item)
                if (tb == null) {
                    println("Tabla no encontrada: $item")
                    continue
                }
                val datos = tb.getAll()

                // Convertir datos a JSON
                val jsonData = gson.toJson(datos)

                // Construir el objeto que quieres enviar al servidor
                val dataToSend = mapOf(
                    "tb_name" to item,
                    "regs" to jsonData
                )

                val result = safeApiCall {
                    ApiRequest.service.post(
                        ApiEndPoints.SYNC_DEVICES,
                        serverConfig?.getParams(dataToSend)
                    )
                }
                when (result) {
                    is ApiResponse.Success -> {
                        val obj = result.data["sync"]?.let { JSONObject(it) }
                        if (obj != null) {
                            handleSync(tb, obj, item)
                        }
                    }

                    is ApiResponse.Error -> {
                        if (result.errorMessage == ApiErrorMessages.UNAUTHORIZED) {
                            validador?.invalidateAuth()
                            break
                        } else {
                            println("Error: ${result.errorMessage}")
                        }

                    }

                }
            }
        }
    }

    private fun handleSync(tb: IBaseDao<*>, obj: JSONObject, tbName: String) {
        if (obj.has("delete")) {
            procesarDelete(obj.getJSONArray("delete"), tb)
        }
        if (obj.has("update")) {
            procesarUpdate(obj.getJSONArray("update"), tb, tbName)
        }

        if (obj.has("create")) {
            procesarCreate(obj.getJSONArray("create"), tb, tbName)
        }
    }

    private fun procesarCreate(insert: JSONArray, tb: IBaseDao<*>, tbName: String) {
        for (i in 0 until insert.length()) {
            val reg = insert.getJSONObject(i)
            val entity = getEntity(tbName)
            entity.executeAccion(reg, tb, "INS")
        }
    }

    private fun procesarUpdate(up: JSONArray, tb: IBaseDao<*>, tbName: String) {
        for (i in 0 until up.length()) {
            val reg = up.getJSONObject(i)
            val entity = getEntity(tbName)
            entity.executeAccion(reg, tb, "UP")
        }
    }

    private fun procesarDelete(delete: JSONArray, tb: IBaseDao<*>) {
        for (i in 0 until delete.length()) {
            val id = delete.getLong(i)
            tb.deleteById(id)
        }
    }


    private fun getEntity(name: String): IBaseEntity {
        return when (name) {
            "camareros" -> Camarero()
            "mesas" -> Mesa()
            "zonas" -> Zona()
            "teclas" -> Tecla()
            "secciones" -> Seccion()
            "lineaspedido" -> LineaPedido()
            else -> throw IllegalArgumentException("Entity no encontrado para: $name")
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): ServiceCom = this@ServiceCom
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun runSync(serverConfig: ServerConfig, v: IServiceState) {
        if (this.serverConfig?.uid != serverConfig.uid) {
            this.serverConfig = serverConfig
            ApiRequest.init(serverConfig.getParseUrl())
            val controller = this
            GlobalScope.launch {
                val wsUrl = serverConfig.getWSUrl()
                procesarCola.stopProcesarCola()
                wsClient?.salir()

                wsClient = WSClient(wsUrl, "comunicacion/devices", controller)
                wsClient?.connect()

                validador = v
                procesarCola.procesarCola(v)
            }
            println("Arrancando Procesos de cola y WSClient")
        }
    }


    fun getDB(tbName: String): IBaseDao<*>? {
        return when (tbName) {
            "camareros" -> appDatabase?.camareroDao() as IBaseDao<*>
            "mesas" -> appDatabase?.mesasDao() as IBaseDao<*>
            "zonas" -> appDatabase?.zonasDao() as IBaseDao<*>
            "teclas" -> appDatabase?.teclasDao() as IBaseDao<*>
            "secciones" -> appDatabase?.seccionesDao() as IBaseDao<*>
            "lineaspedido" -> appDatabase?.lineasDao() as IBaseDao<*>
            else -> null
        }
    }

    fun addInstruccion(inst: Instrucciones) {
        procesarCola.addInstruccion(inst)
    }

    suspend fun abrirCajon() {
        safeApiCall {
            ApiRequest.service.post(
                ApiEndPoints.ABRIR_CAJON,
                serverConfig?.getParams()
            )
        }
    }

    suspend fun preImprimir(mesaId: Long) {
        safeApiCall {
            ApiRequest.service.post(
                ApiEndPoints.PRE_IMPRIMIR,
                serverConfig?.getParams(mapOf("idm" to mesaId))
            )
        }
    }

    suspend fun getListaTicket(offset: Int): List<Ticket>? {
        val result = safeApiCall {
            ApiRequest.service.post(
                ApiEndPoints.GET_LISTA_TICKET,
                serverConfig?.getParams(mapOf("offset" to offset))
            )
        }

        return when (result) {
            is ApiResponse.Success -> {
                val lineasJson = result.data["lineas"] ?: return null
                val type = object : TypeToken<List<Ticket>>() {}.type
                val tickets: List<Ticket> = Gson().fromJson(lineasJson, type)
                tickets
            }

            is ApiResponse.Error -> {
                if (result.errorMessage == ApiErrorMessages.UNAUTHORIZED) {
                    validador?.invalidateAuth()
                    null
                } else {
                    println("Error: ${result.errorMessage}")
                    null
                }
            }
        }
    }

    suspend fun getLineasTicket(id: Long): List<LineasTicket>? {
        val result = safeApiCall {
            ApiRequest.service.post(
                ApiEndPoints.GET_LINEAS_TICKET,
                serverConfig?.getParams(mapOf("id" to id.toString()))
            )
        }
        return when (result) {
            is ApiResponse.Success -> {
                val lineasJson = result.data["lineas"] ?: return null
                val type = object : TypeToken<List<LineasTicket>>() {}.type
                val lineas: List<LineasTicket> = Gson().fromJson(lineasJson, type)
                lineas
            }

            is ApiResponse.Error -> {
                if (result.errorMessage == ApiErrorMessages.UNAUTHORIZED) {
                    validador?.invalidateAuth()
                    null
                } else {
                    println("Error: ${result.errorMessage}")
                    null
                }
            }
        }
    }

    suspend fun imprimirTicket(id: Long) {
        safeApiCall {
            ApiRequest.service.post(
                ApiEndPoints.IMPRIMIR_TICKET,
                serverConfig?.getParams(
                    mapOf(
                        "id" to id,
                        "receptor_activo" to true,
                        "abrircajon" to false
                    )
                )
            )
        }
    }

    suspend fun imprimirFactura(id: Long) {
        safeApiCall {
            ApiRequest.service.post(
                ApiEndPoints.IMPRIMIR_FACTURA,
                serverConfig?.getParams(
                    mapOf(
                        "id" to id,
                        "receptor_activo" to true,
                        "abrircajon" to false
                    )
                )
            )
        }
    }

}
