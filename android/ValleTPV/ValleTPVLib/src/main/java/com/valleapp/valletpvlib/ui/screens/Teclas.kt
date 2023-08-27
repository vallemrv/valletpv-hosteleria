package com.valleapp.valletpvlib.ui.screens

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valleapp.valletpvlib.db.Tecla
import com.valleapp.valletpvlib.models.BindServiceModel

class TeclasModel: ViewModel(){
    fun getTeclas(): List<Tecla> {
        return listOf()
    }
}

@Composable
fun TeclasGrid(){
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val bindServiceModel: BindServiceModel = viewModel(initializer = { BindServiceModel(app) })
    val model: MesasModel = viewModel()
}
