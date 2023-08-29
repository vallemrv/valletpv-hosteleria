package com.valleapp.valletpvlib.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.Tecla
import com.valleapp.valletpvlib.db.TeclasDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TeclasModel(private val teclasDao: TeclasDao) : ViewModel() {


    private var seccionId: Int = -1
    private var strBus: String = ""
    private var teclas = ArrayList<Tecla>()

    var listaTeclas: List<Tecla> by mutableStateOf(listOf())

    fun getSeccionId(): Int {
        return seccionId
    }

    fun cargarTeclasBySeccion(seccion: Int) {
        seccionId = seccion
        viewModelScope.launch(Dispatchers.IO){
            listaTeclas = teclasDao.getBySeccion(seccionId)
        }
    }

    fun cargarTeclasByParent(parent: Tecla) {
        teclas.add(parent)
        viewModelScope.launch(Dispatchers.IO){
            listaTeclas = teclasDao.getByParent(parent.id as Int)
        }
    }

    fun cargarTeclasByBusqueda(busqueda: String) {
        strBus = busqueda
        viewModelScope.launch(Dispatchers.IO){
            listaTeclas = teclasDao.getByBusqueda(strBus)
        }
    }

    fun getLinea(tecla: Tecla) {
        teclas.add(tecla)
    }
}
