package com.valleapp.valletpvlib.ui.screens

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valleapp.valletpvlib.models.BindServiceModel


@Composable
fun BaseScreen(content: @Composable (BindServiceModel) -> Unit) {

    val context = LocalContext.current
    val app = context.applicationContext as Application
    val bindServiceModel: BindServiceModel = viewModel(initializer = { BindServiceModel(app) })

    DisposableEffect(Unit) {
        bindServiceModel.bindService()
        onDispose {
            bindServiceModel.unbindService()
        }
    }
    content(bindServiceModel)
}