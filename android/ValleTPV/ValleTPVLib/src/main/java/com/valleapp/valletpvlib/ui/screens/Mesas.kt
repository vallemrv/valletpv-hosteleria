package com.valleapp.valletpvlib.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.valleapp.valletpvlib.db.AccionMesa
import com.valleapp.valletpvlib.db.Mesa
import com.valleapp.valletpvlib.db.MesasDao
import com.valleapp.valletpvlib.db.Zona
import com.valleapp.valletpvlib.db.ZonasDao
import com.valleapp.valletpvlib.routers.RoutersBase
import com.valleapp.valletpvlib.tools.ServiceCom
import com.valleapp.valletpvlib.ui.BotonSimple
import com.valleapp.valletpvlib.ui.TableroMesas
import com.valleapp.valletpvlib.ui.ValleGridSimple
import com.valleapp.valletpvlib.ui.theme.ColorTheme


class MesasModel : ViewModel() {

    fun moverMesa(mesa: Mesa) {
        mesaOrg = mesa
        accionMesa = AccionMesa.MOVER

    }

    fun juntarMesa(mesa: Mesa) {
        mesaOrg = mesa
        accionMesa = AccionMesa.JUNTAR
    }

    fun borrarMesa(mesa: Mesa) {
        mesaOrg = mesa
        accionMesa = AccionMesa.BORRAR
    }

    fun ejecutarAccion(mesa: Mesa) {

    }

    var mesaOrg: Mesa by mutableStateOf(Mesa())
    var mesaDes: Mesa by mutableStateOf(Mesa())
    var idZona: Long by mutableLongStateOf(0)
    var accionMesa: AccionMesa by mutableStateOf(AccionMesa.NADA)
    var mService: ServiceCom? by mutableStateOf(null)
    var db: MesasDao? by mutableStateOf(null)
    var dbZona: ZonasDao? by mutableStateOf(null)

}

@Composable
fun MesasGrid(navController: NavController, camId: Long, column: Int = 4) {

    BaseScreen {
        val model: MesasModel = viewModel()

        model.mService = it.mService
        model.db = model.mService?.getDB("mesas") as? MesasDao
        model.dbZona = model.mService?.getDB("zonas") as? ZonasDao


        val listaZonas by model.dbZona?.getListaLive()?.observeAsState(initial = listOf())
            ?: remember { mutableStateOf(listOf()) }

        val zona = listaZonas.firstOrNull()
        if (zona != null && model.idZona <= 0) {
            model.idZona = zona.id
        }

        val listaMesas by model.db?.getAllByZona(model.idZona)?.observeAsState(initial = listOf())
            ?: remember { mutableStateOf(listOf()) }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
        ) {
            ListaZonas(listaZonas, model)
            if (model.accionMesa != AccionMesa.NADA)
                SelectorMesas(listaMesas, column, model)
            else
                TableroMesaMain(navController, camId, listaMesas, model)

        }
    }

}


@Composable
fun ListaZonas(listaZonas: List<Zona>, model: MesasModel) {

    Box(
        modifier = Modifier
            .padding(10.dp)
            .height(100.dp)
    ) {
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)) {
            LazyRow {
                items(listaZonas) { zona ->
                    BotonSimple(
                        text = zona.nombre,
                        tag = zona,
                        color = ColorTheme.hexToComposeColor(zona.color)
                    ) { tag ->
                        model.idZona = (tag as Zona).id
                    }
                }
            }
        }
    }

}

@Composable
fun SelectorMesas(listaMesas: List<Mesa>, column: Int = 4, model: MesasModel) {
    Box(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxHeight()
    ) {
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 10.dp))
        {
            ValleGridSimple(columns = column) {
                items(listaMesas) {
                    if (it.id != model.mesaOrg.id) {
                        BotonSimple(
                            text = it.nombre,
                            tag = it,
                            color = ColorTheme.hexToComposeColor(it.color)
                        ) { tag ->
                            model.ejecutarAccion(tag as Mesa)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableroMesaMain(
    navController: NavController,
    camId: Long,
    listaMesas: List<Mesa>,
    model: MesasModel
) {
    Box(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxHeight()
    ) {
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 10.dp))
        {
            TableroMesas(columns = 5, mesas = listaMesas, onItemClick = { m ->
                navController.navigate(
                    RoutersBase.Cuenta.route
                        .replace("{camId}", camId.toString())
                        .replace("{mesaId}", m.id.toString())
                )
            },
                onAccionClick = { mesa, accion ->
                    when (accion) {
                        AccionMesa.MOVER -> model.moverMesa(mesa)
                        AccionMesa.JUNTAR -> model.juntarMesa(mesa)
                        AccionMesa.BORRAR -> model.borrarMesa(mesa)
                        else -> {}
                    }
                })
        }
    }
}