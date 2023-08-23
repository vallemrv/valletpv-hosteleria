package com.valleapp.valletpvlib.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import org.json.JSONObject


@Entity(tableName = "camareros")
data class Camarero(
    var nombre: String = "",
    var apellidos: String = "",
    var activo: Boolean = true,
    var password: String = "",
    var autorizado: Boolean = true,
    var permisos: String = ""
): BaseEntity(){

    private fun loadJson(json: JSONObject) {
        nombre = json.getString("nombre") ?: ""
        apellidos = json.getString("apellidos")?:""
        json.getBoolean("activo").let {
            activo = it
        }
        password = json.getString("password")?:""
        json.getBoolean("autorizado").let {
            autorizado = it
        }
        permisos = json.getString("permisos")?:""
        id = json.getLong("id")
    }

    override fun executeAccion(json: JSONObject, dao: IBaseDao<out BaseEntity>, op: String) {
        loadJson(json)
        val tb = dao as CamareroDao
        when (op) {
            "INS" -> tb.insert(this)
            "UP" ->   tb.update(this)
        }
    }

    override fun toString(): String {
        return "$nombre $apellidos"
    }

}

@Dao
interface CamareroDao: IBaseDao<Camarero> {


    @Query("DELETE FROM camareros WHERE ID = :id")
    override fun deleteById(id: Long)

    @Query("SELECT * FROM camareros ")
    override fun getAll(): List<Camarero>

    @Query("UPDATE camareros SET autorizado = :autorizado WHERE ID = :id")
    fun setAutorizado(id: Long, autorizado: Boolean)

    @Query("SELECT * FROM camareros WHERE activo = 1 AND autorizado = :autorizado")
    fun getAutorizados(autorizado: Boolean): LiveData<List<Camarero>>

    @Query("SELECT * FROM camareros WHERE activo = 1 AND permisos LIKE :permiso")
    fun getConPermiso(permiso: String): LiveData<List<Camarero>>

    @Insert
    fun insertCamarero(camarero: Camarero)



}



