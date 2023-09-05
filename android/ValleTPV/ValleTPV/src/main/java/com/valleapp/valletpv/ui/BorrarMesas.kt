package com.valleapp.valletpv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import com.valleapp.valletpvlib.ui.BotonIcon
import com.valleapp.valletpvlib.ui.BotonSimple
import com.valleapp.valletpvlib.ui.theme.ColorTheme
import com.valleapp.valletpvlib.ui.theme.ExtendIcons
import com.valleapp.valletpvlib.ui.theme.Styles

@Composable
fun BorrarMesa(onDismissRequest: () -> Unit, onSubmit: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Surface(color = ColorTheme.Background) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .height(250.dp)
            ) {
                Text(
                    "Borrar mesa",
                    modifier = Modifier.padding(bottom = 16.dp),
                    style = Styles.H1
                )
                Row(
                    modifier = Modifier.height(70.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextField(
                        value = reason,
                        onValueChange = { reason = it },
                        textStyle = TextStyle(fontSize = 35.sp),
                        label = { Text("Motivo") },
                        modifier = Modifier.weight(0.8f),

                    )
                    Spacer(modifier = Modifier.weight(0.05f))
                    BotonIcon(
                        icon = ExtendIcons.Check,
                        color = ColorTheme.Primary,
                        contentDescription = "Aceptar",
                        modifier = Modifier
                            .weight(0.2f),
                        onClick = { onSubmit(reason) }
                    )
                }


                Row(
                    modifier = Modifier
                        .weight(0.3f)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PreselectButton("Simpa", onClick = { onSubmit("Simpa") })
                    PreselectButton("Invitación", onClick = { onSubmit("Invitación") })
                    PreselectButton("Error pedido", onClick = { onSubmit("Error pedido") })
                }
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

