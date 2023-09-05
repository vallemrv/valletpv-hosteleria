package com.valleapp.valletpvlib.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.AccionMesa
import com.valleapp.valletpvlib.db.Mesa
import com.valleapp.valletpvlib.db.MesasDao
import com.valleapp.valletpvlib.db.ZonasDao
import com.valleapp.valletpvlib.tools.ApiEndPoints
import com.valleapp.valletpvlib.tools.Instrucciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MesasModel(private val mainModel: MainModel) : ViewModel() {

    var db: MesasDao = mainModel.getDB("mesas") as MesasDao
    var dbZona: ZonasDao = mainModel.getDB("zonas") as ZonasDao

    var titulo: String by mutableStateOf("Mesas")
    var mesaOrg: Mesa? by mutableStateOf(null)
    var idZona: Long by mutableLongStateOf(-1)
    var accionMesa: AccionMesa by mutableStateOf(AccionMesa.NADA)


    fun moverMesa(mesa: Mesa) {
        mesaOrg = mesa
        accionMesa = AccionMesa.MOVER
        titulo = "Mover mesa " + mesaOrg!!.nombre
    }

    fun juntarMesa(mesa: Mesa) {
        mesaOrg = mesa
        accionMesa = AccionMesa.JUNTAR
        titulo = "Juntar mesa " + mesaOrg!!.nombre
    }

    fun borrarMesa(mesa: Mesa) {
        mesaOrg = mesa
        accionMesa = AccionMesa.BORRAR
    }


    fun ejecutarAccion(
        mesa: Mesa? = null,
        motivo: String? = null,
        camId: Long? = null
    ) {
        when (accionMesa) {
            AccionMesa.MOVER -> {

            }

            AccionMesa.JUNTAR -> {

            }

            AccionMesa.BORRAR -> {

                viewModelScope.launch(Dispatchers.IO) {
                    val mesasDao = mainModel.getDB("mesas") as? MesasDao
                    mesasDao?.cerrarMesa(mesaOrg!!.id)
                    val inst = Instrucciones(
                        ApiEndPoints.BORRAR_MESA, mainModel.getParams(
                            mapOf(
                                "idm" to mesaOrg!!.id, "motivo" to motivo!!, "idc" to camId!!
                            )
                        )
                    )
                    mainModel.addInstruccion(inst)
                    accionMesa = AccionMesa.NADA

                }
            }

            else -> {
            }
        }

    }

    fun cancelar() {
        accionMesa = AccionMesa.NADA
        titulo = "Mesas"
        mesaOrg = null
    }


}
