package com.valleapp.valletpvlib.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valleapp.valletpvlib.ValleApp
import com.valleapp.valletpvlib.db.LineaPedido
import com.valleapp.valletpvlib.db.TeclasDao
import com.valleapp.valletpvlib.models.TeclasModel
import com.valleapp.valletpvlib.ui.TecladoArt


@Composable
fun TeclasGrid(
    columns: Int,
    rows: Int,
    tarifa: Int,
    onClickTecla: (tecla: LineaPedido) -> Unit
) {
    val app = LocalContext.current.applicationContext as ValleApp
    val mainModel = app.mainModel

    val db = mainModel.getDB("teclas") as? TeclasDao
    if (db != null) {
        val model: TeclasModel = viewModel(initializer = { TeclasModel(db) })

        LaunchedEffect(tarifa) {
            model.tarifa = tarifa
        }

        Box {
            TecladoArt(
                columns = columns,
                rows = rows,
                items = model.listaTeclas,
            ) { tecla ->
                if (tecla.child > 0) {
                    model.cargarTeclasByParent(tecla)
                } else {
                    onClickTecla(model.getLinea(tecla))
                }
            }
        }

    }

}


@Composable
fun SearchView(show: Boolean, onClick: (String) -> Unit) {
    if (show) {
        var search by remember { mutableStateOf("") }
        Box(modifier = Modifier
            .fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Row {
                TextField(
                    value = search,
                    textStyle = TextStyle(fontSize = 35.sp),
                    onValueChange = {
                        search = it
                        onClick(search)
                    },
                    label = { Text("Buscar") },

                    modifier = Modifier
                        .padding(8.dp)
                )

            }

        }

    }
}