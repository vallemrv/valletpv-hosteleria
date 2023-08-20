package com.valleapp.valletpv.routers

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.valleapp.valletpv.screens.MesasTpvScreen
import com.valleapp.valletpv.screens.PaseCamareros
import com.valleapp.valletpv.screens.Preferencias
import com.valleapp.valletpvlib.routers.RoutersBase
import com.valleapp.valletpvlib.ui.screens.CamarerosGrid

@Composable
fun Navegador() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routers.PaseCamareros.route) {
        composable(Routers.PaseCamareros.route) {
            PaseCamareros(navController)
        }
        composable(Routers.Preferencias.route) {
            Preferencias(navController)
        }
        composable(RoutersBase.Camareros.route) {
            CamarerosGrid(navController)
        }
        composable(RoutersBase.Mesas.route) {
            val camid = it.arguments?.getString("camId") ?: "0"
            MesasTpvScreen(navController, camid.toLong())
        }
        composable(RoutersBase.Cuenta.route) {
            Text(text = "Cuenta")
        }
    }
}