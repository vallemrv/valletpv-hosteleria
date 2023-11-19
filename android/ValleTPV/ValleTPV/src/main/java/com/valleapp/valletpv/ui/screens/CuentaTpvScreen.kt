package com.valleapp.valletpv.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.room.util.convertUUIDToByte
import com.valleapp.valletpv.models.ModelCobros
import com.valleapp.valletpv.ui.dialog.BorrarMesa
import com.valleapp.valletpv.ui.dialog.CobrarMesaDialog
import com.valleapp.valletpvlib.ValleApp
import com.valleapp.valletpvlib.db.TeclasDao
import com.valleapp.valletpvlib.models.CuentaModel
import com.valleapp.valletpvlib.models.EditLineaModel
import com.valleapp.valletpvlib.models.TeclasModel
import com.valleapp.valletpvlib.ui.BotonAccion
import com.valleapp.valletpvlib.ui.ListaCuenta
import com.valleapp.valletpvlib.ui.TecladoNumerico
import com.valleapp.valletpvlib.ui.ValleTopBar
import com.valleapp.valletpvlib.ui.dialog.EditarPedido
import com.valleapp.valletpvlib.ui.screens.SearchView
import com.valleapp.valletpvlib.ui.screens.Secciones
import com.valleapp.valletpvlib.ui.screens.TeclasGrid
import com.valleapp.valletpvlib.ui.theme.ExtendIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun CuentaTpvScreen(
    navController: NavController,
    mCobros: ModelCobros,
    camId: Long = 0,
    mesaId: Long = 0
) {

    val coroutineScope = rememberCoroutineScope()

    val app = LocalContext.current.applicationContext as ValleApp
    val mainModel = app.mainModel

    val modelMesas: TeclasModel = viewModel(initializer = { TeclasModel(mainModel.getDB("teclas") as TeclasDao) })
    val modelEditCuenta: EditLineaModel = viewModel(initializer = { EditLineaModel(mainModel) })
    val model: CuentaModel = viewModel(initializer = { CuentaModel(mainModel, camId, mesaId) })
    val titulo by model.titulo.collectAsState()

    var showCobrarDialog by remember { mutableStateOf(false) }

    var titleEditarCuenta by remember { mutableStateOf("Total a borrar") }
    var isBorrar by remember { mutableStateOf(false) }

    val totalTicket by model.total.collectAsState()
    var totalACobrar by remember {
        mutableDoubleStateOf(0.0)
    }

    var showEditarCuenta by remember {
        mutableStateOf(false)
    }
    var showMotivoDialog by remember {
        mutableStateOf(false)
    }

    val tarifa by model.tarifa.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            model.hacerPedido()
        }
    }

    val lineasCuenta by model.lineasDao.getLineasCuenta(mesaId).observeAsState(initial = listOf())


    Scaffold(topBar = {
        ValleTopBar(title = titulo , subtitle = model.subtitulo, backAction = {
            navController.popBackStack()
        }, actions = {
            BotonAccion(
                icon = ExtendIcons.Varios,
                contentDescription = "Varios") {

            }
            BotonAccion(
                icon = ExtendIcons.Borrar,
                contentDescription = "Modificar cuenta") {
                model.hacerPedido()
                coroutineScope.launch {
                    delay(100)
                    if (totalTicket> 0.0) {
                        modelEditCuenta.inicializar(lineasCuenta)
                        titleEditarCuenta = "Total a borrar"
                        isBorrar = true
                        showEditarCuenta = true
                    }
                }
            }
            BotonAccion(icon = ExtendIcons.DividirCuenta, contentDescription = "Dividir cuenta") {
                model.hacerPedido()
                coroutineScope.launch {
                    delay(100)
                    if (totalTicket > 0.0) {
                        modelEditCuenta.inicializar(lineasCuenta)
                        titleEditarCuenta = "Total a dividir"
                        isBorrar = false
                        showEditarCuenta = true
                    }
                }
            }
            BotonAccion(icon = ExtendIcons.Imprimir, contentDescription = "Imprimir ticket") {
                mainModel.preImprimir(mesaId)
            }

            BotonAccion(icon = ExtendIcons.Cobrar, contentDescription = "Cobrar efectivo") {
                model.hacerPedido()
                coroutineScope.launch {
                    delay(100)
                    if (totalTicket > 0.0) {
                        totalACobrar = totalTicket
                        showCobrarDialog = true
                    }
                }
            }
            BotonAccion(icon = ExtendIcons.AbrirCaja, contentDescription = "Abrir cajon") {
                mainModel.abrirCajon()
            }

        })
    }, content = {
        Box(Modifier.padding(it)) {
            Row {
                Column(Modifier.weight(0.4f)) {
                    Box(modifier = Modifier.weight(0.7f)) {
                        ListaCuenta("Cuenta", lineasCuenta)
                    }
                    Box(modifier = Modifier.weight(0.3f)) {
                        TecladoNumerico(columns = 3) { can ->
                            model.cantidad = can.toInt()
                        }
                    }

                }
                //Teclado
                Box(modifier = Modifier.weight(0.5f)) {
                    TeclasGrid(3, 6, tarifa){ lineaPedido ->
                        model.addLinea(lineaPedido)
                        model.cantidad = 1
                    }
                }

                //Secciones
                Box(modifier = Modifier.weight(0.1f)) {
                    Secciones{}
                }
            }

            CobrarMesaDialog(totalACobrar, showCobrarDialog) { totalCobrado, entregado ->
                if (totalCobrado != null && entregado != null) {
                    mCobros.mostrarInfo(totalCobrado, entregado, entregado - totalCobrado)
                    if (totalCobrado == totalTicket) {
                        model.cobrarMesa(entregado, lineasCuenta)
                        navController.popBackStack()
                    }else{
                        model.cobrarMesa(entregado, modelEditCuenta.lineasEditadas.value ?: listOf())
                    }

                }
                showCobrarDialog = false
            }


            EditarPedido(modelEditCuenta,
                title= titleEditarCuenta,
                showDialog = showEditarCuenta
            ){ modificar->
                if (modificar) {
                    totalACobrar = modelEditCuenta.totalEditado.value!!
                    showMotivoDialog = isBorrar
                    showCobrarDialog = !isBorrar
                }
                showEditarCuenta = false
            }

            BorrarMesa(showDialog = showMotivoDialog,
                title = "Borrar lineas",
                onDismissRequest = { showMotivoDialog=false },
                onSubmit = {motivo->
                    showMotivoDialog=false
                    modelEditCuenta.ejecutarBorrado(motivo, mesaId, camId)
                })
        }
    })

    SearchView(modelMesas.showSearch) {
        if (it.isEmpty()){
            modelMesas.cargarTeclasBySeccion(modelMesas.getSeccionId())
            modelMesas.showSearch = false
        }else {
            modelMesas.cargarTeclasByStr(it)
        }
    }

}

