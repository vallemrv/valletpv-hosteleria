package com.valleapp.valletpv.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valleapp.valletpvlib.ui.BotonIcon
import com.valleapp.valletpvlib.ui.BotonSimple
import com.valleapp.valletpvlib.ui.dialog.BaseDialog
import com.valleapp.valletpvlib.ui.theme.ColorTheme
import com.valleapp.valletpvlib.ui.theme.ExtendIcons
import com.valleapp.valletpvlib.ui.theme.Styles

@Composable
fun BorrarMesa(showDialog: Boolean, title: String="Borrar mesa", onDismissRequest: () -> Unit, onSubmit: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }

    BaseDialog(showDialog = showDialog, modifier = Modifier.fillMaxHeight(.6f).fillMaxWidth(.5f)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Row(modifier = Modifier
                .weight(.3f)
                .background(ColorTheme.Background)
                .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .weight(.8f),
                    style = Styles.H1
                )
                Box(
                    modifier = Modifier.weight(.2f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    BotonIcon(
                        contentDescription = "Cancelar",
                        icon = ExtendIcons.Cerrar,
                        onClick = {
                            onDismissRequest()
                        })
                }
            }

            Row(
                modifier = Modifier.weight(.3f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextField(
                    value = reason,
                    onValueChange = { reason = it },
                    textStyle = TextStyle(fontSize = 35.sp),
                    label = { Text("Motivo") },
                    modifier = Modifier.weight(0.8f),

                    )
                Box(
                    modifier = Modifier
                        .weight(0.2f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    BotonIcon(
                        icon = ExtendIcons.Check,
                        color = ColorTheme.Primary,
                        contentDescription = "Aceptar",
                        onClick = { onSubmit(reason) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.05f))
            Row(
                modifier = Modifier
                    .weight(0.4f)
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                PreselectButton("Simpa", onClick = { onSubmit("Simpa") })
                PreselectButton("Invitación", onClick = { onSubmit("Invitación") })
                PreselectButton("Error pedido", onClick = { onSubmit("Error pedido") })
            }
        }
    }
}


@Composable
fun PreselectButton(text: String, onClick: () -> Unit) {
    BotonSimple(
        text = text,
        color = ColorTheme.Primary,
        style = Styles.TextTeclas,
    ) {
        onClick()
    }
}

