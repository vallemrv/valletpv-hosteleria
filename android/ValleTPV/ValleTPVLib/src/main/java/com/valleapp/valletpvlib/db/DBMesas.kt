package com.valleapp.valletpvlib.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.valleapp.valletpvlib.ui.theme.hexToComposeColor
import org.json.JSONObject

@Entity(tableName = "mesas")
data class Mesa(
    var nombre: String = "",
    var color: String = "",
    var abierta: Boolean = false,
    var idZona: Int = 0,
    var num: Int = 0,
    var orden: Int = 0
): BaseEntity() {

    private fun loadJson(json: JSONObject) {
        id = json.getLong("id")
        nombre = json.getString("nombre")
        color = json.getString("color")
        abierta = json.getBoolean("abierta")
        idZona = json.getInt("zona_id")
        num = json.getInt("num")
        orden = json.getInt("orden")
    }

    override fun executeAccion(json: JSONObject, dao: IBaseDao<out BaseEntity>, op: String) {
        loadJson(json)
        val tb = dao as MesasDao
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
interface MesasDao: IBaseDao<Mesa> {

    @Query("SELECT * FROM mesas WHERE idZona = :idZona ORDER BY orden DESC")
    fun getAllByZona(idZona: Long): LiveData<List<Mesa>>

    @Query("SELECT * FROM mesas WHERE idZona = :id AND ID != :idm ORDER BY orden DESC")
    fun getAllMenosUna(id: Int, idm: Int): LiveData<List<Mesa>>

    @Query("UPDATE mesas SET abierta=1, num=0 WHERE ID = :idm")
    fun abrirMesa(idm: Int)

    @Query("UPDATE mesas SET abierta=0, num=0 WHERE ID = :idm")
    fun cerrarMesa(idm: Int)

    @Query("UPDATE mesas SET num=1 WHERE ID = :id")
    fun marcarRojo(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMesa(mesa: Mesa)

    @Update
    fun updateMesa(mesa: Mesa)

    @Query("DELETE FROM mesas WHERE ID = :id")
    override fun deleteById(id: Long)

    @Query("SELECT * FROM mesas ")
    override fun getAll(): List<Mesa>
}
