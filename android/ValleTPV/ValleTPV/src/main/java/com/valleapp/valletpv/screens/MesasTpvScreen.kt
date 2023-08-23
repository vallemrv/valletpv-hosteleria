package com.valleapp.valletpv.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.valleapp.valletpv.routers.Routers
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
                    navController.navigate(Routers.Preferencias.route)
                }
                BotonAccion(
                    ExtendIcons.AddCamareros, "Agregar camareors",
                    onClick = { navController.navigate(Routers.Preferencias.route) })

                BotonAccion(
                    ExtendIcons.Configuration, "Impresoras",
                    onClick = { navController.navigate(Routers.Preferencias.route) })

                BotonAccion(
                    ExtendIcons.Listado,
                    "Camareros",
                    onClick = { navController.navigate(Routers.Preferencias.route) })

                BotonAccion(icon = ExtendIcons.AbrirCaja, contentDescription = "Abrir cajon") {
                    navController.navigate(Routers.Preferencias.route)
                }

            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            MesasGrid(navController = navController, camId = camId)
        }
    }
}