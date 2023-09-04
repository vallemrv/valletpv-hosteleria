package com.valleapp.valletpv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valleapp.valletpv.models.ModelCobros
import kotlinx.coroutines.delay


@Composable
fun InfoCobros(
    timeout: Long,
    model: ModelCobros,
    fontSize: TextUnit = 25.sp,
) {

    if (model.showMostrarInfo) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Surface(
                color = Color.Gray,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .width(400.dp)
                    .align(Alignment.TopCenter)
                    .alpha(0.9f)

            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Text(
                            text = String.format("Total:"),
                            color = Color.White,
                            fontSize = fontSize,
                            modifier = Modifier.weight(.6f)
                        )
                        Text(
                            text = String.format("%.2f €", model.total),
                            color = Color.White,
                            fontSize = fontSize,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.weight(.4f)
                        )
                    }
                    Row {
                        Text(
                            text = if (model.entregado == 0.0) "Pagado con tarjeta" else "Entregado:",
                            color = Color.White,
                            fontSize = fontSize,
                            modifier = Modifier.weight(.6f)
                        )

                        Text(
                            text = if (model.entregado == 0.0) "" else String.format(
                                "%.2f €",
                                model.entregado
                            ),
                            color = Color.White,
                            fontSize = fontSize,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.weight(.4f)
                        )
                    }


                    if (model.entregado > 0.0) {
                        Row {
                            Text(
                                text = "Cambio:",
                                color = Color.Red,
                                fontSize = 30.sp,
                                modifier = Modifier.weight(.6f)
                            )
                            Text(
                                text = String.format("%.2f €", model.cambio),
                                color = Color.Red,
                                fontSize = 30.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.weight(.4f)
                            )
                        }
                    }
                }

            }
        }

        // Usando LaunchedEffect para agregar un delay
        LaunchedEffect(key1 = model.showMostrarInfo) {
            delay(timeout)
            model.showMostrarInfo = false

        }
    }
}
