package com.valleapp.valletpv.routers

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.valleapp.valletpv.screens.CamarerosScreen
import com.valleapp.valletpv.screens.CuentaTpvScreen
import com.valleapp.valletpv.screens.MesasTpvScreen
import com.valleapp.valletpv.screens.PaseCamareros
import com.valleapp.valletpv.screens.Preferencias
import com.valleapp.valletpv.ui.InfoCobros
import com.valleapp.valletpv.ui.ModelCobros
import com.valleapp.valletpvlib.routers.RoutersBase
import com.valleapp.valletpvlib.ui.screens.BaseSecreen


@Composable
fun Navegador() {
    val navController = rememberNavController()

    BaseSecreen(navController) { bindServiceModel ->
        val modelCobros: ModelCobros = viewModel()
        NavHost(navController = navController, startDestination = Routers.PaseCamareros.route) {
            composable(Routers.PaseCamareros.route) {
                PaseCamareros(navController, bindServiceModel)
                println("Mostrando Pase Camareros")
            }
            composable( RoutersBase.Preferencias.route ) {
                val message = it.arguments?.getString("message")
                Preferencias(
                    navController,
                    bindServiceModel,
                    message = if (message != null && message != "{message}") message else "Preferecias cargadas con exito"
                )
                println("Mostrando Preferencias")
            }
            composable(RoutersBase.Camareros.route) {
                CamarerosScreen(navController, bindServiceModel)
            }
            composable(RoutersBase.Mesas.route) {
                val camId = it.arguments?.getString("camId")
                MesasTpvScreen(navController, bindServiceModel,camId?.toLong() ?: 0)
                println("Mostrando Mesas")
            }
            composable(RoutersBase.Cuenta.route) {
                val mesaId = it.arguments?.getString("mesaId")
                val camId = it.arguments?.getString("camId")
                println("Mostrando Cuenta")
                CuentaTpvScreen(navController, bindServiceModel, modelCobros, camId?.toLong() ?: 0, mesaId?.toLong() ?: 0)
            }
        }

        Box {
            InfoCobros(10000, modelCobros)
        }

    }

}

