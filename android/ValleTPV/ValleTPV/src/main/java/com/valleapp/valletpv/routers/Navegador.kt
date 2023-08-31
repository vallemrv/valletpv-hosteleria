package com.valleapp.valletpv.routers

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.valleapp.valletpv.screens.CamarerosScreen
import com.valleapp.valletpv.screens.CuentaTpvScreen
import com.valleapp.valletpv.screens.MesasTpvScreen
import com.valleapp.valletpv.screens.PaseCamareros
import com.valleapp.valletpv.screens.Preferencias
import com.valleapp.valletpvlib.routers.RoutersBase
import com.valleapp.valletpvlib.ui.screens.BaseSecreen


@Composable
fun Navegador() {
    val navController = rememberNavController()

    BaseSecreen(navController) { bindServiceModel ->

        NavHost(navController = navController, startDestination = Routers.PaseCamareros.route) {
            composable(Routers.PaseCamareros.route) {
                PaseCamareros(navController, bindServiceModel)
            }
            composable( RoutersBase.Preferencias.route ) {
                val message = it.arguments?.getString("message")
                Preferencias(
                    navController,
                    bindServiceModel,
                    message = if (message != null && message != "{message}") message else "Preferecias cargadas con exito"
                )
            }
            composable(RoutersBase.Camareros.route) {
                CamarerosScreen(navController, bindServiceModel)
            }
            composable(RoutersBase.Mesas.route) {
                val camId = it.arguments?.getString("camId")
                MesasTpvScreen(navController, bindServiceModel, camId?.toLong() ?: 0)
            }
            composable(RoutersBase.Cuenta.route) {
                val mesaId = it.arguments?.getString("mesaId")
                val camId = it.arguments?.getString("camId")
                CuentaTpvScreen(navController, bindServiceModel, camId?.toLong() ?: 0, mesaId?.toLong() ?: 0)
            }
        }

    }

}

