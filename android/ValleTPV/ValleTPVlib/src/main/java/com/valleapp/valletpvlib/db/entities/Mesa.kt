package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.valleapp.valletpvlib.interfaces.IBaseEntity
import org.json.JSONObject

@Entity(tableName = "mesas")
data class Mesa(
    @PrimaryKey val ID: Int,
    val Nombre: String,
    val RGB: String,
    val abierta: Boolean,
    val IDZona: Int,
    val num: Int,
    val Orden: Int
) : IBaseEntity<Mesa> {

    override fun emtityFromJson(obj: JSONObject): Mesa {
        return Mesa(
            ID = obj.getInt("ID"),
            Nombre = obj.getString("Nombre"),
            RGB = obj.getString("RGB"),
            abierta = obj.getInt("abierta") == 1,
            IDZona = obj.getInt("IDZona"),
            num = obj.getInt("num"),
            Orden = obj.getInt("Orden")
        )
    }

    override fun jsonFromEmtity(entity: Mesa): JSONObject {
        val json = JSONObject()
        json.put("ID", entity.ID)
        json.put("Nombre", entity.Nombre)
        json.put("RGB", entity.RGB)
        json.put("abierta", if (entity.abierta) 1 else 0)
        json.put("IDZona", entity.IDZona)
        json.put("num", entity.num)
        json.put("Orden", entity.Orden)
        return json
    }
}