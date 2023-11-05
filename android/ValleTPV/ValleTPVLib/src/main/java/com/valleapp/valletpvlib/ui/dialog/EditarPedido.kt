package com.valleapp.valletpvlib.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valleapp.valletpvlib.models.EditLineaModel
import com.valleapp.valletpvlib.ui.BotonIcon
import com.valleapp.valletpvlib.ui.ListItemCuenta
import com.valleapp.valletpvlib.ui.ValleList
import com.valleapp.valletpvlib.ui.theme.ExtendIcons

@Composable
fun EditarPedido(
    vModel: EditLineaModel,
    showDialog: Boolean,
    title: String,
    onCloseDialog: (Boolean) -> Unit
) {

    val lineasTicket by vModel.lineasTicket.observeAsState(initial = listOf())
    val lineasModificadas by vModel.lineasEditadas.observeAsState(initial = listOf())

    val totalActivos by vModel.totalTicket.observeAsState(initial = 0.0)
    val totalModificado by vModel.totalEditado.observeAsState(initial = 0.0)

    BaseDialog(showDialog = showDialog, modifier = Modifier.fillMaxSize(.8f)) {

        Row(modifier= Modifier.padding(9.dp) ) {
            Box(modifier = Modifier.weight(.45f)) {
                ValleList(title = "Total ticket: ${String.format("%.2f", totalActivos)} €") {
                    items(lineasTicket) {
                        Box(modifier = Modifier.clickable { vModel.setEliminado(it, true) }){
                            ListItemCuenta(lineaCuenta = it)
                        }
                    }
                }
            }
            Box(modifier = Modifier.weight(.45f)) {
                ValleList(title = "$title  ${String.format("%.2f", totalModificado)} €") {
                    items(lineasModificadas) {
                        Box(modifier = Modifier.clickable { vModel.setEliminado(it, false) }) {
                            ListItemCuenta(lineaCuenta = it)
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(.1f)) {
                BotonIcon(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentDescription = "Salir", icon = ExtendIcons.Salir
                ) {
                    onCloseDialog(false)
                }
                Spacer(modifier = Modifier.height(12.dp))
                BotonIcon(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),

                    contentDescription = "Aceptar", icon = ExtendIcons.Check) {
                    onCloseDialog(true)
                }
            }
        }
    }
}

