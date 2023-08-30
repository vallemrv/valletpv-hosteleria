package com.valleapp.valletpvlib.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.LineaPedido
import com.valleapp.valletpvlib.db.Tecla
import com.valleapp.valletpvlib.db.TeclasDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TeclasModel(private val teclasDao: TeclasDao) : ViewModel() {


    private var seccionId: Int = -1
    private var strBus: String = ""
    private var teclas = ArrayList<Tecla>()


    var tarifa: Int by mutableStateOf(1)
    var listaTeclas: List<Tecla> by mutableStateOf(listOf())

    fun getSeccionId(): Int {
        return seccionId
    }

    fun cargarTeclasBySeccion(seccion: Int) {
        seccionId = seccion
        viewModelScope.launch(Dispatchers.IO) {
            listaTeclas = teclasDao.getBySeccion(seccionId)
        }
    }

    fun cargarTeclasByParent(parent: Tecla) {
        teclas.add(parent)
        viewModelScope.launch(Dispatchers.IO) {
            listaTeclas = teclasDao.getByParent(parent.id.toInt())
        }
    }

    fun cargarTeclasByBusqueda(busqueda: String) {
        strBus = busqueda
        viewModelScope.launch(Dispatchers.IO) {
            listaTeclas = teclasDao.getByBusqueda(strBus)
        }
    }

    fun getLinea(tecla: Tecla): LineaPedido {

        teclas.add(tecla)
        var precio = 0.0
        var descripcion = ""
        var descripcion_t = ""
        for (t: Tecla in teclas) {

            // asignar precio segun tarifa si es 1 p1 y si es 2 p2
            precio = if (tarifa == 1) t.p1 else t.p2
            if (t.incremento > 0){
                precio += t.incremento
            }
            descripcion += t.nombre + " "
            descripcion_t += t.nombre + " "

            if (!t.descripcion_t.isNullOrBlank() && t.descripcion_t != "null") {
                descripcion_t = t.descripcion_t!!
            }

            if (!t.descripcion_r.isNullOrBlank() && t.descripcion_r != "null") {
                descripcion = t.descripcion_r!!
            }

        }
        teclas.clear()

        return LineaPedido(
            estado = "N",
            descripcion = descripcion,
            descripcion_t = descripcion_t,
            precio = precio,
            tecla_id = tecla.id,
            pk = System.currentTimeMillis(),
        )

    }
}
