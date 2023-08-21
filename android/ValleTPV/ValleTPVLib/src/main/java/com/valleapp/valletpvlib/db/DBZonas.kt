package com.valleapp.valletpvlib.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query
import com.valleapp.valletpvlib.ui.theme.hexToComposeColor
import org.json.JSONObject

@Entity(tableName = "zonas")
data class Zona(
    var nombre: String = "",
    var color: String = "",
    var tarifa: Int = 1
) : BaseEntity() {

    private fun loadJson(json: JSONObject) {
        id = json.getLong("id")
        nombre = json.getString("nombre")
        color = json.getString("color")
        tarifa = json.getInt("tarifa")
    }

    override fun executeAccion(json: JSONObject, dao: IBaseDao<out BaseEntity>, op: String) {
        loadJson(json)
        val tb = dao as ZonasDao
        when (op) {
            "INS" -> tb.insert(this)
            "UP" ->   tb.update(this)
           }
    }

    override fun getInfoField(): InfoField {
        return InfoField(nombre, id, hexToComposeColor(color))
    }
}

@Dao
interface ZonasDao : IBaseDao<Zona> {


    @Query("SELECT * FROM zonas")
    fun getListaLive(): LiveData<List<Zona>>

    @Query("SELECT * FROM zonas")
    override fun getAll(): List<Zona>


    @Query("DELETE FROM zonas WHERE ID = :id")
    override fun deleteById(id: Long)


}
