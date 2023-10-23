package com.valleapp.valletpvlib.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.valleapp.valletpvlib.db.LineaCuenta
import com.valleapp.valletpvlib.db.LineasDao
import com.valleapp.valletpvlib.tools.ApiEndPoints
import com.valleapp.valletpvlib.tools.Instrucciones


class EditLineaModel(private val mainModel: MainModel, mesaId: Long) : ViewModel() {

    val db: LineasDao by mutableStateOf(mainModel.getDB("lineaspedido") as LineasDao)
    var pedidosActivos: MutableLiveData<List<LineaCuenta>> by mutableStateOf(MutableLiveData(db.getLineasCuenta(mesaId).value ?: listOf()))
    var pedidosEliminados: MutableLiveData<List<LineaCuenta>> by mutableStateOf(MutableLiveData(listOf()))

    var totalActivo: MutableLiveData<Double> by mutableStateOf(MutableLiveData(0.0))
    var totalBorrado: MutableLiveData<Double> by mutableStateOf(MutableLiveData(0.0))

    fun actualizarTotales() {
        totalActivo.value = pedidosActivos.value?.sumOf { it.cantidad * it.precio } ?: 0.0
        totalBorrado.value = pedidosEliminados.value?.sumOf { it.cantidad * it.precio } ?: 0.0
    }

    init {
        actualizarTotales()
    }

    fun setEliminado(linea: LineaCuenta, borrar: Boolean) {
        val activos = pedidosActivos.value?.toMutableList() ?: mutableListOf()
        val eliminados = pedidosEliminados.value?.toMutableList() ?: mutableListOf()

        if (borrar) {
            val existente = eliminados.find { it.descripcion == linea.descripcion && it.precio == linea.precio }

            if (existente != null) {
                existente.cantidad += linea.cantidad
            } else {
                eliminados.add(linea.copy())
            }

            if (linea.cantidad > 1) {
                linea.cantidad -= 1
            } else {
                activos.remove(linea)
            }
            pedidosActivos.value = activos
        } else {
            val existente = activos.find { it.descripcion == linea.descripcion && it.precio == linea.precio }

            if (existente != null) {
                existente.cantidad += linea.cantidad
            } else {
                activos.add(linea.copy())
            }

            eliminados.remove(linea)
            pedidosActivos.value = activos
            pedidosEliminados.value = eliminados
        }

        actualizarTotales()
    }

    fun ejecutarBorrado() {
        val idsParaBorrar = mutableListOf<Long>()

        pedidosEliminados.value?.forEach { lineaEliminada ->
            repeat(lineaEliminada.cantidad) {
                val id = db.findFirstByDescripcionAndPrecio(lineaEliminada.descripcion, lineaEliminada.precio)
                if (id != null) {
                    db.deleteById(id)
                    idsParaBorrar.add(id)
                }
            }
        }

        val params = mapOf(
            "ids" to idsParaBorrar,
        )

        val inst = Instrucciones(
            params = mainModel.getParams(params),
            endPoint = ApiEndPoints.EDITAR_CUENTA
        )

        mainModel.addInstruccion(inst)
    }


}




