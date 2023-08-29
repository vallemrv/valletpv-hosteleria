package com.valleapp.valletpv.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.valleapp.valletpvlib.db.LineaCuenta
import com.valleapp.valletpvlib.models.CuentaModel
import com.valleapp.valletpvlib.ui.ListaCuenta
import com.valleapp.valletpvlib.ui.TecladoNumerico
import com.valleapp.valletpvlib.ui.ValleTopBar
import com.valleapp.valletpvlib.ui.screens.BaseSecreen
import com.valleapp.valletpvlib.ui.screens.Secciones
import com.valleapp.valletpvlib.ui.screens.TeclasGrid


@Composable
fun CuentaTpvScreen(navController: NavController, camId: Long = 0, mesaId: Long = 0) {


    BaseSecreen { bindServiceModel ->
        val model: CuentaModel = viewModel(initializer = { CuentaModel(camId, mesaId) })
        val mService by rememberUpdatedState(newValue =  bindServiceModel.mService)
        LaunchedEffect(mService) {
             model.loadData(mService)
        }

        var lineasCuenta: List<LineaCuenta> = listOf()

        Scaffold(topBar = {
            ValleTopBar(title = model.titulo, subtitle = model.subtitulo, backAction = {
                navController.popBackStack()
            }, actions = {

            })
        }, content = {
            Box(Modifier.padding(it)) {
                Row {
                    Column(Modifier.weight(0.4f)) {
                        Box(modifier = Modifier.weight(0.7f)) {
                            ListaCuenta("Cuenta", lineasCuenta) {
                                println("Click en linea cuenta")
                            }
                        }
                        Box(modifier = Modifier.weight(0.3f)) {
                            TecladoNumerico(columns = 3) { can ->
                                model.cantidad = can
                            }
                        }

                    }
                    //Teclado
                    Box(modifier = Modifier.weight(0.5f)) {
                        TeclasGrid(4, 5)
                    }

                    //Secciones
                    Box(modifier = Modifier.weight(0.1f)) {
                        Secciones()
                    }
                }
            }
        })
    }


}


