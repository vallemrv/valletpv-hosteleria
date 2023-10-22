package com.valleapp.valletpvlib.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.AccionMesa
import com.valleapp.valletpvlib.db.LineasDao
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
                viewModelScope.launch(Dispatchers.IO){
                    if (mesa != null && mesaOrg != null) {
                        val mesasDao = mainModel.getDB("mesas") as? MesasDao
                        val lineaCuenta = mainModel.getDB("lineaspedido") as? LineasDao
                        val lineasOrg = lineaCuenta?.getByMesa(mesaOrg!!.id)
                        val lineasDest = lineaCuenta?.getByMesa(mesa.id)

                        if (lineasDest?.isEmpty() == true) {
                            lineasOrg?.forEach { linea ->
                                linea.nomMesa = mesa.nombre
                                linea.mesaId = mesa.id
                                linea.zonaId = mesa.zonaId
                                lineaCuenta.update(linea)
                            }
                            mesaOrg!!.id.let { mesasDao?.cerrarMesa(it) }
                            mesasDao?.abrirMesa(mesa.id)
                        } else {
                            lineasOrg?.forEach { linea ->
                                linea.nomMesa = mesa.nombre
                                linea.mesaId = mesa.id
                                linea.zonaId = mesa.zonaId
                                lineaCuenta.update(linea)
                            }
                            lineasDest?.forEach { linea ->
                                linea.nomMesa = mesaOrg!!.nombre
                                linea.mesaId = mesaOrg!!.id
                                linea.zonaId = mesaOrg!!.zonaId
                                lineaCuenta.update(linea)
                            }
                        }

                        val inst = Instrucciones(
                            ApiEndPoints.CAMBIAR_MESAS, mainModel.getParams(
                                mapOf(
                                    "idOrg" to mesaOrg!!.id, "idDest" to mesa.id
                                )
                            )
                        )
                        mainModel.addInstruccion(inst)
                    }
                    cancelar()
                }

            }

            AccionMesa.JUNTAR -> {
                viewModelScope.launch(Dispatchers.IO) {
                    if (mesa != null && mesaOrg != null) {
                        val mesasDao = mainModel.getDB("mesas") as? MesasDao
                        val lineaCuenta = mainModel.getDB("lineaspedido") as? LineasDao

                        if (mesa.abierta) {
                            val lineasDest = lineaCuenta?.getByMesa(mesa.id)
                            lineasDest?.forEach { linea ->
                                linea.nomMesa = mesaOrg!!.nombre
                                linea.mesaId = mesaOrg!!.id
                                linea.zonaId = mesaOrg!!.zonaId
                                lineaCuenta.update(linea)
                            }

                            mesa.id?.let { mesasDao?.cerrarMesa(it) }
                            val inst = Instrucciones(
                                ApiEndPoints.JUNTAR_MESAS, mainModel.getParams(
                                    mapOf(
                                        "idOrg" to mesaOrg!!.id, "idDest" to mesa.id
                                    )
                                )
                            )
                            mainModel.addInstruccion(inst)
                        }
                    }
                    cancelar()
                }
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
                    cancelar()
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
