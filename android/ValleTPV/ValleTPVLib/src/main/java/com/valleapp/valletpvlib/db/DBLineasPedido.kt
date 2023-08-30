package com.valleapp.valletpvlib.db

import androidx.room.Entity
import org.json.JSONObject

@Entity(tableName = "dbcuenta")
data class DBCuenta(
    var estado: String = "",
    var descripcion: String = "",
    var descripcion_t: String = "",
    var precio: Double = 0.0,
    var pedidoId: Long? = null,
    var mesaId: Long? = null,
    var teclaId: Long? = null,
    var nomMesa: String = "",
    var zonaId: Long? = null,
    var servido: Boolean = false,
    var receptor: String = "",
    var camreroId: Long? = null,
    var UID: String = ""
) : BaseEntity() {

    private fun loadJson(json: JSONObject) {
        id = json.getLong("id")
        estado = json.getString("estado")
        descripcion = json.getString("descripcion")
        descripcion_t = json.getString("descripcion_t")
        precio = json.getDouble("precio")
        pedidoId = json.getLong("pedido_id")
        mesaId = json.getLong("mesa_id")
        teclaId = json.getLong("tecal_id")
        nomMesa = json.getString("nomMesa")
        zonaId = json.getLong("zona_id")
        servido = json.getBoolean("servido")
        receptor = json.getString("receptor")
        camreroId = json.getLong("camarero")
        UID = json.getString("UID")
    }

    override fun executeAccion(json: JSONObject, dao: IBaseDao<out BaseEntity>, op: String) {
        loadJson(json)
        val tb = dao as DBCuentaDao
        when (op) {
            "INS" -> tb.insert(this)
            "UP" -> tb.update(this)
        }
    }

    override fun toString(): String {
        return "$descripcion - $precio"
    }
}

@Dao
interface DBCuentaDao : IBaseDao<DBCuenta> {

    @Query("SELECT * FROM dbcuenta")
    fun getListaLive(): LiveData<List<DBCuenta>>

    @Query("SELECT * FROM dbcuenta")
    override fun getAll(): List<DBCuenta>

    @Query("DELETE FROM dbcuenta WHERE ID = :id")
    override fun deleteById(id: Long)

    // Puedes agregar más queries según lo necesites...
}
