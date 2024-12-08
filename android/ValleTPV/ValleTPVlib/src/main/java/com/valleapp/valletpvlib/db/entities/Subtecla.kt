package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subteclas")
data class Subtecla(
    @PrimaryKey val ID: Int,
    val Nombre: String,
    val Incremento: Double,
    val IDTecla: Int,
    val descripcion_t: String?,
    val descripcion_r: String?
)
