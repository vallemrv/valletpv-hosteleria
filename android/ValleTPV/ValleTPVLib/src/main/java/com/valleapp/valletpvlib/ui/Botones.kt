package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.valleapp.valletpvlib.db.AccionMesa
import com.valleapp.valletpvlib.db.Mesa
import com.valleapp.valletpvlib.ui.theme.ColorTheme
import com.valleapp.valletpvlib.ui.theme.ExtendIcons
import com.valleapp.valletpvlib.ui.theme.Styles

@Composable
fun BotonAccion(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit
) {
    BotonIcon(
        modifier = Modifier
            .padding(2.dp)
            .size(60.dp),
        icon = icon,
        color = ColorTheme.BotonesAccion,
        contentDescription = contentDescription
    ) {
        onClick()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotonIcon(
    modifier: Modifier = Modifier,
    url: String? = null,
    icon: Painter = ExtendIcons.NotFound,
    color: Color = ColorTheme.Primary,
    contentDescription: String,
    onClick: () -> Unit
) {

    val defaultModifier = if (modifier == Modifier) {
        Modifier
            .padding(2.dp)
            .size(70.dp)
    } else {
        modifier
    }
    Card(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
        onClick = onClick,
        modifier = defaultModifier,
        colors = CardDefaults.elevatedCardColors(color)

    ) {
        Box(
            modifier = Modifier.padding(3.dp),
            contentAlignment = Alignment.Center,

            ) {
            if (url != null) {
                NetworkImage(url = url, contentDescription = contentDescription)
            } else {
                Icon(
                    painter = icon,
                    contentDescription = contentDescription,
                    tint = Color.Black,
                    modifier = Modifier.fillMaxSize()
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotonSimple(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = ColorTheme.Primary,
    tag: Any? = null,
    onButtonClick: (Any?) -> Unit
) {
    val defaultModifier = if (modifier == Modifier) {
        Modifier
            .padding(2.dp)
            .size(170.dp)
    } else {
        modifier
    }

    Card(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
        onClick = {
            onButtonClick(tag)
        },
        modifier = defaultModifier,
        colors = CardDefaults.elevatedCardColors(color)

    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(8.dp)
                    .wrapContentSize(Alignment.Center),
                lineHeight = 40.sp,
                color = Color.Black,
                style = Styles.TextBotones,
                textAlign = TextAlign.Center
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotonMesa(
    mesa: Mesa,
    modifier: Modifier = Modifier,
    onButtonClick: (Mesa) -> Unit = {},
    onAccionClick: (Mesa, AccionMesa) -> Unit

) {

    val defaultModifier = if (modifier == Modifier) {
        Modifier
            .padding(2.dp)
            .size(170.dp)
    } else {
        modifier
    }
    Card(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
        onClick = {
            onButtonClick(mesa)
        },
        modifier = defaultModifier,

        colors = CardDefaults.cardColors(
            containerColor = ColorTheme.hexToComposeColor(mesa.color)
        )

    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mesa.nombre,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .wrapContentSize(Alignment.Center),
                lineHeight = 40.sp,
                color = Color.Black,
                style = Styles.TextBotones,
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
                        .background(ColorTheme.Primary)
                        .clickable { onAccionClick(mesa, AccionMesa.JUNTAR) })
                Icon(painter = ExtendIcons.CambiarMesa, contentDescription = "CambiarMesa",
                    modifier = Modifier
                        .padding(3.dp)
                        .size(40.dp)
                        .background(ColorTheme.Primary)
                        .clickable { onAccionClick(mesa, AccionMesa.MOVER) })
                Icon(painter = ExtendIcons.Borrar, contentDescription = "BorrarMesa",
                    modifier = Modifier
                        .padding(3.dp)
                        .size(40.dp)
                        .background(ColorTheme.Primary)
                        .clickable { onAccionClick(mesa, AccionMesa.BORRAR) })
            }
        }

    }
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun NetworkImage(url: String, contentDescription: String = "") {
    val painter = rememberImagePainter(
        data = url,
        builder = {
            crossfade(true)
        }
    )
    Image(
        painter = painter,
        contentDescription = contentDescription,  // Añadir una descripción adecuada para accesibilidad
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}
