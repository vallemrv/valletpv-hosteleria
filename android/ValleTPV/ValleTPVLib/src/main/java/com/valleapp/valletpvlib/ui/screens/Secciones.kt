package com.valleapp.valletpvlib.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valleapp.valletpvlib.ValleApp
import com.valleapp.valletpvlib.db.Seccion
import com.valleapp.valletpvlib.db.SeccionesDao
import com.valleapp.valletpvlib.models.TeclasModel
import com.valleapp.valletpvlib.ui.BotonIcon
import com.valleapp.valletpvlib.ui.theme.ExtendIcons


@Composable
fun Secciones(
    onClick: (Any?) -> Unit
) {
    val app = LocalContext.current.applicationContext as ValleApp
    val mainModel = app.mainModel

    val dbSeccion = mainModel.getDB("secciones") as? SeccionesDao
    if (dbSeccion != null) {
        val model: TeclasModel = viewModel()
        val lista by dbSeccion.getListaLive().observeAsState(initial = listOf())

        LaunchedEffect(lista) {
            if (lista.isNotEmpty() && model.getSeccionId() < 0) {
                model.cargarTeclasBySeccion(lista[0].id.toInt())
            }
        }

        Column {
            for (seccion in lista) {
                BotonIcon(
                    url = mainModel.getUrl(seccion.url),
                    contentDescription = seccion.nombre,
                    tag = seccion,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) { sec ->
                    model.cargarTeclasBySeccion((sec as Seccion).id.toInt())
                    onClick(null)
                }
            }
            BotonIcon(
                icon = ExtendIcons.Lupa, modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                contentDescription = "buscar"
            ) {
                model.showSearch = !model.showSearch
            }
        }

    }

}


