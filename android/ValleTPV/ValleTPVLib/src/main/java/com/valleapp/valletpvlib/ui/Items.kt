package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.valleapp.valletpvlib.db.BaseEntity
import com.valleapp.valletpvlib.db.LineaCuenta
import com.valleapp.valletpvlib.ui.theme.ColorTheme
import com.valleapp.valletpvlib.ui.theme.ExtendIcons
import com.valleapp.valletpvlib.ui.theme.Styles


@Composable
fun ListItem(item: BaseEntity, onItemClick: (BaseEntity) -> Unit) {
    Text(
        text = item.toString(),
        style = Styles.TextListas,
        modifier = Modifier
            .clickable { onItemClick(item) }
            .fillMaxWidth()
            .padding(8.dp)
            .height(70.dp)
            .background(ColorTheme.BgListas)
            .padding(start = 16.dp, top = 16.dp)
    )
}


@Composable
fun ListItemCuenta(
    lineaCuenta: LineaCuenta,
    onBorrarClicked: (LineaCuenta) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(2.dp),
         verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = lineaCuenta.cantidad.toString(),
            modifier = Modifier.weight(0.15f),
            textAlign = TextAlign.Right
        )
        Spacer(modifier = Modifier.weight(0.01f))
        Text(
            text = lineaCuenta.descripcion,
            modifier = Modifier.weight(0.4f),
            textAlign = TextAlign.Left,
        )
        Spacer(modifier = Modifier.weight(0.05f))
        Text(
            text = "${lineaCuenta.precio}€",
            modifier = Modifier.weight(0.2f),
            textAlign = TextAlign.Right
        )
        Spacer(modifier = Modifier.weight(0.05f))
        Text(
            text = "${lineaCuenta.total}€",
            modifier = Modifier.weight(0.2f),
            textAlign = TextAlign.Right
        )
        Spacer(modifier = Modifier.weight(0.05f))
             BotonIcon(
                icon = ExtendIcons.Borrar,
                color = ColorTheme.Primary,
                modifier = Modifier.size(40.dp),
                contentDescription = "Borrrar"
            ) {
                onBorrarClicked(lineaCuenta)
            }
                Spacer(modifier = Modifier.weight(0.05f))
    }
}

