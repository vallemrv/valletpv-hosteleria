package com.valleapp.valletpv.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.valleapp.valletpv.models.CamarerosModel
import com.valleapp.valletpv.routers.Routers
import com.valleapp.valletpv.ui.AddCamareroDialog
import com.valleapp.valletpvlib.ValleApp
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.CamareroDao
import com.valleapp.valletpvlib.routers.RoutersBase
import com.valleapp.valletpvlib.ui.BotonAccion
import com.valleapp.valletpvlib.ui.BotonIcon
import com.valleapp.valletpvlib.ui.ListaSimple
import com.valleapp.valletpvlib.ui.ValleTopBar
import com.valleapp.valletpvlib.ui.theme.ColorTheme
import com.valleapp.valletpvlib.ui.theme.ExtendIcons


@Composable
fun CamarerosPaseScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as ValleApp
    val mainModel = app.mainModel

    if (!mainModel.isPreferenciasCargadas) {
        navController.navigate(RoutersBase.Preferencias.route)
    }


    val vModel: CamarerosModel = viewModel(initializer = {
        CamarerosModel(mainModel)
    })

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vModel.showDialog = true },
                containerColor = ColorTheme.Primary,
                modifier = Modifier
                    .padding(10.dp)
                    .size(80.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    painter = ExtendIcons.Add,
                    contentDescription = "Agregar",
                    tint = Color.Black,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                )
            }
        },
        topBar = {
            ValleTopBar(
                title = "Pase de Camareros"
            ) {
                BotonAccion(icon = ExtendIcons.Arqueo, contentDescription = "Arqueo") {
                    navController.navigate(Routers.Arqueo.route)
                }
                BotonAccion(icon = ExtendIcons.Settings, contentDescription = "Preferencias") {
                    navController.navigate(RoutersBase.Preferencias.route)
                }
            }
        },
        content = {
            Box(modifier = Modifier.padding(it)) {
                CamarerosPase(
                    vModel = vModel,
                    navController = navController
                ) {
                    app.exit()
                }

                AddCamareroDialog(vModel) { cam ->
                    vModel.showDialog = false
                    vModel.addCamarero(cam)
                }
            }
        }
    )

}


@Composable
fun CamarerosPase(
    vModel: CamarerosModel,
    navController: NavController,
    onSalir: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Selectores
        Box(modifier = Modifier.weight(0.9f)) {
            SelectoresPase(vModel = vModel)
        }

        // Botones y Floating Action Button
        Column(
            modifier = Modifier
                .weight(0.1f)
                .fillMaxHeight()
                .padding(10.dp)
        ) {
            BotonIcon(
                icon = ExtendIcons.Salir,
                color = ColorTheme.Primary,
                contentDescription = "Salir",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                onClick = {
                    onSalir()
                }
            )
            Spacer(modifier = Modifier.height(3.dp))
            BotonIcon(
                icon = ExtendIcons.Check,
                color = ColorTheme.Primary,
                contentDescription = "Aceptar",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                onClick = {
                    navController.navigate(RoutersBase.Camareros.route)
                }
            )

        }
    }
}

@Composable
fun SelectoresPase(
    vModel: CamarerosModel
) {

    val db: CamareroDao = vModel.db
    val autorizados by db.getAutorizados(autorizado = true)
        .observeAsState(initial = listOf())

    val noAutorizados by db.getAutorizados(autorizado = false)
        .observeAsState(initial = listOf())

    Row {
        Box(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
                .padding(10.dp)
        ) {
            // Primera columna
            ListaSimple(title = "Camarero Libres", list = noAutorizados) {
                vModel.setAutorizado((it as Camarero).id, true)
            }
        }

        Box(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
                .padding(10.dp)
        ) {
            // Segunda columna
            ListaSimple(title = "Camarero Pase", list = autorizados) {
                vModel.setAutorizado((it as Camarero).id, false)
            }
        }
    }


}