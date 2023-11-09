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

    private val _lineasTicket = MutableLiveData(listOf<LineaCuenta>())
    val lineasTicket: LiveData<List<LineaCuenta>> get() = _lineasTicket

    private val _lineasEditadas = MutableLiveData(listOf<LineaCuenta>())
    val lineasEditadas: LiveData<List<LineaCuenta>> get() = _lineasEditadas

    private val _totalTicket = MutableLiveData(0.0)
    val totalTicket: LiveData<Double> get() = _totalTicket

    private val _totalEditado = MutableLiveData(0.0)
    val totalEditado: LiveData<Double> get() = _totalEditado


    fun inicializar(cuenta: List<LineaCuenta>){
        _lineasTicket.value = cuenta.map { it.copy() }.toMutableList()
        _lineasEditadas.value = emptyList()
        actualizarTotales()
    }

    private fun actualizarTotales() {
        _totalTicket.value = _lineasTicket.value?.sumOf { it.cantidad * it.precio } ?: 0.0
        _totalEditado.value = _lineasEditadas.value?.sumOf { it.cantidad * it.precio } ?: 0.0

        println("Total Ticket: ${_totalTicket.value}")
        println("Total Editado: ${_totalEditado.value}")
    }

    fun setEliminado(linea: LineaCuenta, borrar: Boolean) {
        val activos = _lineasTicket.value?.toMutableList() ?: mutableListOf()
        val eliminados = _lineasEditadas.value?.toMutableList() ?: mutableListOf()

        val targetList = if (borrar) eliminados else activos

        val existente = targetList.find { it.descripcion == linea.descripcion && it.precio == linea.precio }


        if (existente != null) {
            existente.cantidad ++
            existente.total = existente.cantidad * existente.precio
        } else {
            val aux = linea.copy()
            aux.cantidad = 1
            aux.total = aux.cantidad * aux.precio
            targetList.add(aux)
        }

        linea.cantidad --
        linea.total = linea.cantidad * linea.precio

        if (linea.cantidad < 1) {
            if (borrar) {
                activos.remove(linea)
            }else{
                eliminados.remove(linea)
            }
        }

        _lineasTicket.value = activos
        _lineasEditadas.value = eliminados
        actualizarTotales()
    }

    fun ejecutarBorrado(motivo: String, idm: Long, idc: Long) {
        _lineasEditadas.value?.let { eliminarPedidos(it, motivo, idm, idc) }
        actualizarTotales()
    }


    private fun eliminarPedidos(pedidos: List<LineaCuenta>, motivo: String = "", idm: Long, idc: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val idsParaBorrar = mutableListOf<Long>()  // Reemplaza TipoDeId con el tipo correcto de tu ID

            pedidos.forEach { pedido ->
                repeat(pedido.cantidad) {
                    db.findFirstByDescripcionAndPrecio(pedido.descripcion, pedido.precio, estado = listOf("P"))?.let { id ->
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
