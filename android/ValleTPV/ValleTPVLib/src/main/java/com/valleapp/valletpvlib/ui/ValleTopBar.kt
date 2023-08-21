package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valleapp.valletpvlib.ExtendIcons
import com.valleapp.valletpvlib.ui.theme.Pink00


val NoOp: () -> Unit = {}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValleTopBar(
    title: String,
    backgroundColor: Color = Pink00,
    backAction: () -> Unit = NoOp,
    actions: @Composable () -> Unit = {},
) {
    val isBackActionEmpty = backAction === NoOp
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
        ),
        modifier = Modifier.height(100.dp),
        title = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = title,
                    style = TextStyle(fontSize = 20.sp, color = Color.Black),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row {
                    actions()
                }

            }
        },
        navigationIcon = {
            if (!isBackActionEmpty) {
                BotonAccion(
                    icon = ExtendIcons.Back,
                    contentDescription = "Back Button",
                    onClick = backAction
                )
            }
        }
    )
}


@Preview
@Composable
fun CustomNavigationBarPreview() {
    ValleTopBar(
        title = "Preferencias",
    ) {

        BotonAccion(icon = ExtendIcons.AbrirCaja, contentDescription = "Abrir cajon") {

        }

        BotonAccion(icon = ExtendIcons.AddCamareros, contentDescription = "Abrir cajon") {

        }

    }
}
