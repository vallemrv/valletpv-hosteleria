package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zonas")
data class Zona(
    @PrimaryKey val ID: Int,
    val Nombre: String,
    val RGB: String,
    val Tarifa: Int
)
