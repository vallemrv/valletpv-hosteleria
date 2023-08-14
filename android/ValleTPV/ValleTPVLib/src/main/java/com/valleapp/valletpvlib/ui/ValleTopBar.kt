package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val NoOp: () -> Unit = {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValleTopBar(
    title: String,
    backgroundColor: Color = Color(0xFFBD98E7),
    backAction: () -> Unit = NoOp,
    actions: @Composable () -> Unit = {},
) {
    val isBackActionEmpty = backAction === NoOp
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
          ),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {

                Text(
                    text = title,
                    style = TextStyle(fontSize = 20.sp, color = Color.Black),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        actions = { actions() },
        navigationIcon = {
            if( !isBackActionEmpty) {
                IconButton(onClick = backAction) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back Button",
                        tint = Color.Black
                    )
                }
            }
        }
    )
}


@Preview
@Composable
fun CustomNavigationBarPreview() {
    ValleTopBar(
        title = "Preferencias",
    ){
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Back Button",
                tint = Color.Black
            )
        }

        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Back Button",
                tint = Color.Black
            )
        }
    }
}
