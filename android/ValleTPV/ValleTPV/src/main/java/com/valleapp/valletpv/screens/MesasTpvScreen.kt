package com.valleapp.valletpv.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.valleapp.valletpvlib.ui.BotonAccion
import com.valleapp.valletpvlib.ui.ValleTopBar
import com.valleapp.valletpvlib.ui.screens.MesasGrid
import com.valleapp.valletpvlib.ui.theme.ExtendIcons

@Composable
fun MesasTpvScreen(navController: NavController, camId: Long) {
    Scaffold(
        topBar = {
            ValleTopBar(title = "Mesas",
                backAction = {
                    navController.popBackStack()
                }
            ) {
                BotonAccion(icon = ExtendIcons.Mensajes, contentDescription = "Mesajes") {

                }
                BotonAccion(
                    ExtendIcons.AddCamareros, "Agregar camareors",
                    onClick = {  })

                BotonAccion(
                    ExtendIcons.Configuration, "Impresoras",
                    onClick = {  })

                BotonAccion(
                    ExtendIcons.Listado,
                    "Camareros",
                    onClick = {  })

                BotonAccion(icon = ExtendIcons.AbrirCaja, contentDescription = "Abrir cajon") {

                }

            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            MesasGrid(navController = navController, camId = camId)
        }
    }
}