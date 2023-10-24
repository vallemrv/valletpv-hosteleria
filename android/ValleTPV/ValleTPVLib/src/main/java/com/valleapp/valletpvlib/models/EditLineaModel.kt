package com.valleapp.valletpvlib.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.valleapp.valletpvlib.db.LineaCuenta
import com.valleapp.valletpvlib.db.LineasDao
import com.valleapp.valletpvlib.tools.ApiEndPoints
import com.valleapp.valletpvlib.tools.Instrucciones

class EditLineaModel(private val mainModel: MainModel) : ViewModel() {

    private val db: LineasDao = mainModel.getDB("lineaspedido") as LineasDao

    private val _pedidosActivos = MutableLiveData(listOf<LineaCuenta>())
    val pedidosActivos: LiveData<List<LineaCuenta>> get() = _pedidosActivos

    private val _pedidosEliminados = MutableLiveData(listOf<LineaCuenta>())
    val pedidosEliminados: LiveData<List<LineaCuenta>> get() = _pedidosEliminados

    private val _totalActivo = MutableLiveData(0.0)
    val totalActivo: LiveData<Double> get() = _totalActivo

    private val _totalBorrado = MutableLiveData(0.0)
    val totalBorrado: LiveData<Double> get() = _totalBorrado


    fun inicializar(cuenta: List<LineaCuenta>){
        _pedidosActivos.value = cuenta.map { it.copy() }.toMutableList()
        _pedidosEliminados.value = emptyList()
        actualizarTotales()
    }

    private fun actualizarTotales() {
        _totalActivo.value = _pedidosActivos.value?.sumOf { it.cantidad * it.precio } ?: 0.0
        _totalBorrado.value = _pedidosEliminados.value?.sumOf { it.cantidad * it.precio } ?: 0.0
    }

    fun setEliminado(linea: LineaCuenta, borrar: Boolean) {
        val activos = _pedidosActivos.value?.toMutableList() ?: mutableListOf()
        val eliminados = _pedidosEliminados.value?.toMutableList() ?: mutableListOf()

        val targetList = if (borrar) eliminados else activos

        val existente = targetList.find { it.descripcion == linea.descripcion && it.precio == linea.precio }


        if (existente != null) {
            existente.cantidad ++
        } else {
            val aux = linea.copy()
            aux.cantidad = 1
            targetList.add(aux)
        }

        linea.cantidad --

        if (linea.cantidad < 1) {
            if (borrar)
            activos.remove(linea)
            else
            eliminados.remove(linea)
        }

        println("Linea: $linea Borrado = $borrar")
        println("Existente: $existente")

        _pedidosActivos.postValue(activos)
        _pedidosEliminados.postValue(eliminados)

        actualizarTotales()
    }

    fun ejecutarBorrado() {
        _pedidosEliminados.value?.let { eliminarPedidos(it) }
        actualizarTotales()
    }

    private fun eliminarPedidos(pedidos: List<LineaCuenta>) {
        val idsParaBorrar = pedidos.map { lineaEliminada ->
            List(lineaEliminada.cantidad) {
                db.findFirstByDescripcionAndPrecio(lineaEliminada.descripcion, lineaEliminada.precio)
            }
        }.flatten().filterNotNull()

        idsParaBorrar.forEach { db.deleteById(it) }

        val params = mapOf("ids" to idsParaBorrar)
        val inst = Instrucciones(params = mainModel.getParams(params), endPoint = ApiEndPoints.EDITAR_CUENTA)
        mainModel.addInstruccion(inst)
    }
}
