package com.valleapp.valletpvlib.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valleapp.valletpvlib.ValleApp
import com.valleapp.valletpvlib.db.TeclasDao
import com.valleapp.valletpvlib.models.CuentaModel
import com.valleapp.valletpvlib.models.TeclasModel
import com.valleapp.valletpvlib.ui.TecladoArt


@Composable
fun TeclasGrid(
    columns: Int,
    rows: Int,
) {
    val app = LocalContext.current.applicationContext as ValleApp
    val mainModel = app.mainModel

    val cuentaModel: CuentaModel = viewModel()
    val db = mainModel.getDB("teclas") as? TeclasDao
    if (db != null) {
        val model: TeclasModel = viewModel(initializer = { TeclasModel(db) })
        val tarifa by  cuentaModel.tarifa.collectAsState()

        LaunchedEffect(tarifa){
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
                   cuentaModel.addLinea(model.getLinea(tecla))
                }
            }
        }
    }

}
