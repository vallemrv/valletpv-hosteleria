package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valleapp.valletpvlib.ui.theme.ColorTheme
import com.valleapp.valletpvlib.ui.theme.ExtendIcons
import com.valleapp.valletpvlib.ui.theme.Styles


val NoOp: (Any?) -> Unit = {}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValleTopBar(
    title: String,
    subtitle: String? = null,
    backgroundColor: Color = ColorTheme.Primary,
    backAction: (Any?) -> Unit = NoOp,
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
                Column {
                    Text(
                        text = title,
                        style = Styles.TextTitulos,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = Styles.TextSubTitulos,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

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
                BotonIcon(
                    icon = ExtendIcons.Back,
                    contentDescription = "Back Button",
                    color = ColorTheme.Primary,
                    modifier = Modifier
                        .padding(top = 20.dp, start = 5.dp)
                        .size(60.dp),
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
