package com.valleapp.valletpv.routers

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.valleapp.valletpv.screens.PaseCamareros
import com.valleapp.valletpv.screens.Preferencias

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
    }
}