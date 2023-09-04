package com.valleapp.valletpv.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.valleapp.valletpv.models.CamarerosModel
import com.valleapp.valletpv.ui.BorrarMesa
import com.valleapp.valletpv.ui.PaseCamarerosDialog
import com.valleapp.valletpvlib.ValleApp
import com.valleapp.valletpvlib.db.AccionMesa
import com.valleapp.valletpvlib.models.MesasModel
import com.valleapp.valletpvlib.ui.BotonAccion
import com.valleapp.valletpvlib.ui.ValleTopBar
import com.valleapp.valletpvlib.ui.screens.MesasGrid
import com.valleapp.valletpvlib.ui.theme.ExtendIcons

@Composable
fun MesasTpvScreen(
    navController: NavController,
    camId: Long = 0
) {

    val app = LocalContext.current.applicationContext as ValleApp
    val mainModel = app.mainModel
    val model: MesasModel = viewModel(initializer = { MesasModel(mainModel) })
    val camarerosModel: CamarerosModel = viewModel(initializer = { CamarerosModel(mainModel) })
    var showDialogSelPas by remember {
        mutableStateOf(false)
    }

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
                        ExtendIcons.AddCamareros, "Agregar camareros",
                        onClick = { showDialogSelPas = true })

                    BotonAccion(
                        ExtendIcons.Configuration, "Impresoras",
                        onClick = { })

                    BotonAccion(
                        ExtendIcons.Listado,
                        "Camareros",
                        onClick = { })

                    BotonAccion(icon = ExtendIcons.AbrirCaja, contentDescription = "Abrir cajon") {
                        mainModel.abrirCajon()
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
                        )
                    })
            }
            PaseCamarerosDialog(camarerosModel, showDialogSelPas) {
                showDialogSelPas = false
            }
        }
    }
}