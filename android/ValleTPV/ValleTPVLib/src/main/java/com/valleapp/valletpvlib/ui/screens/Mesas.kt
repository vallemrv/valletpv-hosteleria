package com.valleapp.valletpvlib.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.valleapp.valletpvlib.db.AccionMesa
import com.valleapp.valletpvlib.db.Mesa
import com.valleapp.valletpvlib.db.MesasDao
import com.valleapp.valletpvlib.db.Zona
import com.valleapp.valletpvlib.db.ZonasDao
import com.valleapp.valletpvlib.models.BindServiceModel
import com.valleapp.valletpvlib.models.MesasModel
import com.valleapp.valletpvlib.routers.RoutersBase
import com.valleapp.valletpvlib.ui.BotonSimple
import com.valleapp.valletpvlib.ui.TableroMesas
import com.valleapp.valletpvlib.ui.ValleGridSimple
import com.valleapp.valletpvlib.ui.theme.ColorTheme


@Composable
fun MesasGrid(
    navController: NavController,
    bindServiceModel: BindServiceModel,
    camId: Long,
    columnMesas: Int,
    landScape: Boolean
) {

    val model: MesasModel = viewModel()


    model.mService = bindServiceModel.mService
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

    if (landScape) {
        LandScapeGrid(
            navController = navController,
            camId = camId,
            listaZonas = listaZonas,
            listaMesas = listaMesas,
            model = model,
            column = columnMesas
        )
    } else {
        PortraitGrid(
            navController = navController,
            camId = camId,
            listaZonas = listaZonas,
            listaMesas = listaMesas,
            model = model,
            column = columnMesas
        )
    }

}

@Composable
fun PortraitGrid(
    navController: NavController,
    camId: Long,
    listaZonas: List<Zona>,
    listaMesas: List<Mesa>,
    model: MesasModel,
    column: Int
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(0.2f)
                .padding(start = 10.dp)
        ) {
            ListaZonas(listaZonas, model, landScape = false)
        }
        Box(modifier = Modifier.weight(0.8f)) {
            if (model.accionMesa != AccionMesa.NADA) SelectorMesas(listaMesas, model, column)
            else TableroMesaMain(navController, camId, listaMesas, model, column)
        }

    }
}

@Composable
fun LandScapeGrid(
    navController: NavController,
    camId: Long,
    listaZonas: List<Zona>,
    listaMesas: List<Mesa>,
    model: MesasModel,
    column: Int
) {

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
    ) {

        Box(modifier = Modifier.weight(0.85f)) {
            if (model.accionMesa != AccionMesa.NADA) SelectorMesas(listaMesas, model, column)
            else TableroMesaMain(navController, camId, listaMesas, model, column)
        }
        Box(
            modifier = Modifier
                .weight(0.15f)
                .padding(start = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            ListaZonas(listaZonas, model, landScape = true)
        }
    }
}


@Composable
fun ListaZonas(listaZonas: List<Zona>, model: MesasModel, landScape: Boolean) {


    if (landScape) {
        LazyColumn {
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
    } else {
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

@Composable
fun SelectorMesas(listaMesas: List<Mesa>, model: MesasModel, column: Int) {
    Box(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxHeight()
    ) {

        ValleGridSimple(columns = column) {
            items(listaMesas) {
                if (it.id != model.mesaOrg?.id) {
                    BotonSimple(
                        text = it.nombre,
                        tag = it,
                        color = ColorTheme.hexToComposeColor(it.color)
                    ) { tag ->
                        model.ejecutarAccion(tag as Mesa)
                    }
                } else {
                    BotonSimple(
                        text = it.nombre,
                        color = Color.Red
                    ) {}
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
    model: MesasModel,
    column: Int
) {
    Box(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxHeight()
    ) {

        TableroMesas(columns = column, mesas = listaMesas, onItemClick = { m ->
            navController.navigate(
                RoutersBase.Cuenta.route.replace("{camId}", camId.toString())
                    .replace("{mesaId}", m.id.toString())
            )
        }, onAccionClick = { mesa, accion ->
            when (accion) {
                AccionMesa.MOVER -> model.moverMesa(mesa)
                AccionMesa.JUNTAR -> model.juntarMesa(mesa)
                AccionMesa.BORRAR -> model.borrarMesa(mesa)
                else -> {}
            }
        })
    }

}