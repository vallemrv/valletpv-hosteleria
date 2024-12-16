package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.valleapp.valletpvlib.interfaces.IBaseEntity
import org.json.JSONObject

@Entity(tableName = "cuenta")
data class Cuenta(
    @PrimaryKey val ID: String,
    val Estado: String?,
    val Descripcion: String?,
    val descripcion_t: String?,
    val Precio: Double?,
    val IDPedido: Int?,
    val IDMesa: Int?,
    val IDArt: Int?,
    val nomMesa: String?,
    val IDZona: String?,
    val servido: Int?
) : IBaseEntity<Cuenta> {

    override fun entityFromJson(obj: JSONObject): Cuenta {
        return Cuenta(
            ID = obj.getString("ID"),
            Estado = obj.optString("Estado"),
            Descripcion = obj.optString("Descripcion"),
            descripcion_t = obj.optString("descripcion_t"),
            Precio = obj.optDouble("Precio"),
            IDPedido = obj.optInt("IDPedido"),
            IDMesa = obj.optInt("IDMesa"),
            IDArt = obj.optInt("IDArt"),
            nomMesa = obj.optString("nomMesa"),
            IDZona = obj.optString("IDZona"),
            servido = obj.optInt("servido")
        )
    }

    override fun jsonFromEntity(entity: Cuenta): JSONObject {
        val json = JSONObject()
        json.put("ID", entity.ID)
        json.put("Estado", entity.Estado)
        json.put("Descripcion", entity.Descripcion)
        json.put("descripcion_t", entity.descripcion_t)
        json.put("Precio", entity.Precio)
        json.put("IDPedido", entity.IDPedido)
        json.put("IDMesa", entity.IDMesa)
        json.put("IDArt", entity.IDArt)
        json.put("nomMesa", entity.nomMesa)
        json.put("IDZona", entity.IDZona)
        json.put("servido", entity.servido)
        return json
    }
}