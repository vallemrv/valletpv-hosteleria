package com.valleapp.valletpvlib.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.valleapp.valletpvlib.db.AccionMesa
import com.valleapp.valletpvlib.db.Mesa
import com.valleapp.valletpvlib.db.MesasDao
import com.valleapp.valletpvlib.db.ZonasDao
import com.valleapp.valletpvlib.tools.ServiceCom


class MesasModel : ViewModel() {

    fun moverMesa(mesa: Mesa) {
        mesaOrg = mesa
        accionMesa = AccionMesa.MOVER
        titulo = "Mover mesa "+ mesaOrg!!.nombre
    }

    fun juntarMesa(mesa: Mesa) {
        mesaOrg = mesa
        accionMesa = AccionMesa.JUNTAR
        titulo = "Juntar mesa "+ mesaOrg!!.nombre
    }

    fun borrarMesa(mesa: Mesa) {
        mesaOrg = mesa
        accionMesa = AccionMesa.BORRAR
    }

    fun ejecutarAccion(mesa: Mesa) {

    }

    fun cancelar() {
        accionMesa = AccionMesa.NADA
        titulo = "Mesas"
        mesaOrg = null
    }

    var titulo: String by mutableStateOf("Mesas")
    var mesaOrg: Mesa? by mutableStateOf(null)
    var mesaDes: Mesa? by mutableStateOf(null)
    var idZona: Long by mutableLongStateOf(0)
    var accionMesa: AccionMesa by mutableStateOf(AccionMesa.NADA)
    var mService: ServiceCom? by mutableStateOf(null)
    var db: MesasDao? by mutableStateOf(null)
    var dbZona: ZonasDao? by mutableStateOf(null)

}
