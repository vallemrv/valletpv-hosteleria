package com.valleapp.valletpv.routers

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.valleapp.valletpv.models.ModelCobros
import com.valleapp.valletpv.ui.screens.CamarerosPaseScreen
import com.valleapp.valletpv.ui.screens.CamarerosScreen
import com.valleapp.valletpv.ui.screens.CuentaTpvScreen
import com.valleapp.valletpv.ui.screens.MesasTpvScreen
import com.valleapp.valletpv.ui.screens.Preferencias
import com.valleapp.valletpv.ui.InfoCobros
import com.valleapp.valletpvlib.routers.RoutersBase


@Composable
fun Navegador() {
    val navController = rememberNavController()
    val modelCobros: ModelCobros = viewModel()

    NavHost(navController = navController, startDestination = Routers.PaseCamareros.route) {
        composable(Routers.PaseCamareros.route) {
            CamarerosPaseScreen(navController)
        }
        composable(RoutersBase.Preferencias.route) {
            val message = it.arguments?.getString("message")
            Preferencias(
                navController,
                message = if (message != null && message != "{message}") message else "Preferecias cargadas con exito"
            )
        }
        composable(RoutersBase.Camareros.route) {
            CamarerosScreen(navController)
        }
        composable(RoutersBase.Mesas.route) {
            val camId = it.arguments?.getString("camId")
            MesasTpvScreen(navController, camId?.toLong() ?: 0)

        }
        composable(RoutersBase.Cuenta.route) {
            val mesaId = it.arguments?.getString("mesaId")
            val camId = it.arguments?.getString("camId")
            CuentaTpvScreen(
                navController,
                modelCobros,
                camId?.toLong() ?: 0,
                mesaId?.toLong() ?: 0
            )
        }
    }

    Box {
        InfoCobros(10000, modelCobros)
    }



}

