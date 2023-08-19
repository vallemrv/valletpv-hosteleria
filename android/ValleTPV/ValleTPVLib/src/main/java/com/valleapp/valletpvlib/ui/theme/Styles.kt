package com.valleapp.valletpvlib.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val ValleTypography = Typography(
    labelLarge = TextStyle(
        fontSize = 40.sp,
        letterSpacing = (-1.5).sp
    ),
    labelMedium = TextStyle(
        fontSize = 30.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontSize = 20.sp,
        letterSpacing = 0.sp
    ),
)
val ValleShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(0.dp),
)