package com.valleapp.valletpvlib.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.LineaCuenta
import com.valleapp.valletpvlib.db.LineasDao
import com.valleapp.valletpvlib.tools.ApiEndPoints
import com.valleapp.valletpvlib.tools.Instrucciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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

    fun ejecutarBorrado(motivo: String, idm: Long, idc: Long) {
        _pedidosEliminados.value?.let { eliminarPedidos(it, motivo, idm, idc) }
        actualizarTotales()
    }


    private fun eliminarPedidos(pedidos: List<LineaCuenta>, motivo: String = "", idm: Long, idc: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val idsParaBorrar = mutableListOf<Long>()  // Reemplaza TipoDeId con el tipo correcto de tu ID

            pedidos.forEach { pedido ->
                repeat(pedido.cantidad) {
                    db.findFirstByDescripcionAndPrecio(pedido.descripcion, pedido.precio)?.let { id ->
                        db.deleteById(id)
                        idsParaBorrar.add(id)
                    }
                }
            }

            val params = mapOf("ids" to idsParaBorrar, "motivo" to motivo, "idm" to idm, "idc" to idc)
            val inst = Instrucciones(
                params = mainModel.getParams(params),
                endPoint = ApiEndPoints.EDITAR_CUENTA
            )
            mainModel.addInstruccion(inst)
        }
    }
}
