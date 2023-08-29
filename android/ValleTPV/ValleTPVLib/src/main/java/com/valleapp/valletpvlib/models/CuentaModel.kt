package com.valleapp.valletpvlib.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.CamareroDao
import com.valleapp.valletpvlib.db.Mesa
import com.valleapp.valletpvlib.db.MesasDao
import com.valleapp.valletpvlib.tools.ServiceCom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CuentaModel(private val camId: Long, private val mesaId: Long) : ViewModel() {


    var cantidad: Int by mutableIntStateOf(1)
    private var serviceCom: ServiceCom? by mutableStateOf(null)
    private var mesasDao: MesasDao? = null
    private var camarerosDao: CamareroDao? = null

    var titulo: String by mutableStateOf("")
    val subtitulo: String get() = "Cantidad $cantidad"

    private var mesa: Mesa? by mutableStateOf(null)
    var camarero: Camarero? by mutableStateOf(null)

    fun loadData(service: ServiceCom?) {
        if (service == null) return
        this.serviceCom = service
        mesasDao = service?.getDB("mesas") as MesasDao
        camarerosDao = service.getDB("camareros") as CamareroDao

        viewModelScope.launch(Dispatchers.IO) {
            if (mesa == null) {
                mesa = mesasDao?.getMesa(mesaId)
            }
            if (camarero == null) {
                camarero = camarerosDao?.getCamarero(camId)
            }

            titulo = camarero.toString() + " - " + mesa.toString()
        }
    }

    fun addLinea(linea: LineaModel) {
        serviceCom?.addLinea(linea)
    }


}

