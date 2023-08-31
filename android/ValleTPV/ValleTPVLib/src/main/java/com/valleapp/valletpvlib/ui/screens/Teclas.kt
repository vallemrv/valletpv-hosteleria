package com.valleapp.valletpvlib.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valleapp.valletpvlib.db.TeclasDao
import com.valleapp.valletpvlib.models.BindServiceModel
import com.valleapp.valletpvlib.models.CuentaModel
import com.valleapp.valletpvlib.models.TeclasModel
import com.valleapp.valletpvlib.ui.TecladoArt


@Composable
fun TeclasGrid(
    bindServiceModel: BindServiceModel,
    columns: Int,
    rows: Int,
) {

    val cuentaModel: CuentaModel = viewModel()
    val mService = bindServiceModel.mService
    val db = mService?.getDB("teclas") as? TeclasDao
    if (db != null) {
        val model: TeclasModel = viewModel(initializer = { TeclasModel(db) })
        model.tarifa = cuentaModel.mesa?.tarifa ?: 1

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
