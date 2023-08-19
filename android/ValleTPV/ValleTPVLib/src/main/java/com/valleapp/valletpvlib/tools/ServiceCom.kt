package com.valleapp.valletpvlib.tools

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.valleapp.valletpvlib.db.AppDatabase
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.IBaseDao
import com.valleapp.valletpvlib.db.IBaseEntity
import com.valleapp.valletpvlib.tools.tareas.InstruccionesManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class ServiceCom : Service() {

    private val binder = LocalBinder()
    private var serverConfig: ServerConfig? = null
    private var appDatabase: AppDatabase? = null
    private val procesarCola = InstruccionesManager()

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

    @OptIn(DelicateCoroutinesApi::class)
    private fun syncDevice(lista: List<String>) {
        GlobalScope.launch {
            val gson = Gson()

            for (item in lista) {
                val tb = getDB(item)
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
                        println("Error: ${result.errorMessage}")
                    }

                }
            }
        }
    }

    private fun handleSync(tb: IBaseDao<*>, obj: JSONObject, tb_name: String) {
        println(obj.toString())
        val up = obj?.getJSONArray("update")

        if (up != null) {
            for (i in 0 until up.length()) {
                val reg = up.getJSONObject(i)
                val entity =  getEntity(tb_name)
                entity.executeAccion(reg, tb, "UP")
            }
        }
        val insert = obj?.getJSONArray("create")
        if (insert != null) {
            for (i in 0 until insert.length()) {
                val reg = insert.getJSONObject(i)
                val entity =  getEntity(tb_name)
                entity.executeAccion(reg, tb, "INS")
            }
        }
        val delete = obj?.getJSONArray("delete")
        if (delete != null) {
            for (i in 0 until delete.length()) {
                val id = delete.getLong(i)
                tb.deleteById(id)
            }
        }

    }



    private fun getEntity(name: String): IBaseEntity {
        when (name) {
            "camareros" -> return Camarero()
            else -> throw IllegalArgumentException("Entity no encontrado para: $name")
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): ServiceCom = this@ServiceCom
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun setServerConfig(serverConfig: ServerConfig) {
        if (this.serverConfig == null) {
            this.serverConfig = serverConfig
            ApiRequest.init(serverConfig.getParseUrl())
            syncDevice(listOf("camareros"))
            GlobalScope.launch {
                procesarCola.procesarCola()
            }
            println("ServiceCom: setServerConfig")
        }
    }


    fun getDB(tb_name: String): IBaseDao<*> {
        when (tb_name) {
            "camareros" -> return appDatabase?.camareroDao() as IBaseDao<*>
            else -> throw IllegalArgumentException("DAO no encontrado para: $tb_name")
        }
    }

    fun addInstruccion(inst: Instrucciones) {
        procesarCola.addInstruccion(inst)
        println("ServiceCom: addInstruccion")
    }
}
