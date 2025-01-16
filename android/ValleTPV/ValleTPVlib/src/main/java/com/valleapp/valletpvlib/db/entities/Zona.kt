package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.valleapp.valletpvlib.interfaces.IBaseEntity
import org.json.JSONObject

@Entity(tableName = "zonas")
data class Zona(
    @PrimaryKey val ID: Int,
    val Nombre: String,
    val RGB: String,
    val Tarifa: Int
) : IBaseEntity<Zona> {

    override fun entityFromJson(obj: JSONObject): Zona {
        return Zona(
            ID = obj.getInt("ID"),
            Nombre = obj.getString("Nombre"),
            RGB = obj.getString("RGB"),
            Tarifa = obj.getInt("Tarifa")
        )
    }

    override fun jsonFromEntity(entity: Zona): JSONObject {
        val json = JSONObject()
        json.put("ID", entity.ID)
        json.put("Nombre", entity.Nombre)
        json.put("RGB", entity.RGB)
        json.put("Tarifa", entity.Tarifa)
        return json
    }
}