package com.valleapp.valletpvlib.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.valleapp.valletpvlib.models.BindServiceModel
import com.valleapp.valletpvlib.models.PreferenciasModel
import com.valleapp.valletpvlib.routers.RoutersBase

@Composable
fun BaseSecreen(navController: NavController, content: @Composable (BindServiceModel) -> Unit) {

    val bindServiceModel: BindServiceModel = viewModel()
    val preferenciasModel: PreferenciasModel = viewModel()

    val isAuth = bindServiceModel.isAunthValid
    val mService = bindServiceModel.mService
    val serverConfig = preferenciasModel.serverConfig

    LaunchedEffect(isAuth) {
        if (!isAuth) {
            navController.navigate(
                RoutersBase.Preferencias.route.replace(
                    "{message}",
                    "Dispositivo no autorizado"
                )
            )
        }
    }

    LaunchedEffect(serverConfig, mService) {
        if (!serverConfig.isEmpty() && mService != null) {
            mService.setServerConfig(serverConfig)
        }
    }

    DisposableEffect(Unit) {
        bindServiceModel.bindService()
        onDispose { bindServiceModel.unbindService() }
    }
    content(bindServiceModel)
}