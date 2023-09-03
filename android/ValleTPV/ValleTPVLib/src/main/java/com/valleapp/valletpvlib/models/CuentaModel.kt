package com.valleapp.valletpvlib.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.CamareroDao
import com.valleapp.valletpvlib.db.LineaPedido
import com.valleapp.valletpvlib.db.LineasDao
import com.valleapp.valletpvlib.db.Mesa
import com.valleapp.valletpvlib.db.MesasDao
import com.valleapp.valletpvlib.tools.ApiEndPoints
import com.valleapp.valletpvlib.tools.Instrucciones
import com.valleapp.valletpvlib.tools.ServerConfig
import com.valleapp.valletpvlib.tools.ServiceCom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class CuentaModel(private val camId: Long, private val mesaId: Long) : ViewModel() {


    private var serviceCom: ServiceCom? by mutableStateOf(null)
    private var mesasDao: MesasDao? = null
    private var camarerosDao: CamareroDao? = null
    private var serverConfig: ServerConfig? = null

    val total = MutableStateFlow(0.0)

    var cantidad: Int by mutableIntStateOf(1)

    var lineasDao: LineasDao? = null

    var titulo: String by mutableStateOf("")
    val subtitulo: String get() = "Cantidad $cantidad"

    var mesa: Mesa? by mutableStateOf(null)
    var camarero: Camarero? by mutableStateOf(null)

    private fun getTotal() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalValue = lineasDao?.getTotal(mesaId) ?: 0.0
            total.emit(totalValue)
        }
    }

    fun loadData(service: ServiceCom?) {
        if (service == null) return
        this.serviceCom = service
        mesasDao = service.getDB("mesas") as MesasDao
        camarerosDao = service.getDB("camareros") as CamareroDao
        lineasDao = service.getDB("lineaspedido") as LineasDao
        serverConfig = service.getServerConfig()

        viewModelScope.launch(Dispatchers.IO) {
            if (mesa == null) {
                mesa = mesasDao?.getMesa(mesaId)
                getTotal()
            }
            if (camarero == null) {
                camarero = camarerosDao?.getCamarero(camId)
            }

            titulo = camarero.toString() + " - " + mesa.toString()
        }
    }

    fun addLinea(linea: LineaPedido) {
        viewModelScope.launch(Dispatchers.IO) {
            for (i in 1..cantidad) {
                linea.mesaId = mesaId
                linea.camareroId = camId
                linea.id = System.currentTimeMillis()
                lineasDao?.insert(linea)
                mesasDao?.abrirMesa(mesaId.toInt())
            }

        }
    }

    fun hacerPedido() {
        viewModelScope.launch(Dispatchers.IO) {
            val lineas = lineasDao?.getNuevas(mesaId)
            if (lineas.isNullOrEmpty()) return@launch
            val pedido = JSONArray()
            for (linea in lineas) {
                linea.estado = "P"
                lineasDao?.update(linea)
                val obj = JSONObject()
                obj.put("descripcion", linea.descripcion)
                obj.put("precio", linea.precio)
                obj.put("descripcionT", linea.descripcionT)
                obj.put("teclaId", linea.teclaId)
                obj.put("id", linea.id)
                pedido.put(obj)
            }
            val params = mapOf(
                "idm" to mesaId,
                "idc" to camId,
                "pedido" to pedido,
                "uid_pedido" to System.currentTimeMillis()
            )
            val inst = Instrucciones(
                params = serverConfig?.getParams(params),
                endPoint = ApiEndPoints.PEDIDOS_ADD
            )
            serviceCom?.addInstruccion(inst)
            getTotal()
        }

    }

    fun cobrarMesa(entregado: Double) {
        //Dentro de un scope IO cargar todos los datos de la mesa y enviarlos al servidor
        //los paramteros son idm, idc, array de lineas y entregado
        viewModelScope.launch(Dispatchers.IO) {
            val lineas: List<Long> = lineasDao?.getAllIds(mesaId)!!
            if (lineas.isEmpty()) return@launch
            val params =
                mapOf("idm" to mesaId, "idc" to camId, "ids" to lineas, "entrega" to entregado)
            val inst = Instrucciones(
                params = serverConfig?.getParams(params),
                endPoint = ApiEndPoints.PEDIDOS_COBRAR
            )
            serviceCom?.addInstruccion(inst)
            mesasDao?.cerrarMesa(mesaId)
            lineasDao?.cobrarLineas(mesaId)
        }
    }


}
