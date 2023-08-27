package com.valleapp.valletpv.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.valleapp.valletpvlib.db.LineaCuenta
import com.valleapp.valletpvlib.db.Tecla
import com.valleapp.valletpvlib.models.CuentaModel
import com.valleapp.valletpvlib.ui.BotonSimple
import com.valleapp.valletpvlib.ui.ListaCuenta
import com.valleapp.valletpvlib.ui.TecladoNumerico
import com.valleapp.valletpvlib.ui.ValleGridSimple
import com.valleapp.valletpvlib.ui.ValleTopBar
import com.valleapp.valletpvlib.ui.screens.BaseScreen


@Composable
fun CuentaTpvScreen(navController: NavController, camId: Long, mesaId: Long) {

    BaseScreen {

        val model: CuentaModel =
            viewModel(initializer = { CuentaModel(camId, mesaId) })

        if (it.mService != null) {
            model.loadData(it.mService!!)
        }
        var lineasCuenta: List<LineaCuenta> = listOf()
        var teclas: List<Tecla> = listOf()


        Scaffold(
            topBar = {
                ValleTopBar(
                    title = model.titulo,
                    subtitle = model.subtitulo,
                    backAction = {
                        navController.popBackStack()
                    },
                    actions = {

                    }
                )
            },
            content = {
                Box(Modifier.padding(it)) {
                    Row {
                        Column(Modifier.weight(0.4f)) {
                            Box(modifier = Modifier.weight(0.7f)) {
                                ListaCuenta("Cuenta", lineasCuenta) {
                                    println("Click en linea cuenta")
                                }
                            }
                            Box(modifier = Modifier.weight(0.3f)) {
                                TecladoNumerico(columns = 3) { can->
                                    model.cantidad = can
                                }
                            }

                        }
                        //Teclado
                        Box(modifier = Modifier.weight(0.5f)) {
                            ValleGridSimple(columns = 3) {
                                items(teclas) {
                                    BotonSimple(text = it.toString()) {
                                        println("Click en tecla")
                                    }
                                }

                            }
                        }
                        //Secciones
                        Box(modifier = Modifier.weight(0.1f)) {
                            Column {
                                for (i in 1..6) {
                                    BotonSimple(
                                        text = "Boton $i",
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(2.dp)
                                    ) {
                                        println("Click en boton $i")
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }


}


