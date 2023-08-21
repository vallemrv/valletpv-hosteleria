package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valleapp.valletpvlib.ExtendIcons
import com.valleapp.valletpvlib.db.InfoField
import com.valleapp.valletpvlib.ui.theme.Pink00
import com.valleapp.valletpvlib.ui.theme.Pink01


@Composable
fun BotonAccion(icon: Painter, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(3.dp)
            .background(Pink01),
        contentAlignment = Alignment.CenterStart,

        ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .width(70.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                tint = Color.Black,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotonSimple(obj: InfoField, onButtonClick: (InfoField, String?) -> Unit) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
        onClick = {
            onButtonClick(obj, "main")
        },
        modifier = Modifier
            .padding(8.dp)
            .height(180.dp),
        colors = CardDefaults.elevatedCardColors(obj.color)

    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = obj.text,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(8.dp)
                    .wrapContentSize(Alignment.Center),
                lineHeight = 40.sp,
                color = Color.Black,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotonMesa(obj: InfoField, onButtonClick: (InfoField, String?) -> Unit) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
        onClick = {
            onButtonClick(obj, "main")
        },
        modifier = Modifier
            .padding(8.dp)
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = obj.color
        )

    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = obj.text,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .wrapContentSize(Alignment.Center),
                lineHeight = 40.sp,
                color = Color.Black,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
                ) {
                Icon(painter = ExtendIcons.JuntarMesa, contentDescription = "JuntarMesa",
                    modifier = Modifier
                        .padding(3.dp)
                        .size(40.dp)
                        .background(Pink00)
                        .clickable { onButtonClick(obj, "juntar") })
                Icon(painter = ExtendIcons.CambiarMesa, contentDescription = "CambiarMesa",
                    modifier = Modifier
                        .padding(3.dp)
                        .size(40.dp)
                        .background(Pink00)
                        .clickable { onButtonClick(obj, "cambiar") })
                Icon(painter = ExtendIcons.Borrar, contentDescription = "BorrarMesa",
                    modifier = Modifier
                        .padding(3.dp)
                        .size(40.dp)
                        .background(Pink00)
                        .clickable { onButtonClick(obj, "borrar") })
            }
        }

    }
}
