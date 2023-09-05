package com.valleapp.valletpv.models


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.CamareroDao
import com.valleapp.valletpvlib.models.MainModel
import com.valleapp.valletpvlib.tools.ApiEndPoints
import com.valleapp.valletpvlib.tools.Instrucciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CamarerosModel(private val mainModel: MainModel) : ViewModel() {

    var showDialog: Boolean by mutableStateOf(false)

    var db: CamareroDao by mutableStateOf( mainModel.getDB("camareros") as CamareroDao)

    fun addCamarero(camarero: Camarero) {
        viewModelScope.launch(Dispatchers.IO) {
            val inst = Instrucciones(
                params = mainModel.getParams(mapOf("nombre" to camarero.nombre, "apellido" to camarero.nombre)),
                endPoint = ApiEndPoints.CAMAREROS_ADD,
            )
            mainModel.addInstruccion(inst)
            db.insertCamarero(camarero)
        }
    }

    fun setAutorizado(id: Long, b: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val inst = Instrucciones(
                params = mainModel.getParams(mapOf("autorizado" to b, "id" to id)),
                endPoint = ApiEndPoints.CAMAREROS_SET_AUTORIZADO
            )
            mainModel.addInstruccion(inst)
            db.setAutorizado(id, b)
        }
    }

}

