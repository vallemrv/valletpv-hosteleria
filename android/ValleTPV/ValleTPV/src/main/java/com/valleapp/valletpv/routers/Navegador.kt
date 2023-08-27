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

@Composable
fun Navegador() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routers.PaseCamareros.route) {
        composable(Routers.PaseCamareros.route) {
            PaseCamareros(navController)
        }
        composable(RoutersBase.Preferencias.route) {
            Preferencias(navController)
        }
        composable(RoutersBase.Camareros.route) {
            CamarerosScreen(navController)
        }
        composable(RoutersBase.Mesas.route) {
            val camid = it.arguments?.getString("camId") ?: "0"
            MesasTpvScreen(navController, camid.toLong())
        }
        composable(RoutersBase.Cuenta.route) {
            val camid = it.arguments?.getString("camId") ?: "0"
            val mesaId = it.arguments?.getString("mesaId") ?: "0"
            CuentaTpvScreen(navController, camid.toLong(), mesaId.toLong())
        }
    }
}