package com.valleapp.valletpvlib.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.valleapp.valletpvlib.db.LineaCuenta
import com.valleapp.valletpvlib.models.EditLineaModel

@Composable
fun EditarPedido(
    vModel: EditLineaModel,
    showDialog: Boolean,
    onCloseDialog: () -> Unit
) {

    val pedidosActivos by vModel.pedidosActivos.observeAsState(initial = listOf())
    val pedidosEliminados by vModel.pedidosEliminados.observeAsState(initial = listOf())

    val totalActivos by  vModel.totalActivo.observeAsState(initial = 0.0)
    val totalEliminados by vModel.totalBorrado.observeAsState(initial = 0.0)

    if (showDialog){
        Box(modifier = Modifier
            .fillMaxSize(.8f)
            .padding(10.dp),
            contentAlignment = Alignment.Center
        ){
            Row {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "Total: $totalActivos")
                        ListaPedidos(title = "Pedidos Activos", list = pedidosActivos) {
                            vModel.setEliminado(it, true)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "Eliminados")
                        Text(text = "Total: $totalEliminados")
                        ListaPedidos(title = "Pedidos Eliminados", list = pedidosEliminados) {
                            vModel.setEliminado(it, false)
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun ListaPedidos(title: String, list: List<LineaCuenta>, onItemClick: (LineaCuenta) -> Unit) {
    Text(text = title)
    LazyColumn {
        items(list) { linea ->
            Row(
                modifier = Modifier.clickable { onItemClick(linea) }
            ) {

                Text(text = linea.cantidad.toString(), modifier = Modifier.weight(0.2f), textAlign = TextAlign.Right)
                Text(text = linea.descripcion, modifier = Modifier.weight(.6f), textAlign = TextAlign.Left)
                Text(text = linea.precio.toString(), modifier = Modifier.weight(.2f), textAlign = TextAlign.Right)
            }
        }
    }
}
