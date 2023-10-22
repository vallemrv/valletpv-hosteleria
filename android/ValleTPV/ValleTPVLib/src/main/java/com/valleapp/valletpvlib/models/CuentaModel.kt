package com.valleapp.valletpvlib.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.CamareroDao
import com.valleapp.valletpvlib.db.LineaPedido
import com.valleapp.valletpvlib.db.LineasDao
import com.valleapp.valletpvlib.db.MesasDao
import com.valleapp.valletpvlib.tools.ApiEndPoints
import com.valleapp.valletpvlib.tools.Instrucciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class CuentaModel(val mainModel: MainModel, private val camId: Long, private val mesaId: Long) : ViewModel() {


    private val mesasDao: MesasDao = mainModel.getDB("mesas") as MesasDao
    private val camarerosDao: CamareroDao = mainModel.getDB("camareros") as CamareroDao

    val lineasDao: LineasDao = mainModel.getDB("lineaspedido") as LineasDao

    val total = MutableStateFlow(0.0)
    val tarifa = MutableStateFlow(1)
    val titulo = MutableStateFlow("")
    var cantidad: Int by mutableIntStateOf(1)

    val subtitulo: String get() = "Cantidad $cantidad"

    init {
        loadData()
    }

    private fun getTotal() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalValue = lineasDao.getTotal(mesaId)
            total.emit(totalValue)
        }
    }


    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val mesa = mesasDao.getMesa(mesaId)
            val camarero = camarerosDao.getCamarero(camId)
            titulo.emit("$camarero - $mesa")
            mesa.tarifa?.let { tarifa.emit(it) }
            getTotal()
        }
    }

    fun addLinea(linea: LineaPedido) {
        viewModelScope.launch(Dispatchers.IO) {
            for (i in 1..cantidad) {
                linea.mesaId = mesaId
                linea.camareroId = camId
                linea.id = System.currentTimeMillis()
                lineasDao.insert(linea)
                mesasDao.abrirMesa(mesaId)
            }

        }
    }

    fun hacerPedido() {
        viewModelScope.launch(Dispatchers.IO) {
            val lineas = lineasDao.getNuevas(mesaId)
            if (lineas.isEmpty()) return@launch
            val pedido = JSONArray()
            for (linea in lineas) {
                linea.estado = "P"
                lineasDao.update(linea)
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
                params = mainModel.getParams(params),
                endPoint = ApiEndPoints.PEDIDOS_ADD
            )
            mainModel.addInstruccion(inst)
            getTotal()
        }

    }

    fun cobrarMesa(entregado: Double) {
        //Dentro de un scope IO cargar todos los datos de la mesa y enviarlos al servidor
        //los paramteros son idm, idc, array de lineas y entregado
        viewModelScope.launch(Dispatchers.IO) {
            val lineas: List<Long> = lineasDao.getAllIds(mesaId)
            if (lineas.isEmpty()) return@launch
            val params =
                mapOf("idm" to mesaId, "idc" to camId, "ids" to lineas, "entrega" to entregado)
            val inst = Instrucciones(
                params = mainModel.getParams(params),
                endPoint = ApiEndPoints.PEDIDOS_COBRAR
            )
            mainModel.addInstruccion(inst)
            mesasDao.cerrarMesa(mesaId)
            lineasDao.cobrarLineas(mesaId)
        }
    }


}

