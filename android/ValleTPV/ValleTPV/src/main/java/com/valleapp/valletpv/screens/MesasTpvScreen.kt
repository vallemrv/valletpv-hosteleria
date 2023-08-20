package com.valleapp.valletpv.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.valleapp.valletpvlib.ui.ValleTopBar
import com.valleapp.valletpvlib.ui.screens.MesasGrid

@Composable
fun MesasTpvScreen(navController: NavController, camId: Long){
    Scaffold (
        topBar = {
            ValleTopBar(title = "Mesas") {

            }
        }
    ){
        Box(modifier = Modifier.padding(it)){
            MesasGrid(navController = navController, camId = camId )
        }
    }
}