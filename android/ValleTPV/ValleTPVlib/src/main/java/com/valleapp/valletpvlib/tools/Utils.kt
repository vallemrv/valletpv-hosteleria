package com.valleapp.valletpvlib.tools

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.round(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return (this * factor).roundToInt() / factor
}