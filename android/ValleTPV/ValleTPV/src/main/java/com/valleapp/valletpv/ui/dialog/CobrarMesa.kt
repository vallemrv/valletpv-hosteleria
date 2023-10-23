package com.valleapp.valletpv.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.valleapp.valletpvlib.models.CuentaModel
import com.valleapp.valletpvlib.ui.BotonIcon
import com.valleapp.valletpvlib.ui.TecladoNumerico
import com.valleapp.valletpvlib.ui.dialog.BaseDialog
import com.valleapp.valletpvlib.ui.theme.ColorTheme
import com.valleapp.valletpvlib.ui.theme.ExtendIcons
import com.valleapp.valletpvlib.ui.theme.Styles

@Composable
fun CobrarMesaDialog(
    modelCuenta: CuentaModel,
    showDialog: Boolean,
    onDismiss: (Double?, Double?) -> Unit
) {

    val total by modelCuenta.total.collectAsState()
    var entregado by remember { mutableStateOf("") }
    var cambio by remember { mutableDoubleStateOf(0.0) }


    BaseDialog(showDialog = showDialog, modifier = Modifier.fillMaxWidth(0.65f).fillMaxHeight(.85f)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Cobrar mesa",
                modifier = Modifier.padding(bottom = 16.dp),
                style = Styles.H1
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.weight(0.7f)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Column(modifier = Modifier.weight(0.7f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                "Total: ",
                                style = Styles.TextTeclas,
                                modifier = Modifier.weight(0.6f)
                            )
                            Text(
                                String.format("%.2f €", total),
                                style = Styles.TextTeclas,
                                modifier = Modifier.weight(0.4f),
                                textAlign = TextAlign.Right
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                "Entregado: ",
                                style = Styles.TextTeclas,
                                modifier = Modifier.weight(0.6f)
                            )
                            Text(
                                String.format(
                                    "%.2f €", entregado.toDoubleOrNull() ?: 0.0
                                ),
                                style = Styles.TextTeclas,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.weight(0.4f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Cambio: ",
                                style = Styles.TextTeclas,
                                modifier = Modifier.weight(0.6f)
                            )
                            Text(
                                if (cambio > 0) String.format(
                                    "%.2f €", cambio
                                ) else "",
                                style = Styles.TextTeclas,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.weight(0.4f)
                            )
                        }
                    }


                    Row(
                        modifier = Modifier
                            .weight(0.3f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        BotonIcon(
                            icon = ExtendIcons.Tarjeta,
                            contentDescription = "Tarjeta",
                            color = ColorTheme.Primary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            if (entregado.isNotEmpty()) return@BotonIcon
                            val entregadoDouble = 0.0
                            modelCuenta.cobrarMesa(entregadoDouble)
                            onDismiss(total, entregadoDouble)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        BotonIcon(
                            icon = ExtendIcons.Efectivo,
                            contentDescription = "Efectivo",
                            color = ColorTheme.Primary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            if (entregado.isEmpty()) entregado = total.toString()
                            if (cambio < 0) return@BotonIcon
                            val entregadoDouble = entregado.toDoubleOrNull() ?: 0.0
                            modelCuenta.cobrarMesa(entregadoDouble)
                            onDismiss(total, entregadoDouble)

                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        BotonIcon(
                            icon = ExtendIcons.Salir,
                            contentDescription = "Salir",
                            color = ColorTheme.Primary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            onDismiss(null, null)
                        }
                    }

                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(0.5f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TecladoNumerico(tipoCal = true) { key ->
                        if (key == "C") {
                            entregado = ""
                            cambio = 0.0
                            return@TecladoNumerico
                        } else if (key == "." && entregado.contains(".")) {
                            return@TecladoNumerico
                        } else if (key == "." && entregado.isEmpty()) {
                            entregado = "0."
                        } else {
                            entregado += key
                        }
                        cambio =
                            (entregado.toDoubleOrNull() ?: 0.0) - total

                    }
                }
            }
        }
    }

}




