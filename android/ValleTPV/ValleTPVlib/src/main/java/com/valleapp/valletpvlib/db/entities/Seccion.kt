package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.valleapp.valletpvlib.interfaces.IBaseEntity
import org.json.JSONObject

@Entity(tableName = "secciones")
data class Seccion(
    @PrimaryKey val ID: Int,
    val Nombre: String,
    val Orden: Int,
    val RGB: String
) : IBaseEntity<Seccion> {

    override fun emtityFromJson(obj: JSONObject): Seccion {
        return Seccion(
            ID = obj.getInt("ID"),
            Nombre = obj.getString("Nombre"),
            Orden = obj.getInt("Orden"),
            RGB = obj.getString("RGB")
        )
    }

    override fun jsonFromEmtity(entity: Seccion): JSONObject {
        val json = JSONObject()
        json.put("ID", entity.ID)
        json.put("Nombre", entity.Nombre)
        json.put("Orden", entity.Orden)
        json.put("RGB", entity.RGB)
        return json
    }
}