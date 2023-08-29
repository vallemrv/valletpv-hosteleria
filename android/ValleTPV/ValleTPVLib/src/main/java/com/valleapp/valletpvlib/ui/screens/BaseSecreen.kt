package com.valleapp.valletpvlib.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valleapp.valletpvlib.models.BindServiceModel

@Composable
fun BaseSecreen(content: @Composable (BindServiceModel) -> Unit) {
    val bindServiceModel: BindServiceModel = viewModel()
    DisposableEffect(Unit){
        bindServiceModel.bindService()
        onDispose { bindServiceModel.unbindService() }
    }
    content(bindServiceModel)
}