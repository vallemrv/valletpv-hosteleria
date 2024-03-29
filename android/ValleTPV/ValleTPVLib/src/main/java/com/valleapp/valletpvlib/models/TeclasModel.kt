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
    var showSearch: Boolean by mutableStateOf(false)

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

    fun cargarTeclasByStr(busqueda: String) {
        strBus = busqueda
        viewModelScope.launch(Dispatchers.IO) {
            listaTeclas = teclasDao.getByBusqueda(strBus)
        }
    }

    fun getLinea(tecla: Tecla): LineaPedido {

        teclas.add(tecla)
        var precio = 0.0
        var descripcion = ""
        var descripcionT = ""
        for (t: Tecla in teclas) {

            // asignar precio segun tarifa si es 1 p1 y si es 2 p2
            precio = if (tarifa == 1) t.p1 else t.p2
            if (t.incremento > 0){
                precio += t.incremento
            }
            descripcion += t.nombre + " "
            descripcionT += t.nombre + " "

            if (t.descripcionT.isNotBlank() && t.descripcionT != "null") {
                descripcionT = t.descripcionT
            }

            if (t.descripcionR.isNotBlank() && t.descripcionR != "null") {
                descripcion = t.descripcionR
            }

        }
        teclas.clear()

        return LineaPedido(
            estado = "N",
            descripcion = descripcion,
            descripcionT = descripcionT,
            precio = precio,
            teclaId = tecla.id,
        )

    }
}
