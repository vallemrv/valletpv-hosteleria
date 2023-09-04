package com.valleapp.valletpvlib.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.valleapp.valletpvlib.ValleApp
import com.valleapp.valletpvlib.db.CamareroDao
import com.valleapp.valletpvlib.routers.RoutersBase
import com.valleapp.valletpvlib.ui.TableroCamareros


@Composable
fun CamarerosGrid(
    navController: NavController,
    column: Int
) {
    var app = LocalContext.current.applicationContext as    ValleApp
    var mainModel = app.mainModel
    val db: CamareroDao? = mainModel.getDB("camareros") as CamareroDao?

    val listaCamareros by db?.getAutorizados(autorizado = true)
        ?.observeAsState(initial = listOf()) ?: remember { mutableStateOf(listOf()) }

    Box {
        TableroCamareros(columns = column, camareros = listaCamareros) { camarero ->
            navController.navigate(
                RoutersBase.Mesas.route.replace("{camId}", camarero.id.toString())
            )
        }

    }
}

