package com.valleapp.valletpvlib.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Update
import org.json.JSONObject

enum class AccionMesa{
    JUNTAR, MOVER, BORRAR, NADA
}


@Entity(tableName = "mesas")
data class Mesa(
    var nombre: String = "",
    var color: String = "",
    var abierta: Boolean = false,
    var zona_id: Int? = -1,
    var num: Int? = -1,
    var orden: Int? = -1,
    var tarifa: Int? = -1,
): BaseEntity() {

    private fun loadJson(json: JSONObject) {
        id = json.getLong("id")
        nombre = json.getString("nombre")
        color = json.getString("color")
        abierta = json.getBoolean("abierta")
        zona_id = json.getInt("zona_id")
        num = json.getInt("num")
        orden = json.getInt("orden")
        tarifa = json.getInt("tarifa")
    }

    override fun executeAccion(json: JSONObject, dao: IBaseDao<out BaseEntity>, op: String) {
        loadJson(json)
        val tb = dao as MesasDao
        when (op) {
            "INS" -> tb.insert(this)
            "UP" ->   tb.update(this)
        }
    }

    override fun toString(): String {
        return nombre
    }
}


@Dao
interface MesasDao: IBaseDao<Mesa> {

    @Query("SELECT * FROM mesas WHERE zona_id = :idZona ORDER BY orden DESC")
    fun getAllByZona(idZona: Long): LiveData<List<Mesa>>

    @Query("UPDATE mesas SET abierta=1, num=0 WHERE ID = :idm")
    fun abrirMesa(idm: Int)

    @Query("UPDATE mesas SET abierta=0, num=0 WHERE ID = :idm")
    fun cerrarMesa(idm: Long)

    @Query("UPDATE mesas SET num=1 WHERE ID = :id")
    fun marcarRojo(id: Int)

    @Update
    fun updateMesa(mesa: Mesa)

    @Query("DELETE FROM mesas WHERE ID = :id")
    override fun deleteById(id: Long)

    @Query("SELECT * FROM mesas ")
    override fun getAll(): List<Mesa>

    @Query("SELECT * FROM mesas WHERE ID = :id")
    fun getMesa(id: Long): Mesa
}
