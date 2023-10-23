package com.valleapp.valletpv.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valleapp.valletpvlib.models.MainModel
import com.valleapp.valletpvlib.tools.LineasTicket
import com.valleapp.valletpvlib.tools.Ticket
import com.valleapp.valletpvlib.ui.BotonAccion
import com.valleapp.valletpvlib.ui.dialog.BaseDialog
import com.valleapp.valletpvlib.ui.theme.ColorTheme
import com.valleapp.valletpvlib.ui.theme.ExtendIcons
import com.valleapp.valletpvlib.ui.theme.Styles
import kotlinx.coroutines.launch

@Composable
fun ListadoTicket(mainModel: MainModel, show: Boolean, onClickSalir: () -> Unit) {


    var selectedTicket by remember { mutableStateOf<Ticket?>(null) }
    var offset by remember { mutableIntStateOf(0) }
    var listaTicket by remember { mutableStateOf(listOf<Ticket>()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasMoreItems by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var lineas by remember { mutableStateOf(listOf<LineasTicket>()) }

    LaunchedEffect(Unit) {
        if (listaTicket.isEmpty()) {
            isLoading = true
            listaTicket = mainModel.getListaTicket(offset)
            isLoading = false
            offset += 10
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex == listState.layoutInfo.totalItemsCount - 1) {
                    isLoading = true
                    val newTickets = mainModel.getListaTicket(offset)
                    if (newTickets.size < 10) {
                        hasMoreItems = false
                    }
                    listaTicket = listaTicket + newTickets
                    isLoading = false
                    if (hasMoreItems) {
                        offset += 10
                    }
                }
            }
    }

    BaseDialog(modifier = Modifier.fillMaxSize(.9f), show) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "Listado de tickets", style = Styles.H1)
            Spacer(modifier = Modifier.padding(8.dp))
            Row(
                modifier = Modifier
                    .weight(.8f)
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(.5f)
                    ) {
                        items(listaTicket) { ticket ->
                            CabeceraTicket(ticket = ticket, isSel = ticket == selectedTicket) {
                                selectedTicket = ticket
                                coroutineScope.launch {
                                    lineas = mainModel.getLineasTicket(ticket.id)
                                }
                            }
                            Spacer(modifier = Modifier.padding(8.dp))
                        }
                    }
                }

                Box(modifier = Modifier.weight(.5f)) {
                    TicketDetalle(
                        lineas = lineas, // Asegúrate de obtener 'lineas' de algún lugar
                        ticket = selectedTicket
                    )
                }
            }
            Row(
                modifier = Modifier
                    .weight(.2f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (selectedTicket != null) {
                    BotonAccion(
                        icon = ExtendIcons.ImprimirFactura,
                        contentDescription = "Factura",
                    ) {
                        mainModel.imprimirFactura(selectedTicket)
                    }
                    BotonAccion(
                        icon = ExtendIcons.Imprimir,
                        contentDescription = "Imprimir"
                    ) {
                        mainModel.imprimirTicket(selectedTicket)
                    }

                }
                BotonAccion(
                    icon = ExtendIcons.Salir,
                    contentDescription = "Salir",
                    onClick = onClickSalir
                )


            }
        }
    }
}


@Composable
fun CabeceraTicket(ticket: Ticket, isSel: Boolean, onClick: (Ticket) -> Unit = {}) {
    Column(
        modifier = Modifier
            .background(if (isSel) Color.Red else ColorTheme.Primary)
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick(ticket) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Ticket: ${ticket.id}", style = Styles.TextListas)
            Text(text = ticket.camarero, style = Styles.TextListas)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Mesa: ${ticket.nomMesa}", style = Styles.TextListas)
            Text(text = "Hora: ${ticket.hora}", style = Styles.TextListas)

        }
        Text(text = "Total: ${String.format("%.2f€", ticket.total)}", style = Styles.TextListas)
    }
}

@Composable
fun TicketDetalle(lineas: List<LineasTicket>, ticket: Ticket?) {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        item {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ticket?.let {
                    Text(
                        "${it.fecha}  --  ${it.hora} ",
                        style = Styles.H3,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Mesa: ${it.nomMesa}",
                        style = Styles.H3,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        it.camarero,
                        style = Styles.H3,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (linea in lineas) {
                        Row {
                            Text(
                                text = linea.cantidad.toString(),
                                style = Styles.TextListas,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.weight(.2f)
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = linea.descripcion,
                                style = Styles.TextListas,
                                textAlign = TextAlign.Left,
                                modifier = Modifier.weight(.5f)
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = String.format("%.2f €", linea.total),
                                style = Styles.TextListas,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.weight(.3f)
                            )
                        }

                        Spacer(modifier = Modifier.padding(8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.padding(28.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column {
                    ticket?.let {
                        Text(
                            text = "Total: ${String.format("%.2f €", it.total)}",
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 40.sp),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = if (it.entrega == 0.0) "Pagado con tarjeta" else "Entregado: ${
                                String.format(
                                    "%.2f €",
                                    it.entrega
                                )
                            }",
                            textAlign = TextAlign.End,
                            style = Styles.TextListas,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (it.entrega != 0.0) {
                            Text(
                                text = "Cambio: ${String.format("%.2f €", it.entrega - it.total)}",
                                textAlign = TextAlign.End,
                                style = Styles.TextListas,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

        }
    }

}
