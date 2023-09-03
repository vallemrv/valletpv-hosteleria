package com.valleapp.valletpv.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.valleapp.valletpv.ui.BorrarMesa
import com.valleapp.valletpvlib.db.AccionMesa
import com.valleapp.valletpvlib.models.BindServiceModel
import com.valleapp.valletpvlib.models.MesasModel
import com.valleapp.valletpvlib.ui.BotonAccion
import com.valleapp.valletpvlib.ui.ValleTopBar
import com.valleapp.valletpvlib.ui.screens.MesasGrid
import com.valleapp.valletpvlib.ui.theme.ExtendIcons

@Composable
fun MesasTpvScreen(
    navController: NavController,
    bindServiceModel: BindServiceModel,
    camId: Long = 0
) {
    val model: MesasModel = viewModel()

    Scaffold(
        topBar = {
            ValleTopBar(title = model.titulo,
                backAction = {
                    navController.popBackStack()
                }
            ) {
                if (model.accionMesa == AccionMesa.NADA) {
                    BotonAccion(icon = ExtendIcons.Mensajes, contentDescription = "Mesajes") {

                    }
                    BotonAccion(
                        ExtendIcons.AddCamareros, "Agregar camareors",
                        onClick = { })

                    BotonAccion(
                        ExtendIcons.Configuration, "Impresoras",
                        onClick = { })

                    BotonAccion(
                        ExtendIcons.Listado,
                        "Camareros",
                        onClick = { })

                    BotonAccion(icon = ExtendIcons.AbrirCaja, contentDescription = "Abrir cajon") {
                        bindServiceModel.abrirCajon()
                    }
                } else {
                    BotonAccion(icon = ExtendIcons.Reset, contentDescription = "Cancelar") {
                        model.cancelar()
                    }
                }

            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            MesasGrid(
                navController = navController,
                bindServiceModel = bindServiceModel,
                camId = camId,
                landScape = true,
                columnMesas = 5
            )
            if (model.accionMesa == AccionMesa.BORRAR) {
                BorrarMesa(
                    onDismissRequest = { model.cancelar() },
                    onSubmit = { motivo ->
                        model.ejecutarAccion(
                            motivo = motivo,
                            camId = camId,
                            mService = bindServiceModel.mService
                        )
                    })
            }
        }
    }
}