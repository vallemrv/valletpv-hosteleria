package com.valleapp.valletpvlib.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query
import androidx.room.RoomWarnings
import org.json.JSONObject

data class LineaCuenta(
    var cantidad: Int = 0,
    var descripcion: String = "",
    var precio: Double = 0.0,
    var total: Double = 0.0,
)

@Entity(tableName = "LineasPedido")
data class LineaPedido(
    var estado: String? = "",
    var descripcion: String = "",
    var descripcionT: String = "",
    var precio: Double = 0.0,
    var pedidoId: Long? = -1,
    var mesaId: Long? = -1,
    var teclaId: Long? = -1,
    var nomMesa: String? = "",
    var zonaId: Long? = -1,
    var servido: Boolean? = false,
    var receptorId: Long? = -1,
    var camareroId: Long? = -1,
    var uid: String? = "",
) : BaseEntity() {

    private fun loadJson(json: JSONObject) {
        id = json.getLong("id")
        estado = json.getString("estado")
        descripcion = json.getString("descripcion")
        descripcionT = json.getString("descripcionT")
        precio = json.getDouble("precio")
        pedidoId = json.getLong("pedidoId")
        mesaId = json.getLong("mesaId")
        teclaId = json.getLong("teclaId")
        nomMesa = json.getString("nomMesa")
        zonaId = json.getLong("zonaId")
        servido = json.getBoolean("servido")
        receptorId = json.getLong("receptorId")
        camareroId = json.getLong("camareroId")
        uid = json.getString("uid")
    }

    override fun executeAccion(json: JSONObject, dao: IBaseDao<out BaseEntity>, op: String) {
        loadJson(json)
        val tb = dao as LineasDao
        when (op) {
            "INS" -> tb.insert(this)
            "UP" -> tb.update(this)
        }
    }

    override fun toString(): String {
        return "$descripcionT $precio $estado $uid $id $pedidoId $mesaId $teclaId $nomMesa $zonaId $servido $receptorId $camareroId \n"
    }
}

@Dao
interface LineasDao : IBaseDao<LineaPedido> {

    @Query("SELECT * FROM LineasPedido WHERE mesaId=:mesaId")
    fun getByMesa(mesaId: Long): List<LineaPedido>

    @Query("SELECT * FROM LineasPedido WHERE mesaId=:mesaId")
    fun getAllLinea(mesaId: Long): LiveData<List<LineaPedido>>

    @Query("SELECT id FROM LineasPedido WHERE mesaId=:mesaId")
    fun getAllIds(mesaId: Long): List<Long>

    @Query("SELECT * FROM LineasPedido")
    override fun getAll(): List<LineaPedido>

    @Query("DELETE FROM LineasPedido WHERE id=:id")
    override fun deleteById(id: Long)


    @Query(
        "SELECT COUNT(id) as cantidad, descripcionT as descripcion, precio, COUNT(id) * precio as total  " +
                "FROM LineasPedido WHERE mesaId = :mesaId AND estado in ('P', 'N')" +
                " GROUP BY teclaId, descripcionT, precio, estado"
    )
    fun getLineasCuenta(mesaId: Long): LiveData<List<LineaCuenta>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id, descripcion, descripcionT, teclaId, precio, mesaId " +
            "  FROM LineasPedido Where mesaId = :mesaId AND estado = 'N'")
    fun getNuevas(mesaId: Long): List<LineaPedido>

    @Query("SELECT SUM(precio) FROM LineasPedido WHERE mesaId = :mesaId AND estado = 'P'")
    fun getTotal(mesaId: Long): Double

    @Query("UPDATE LineasPedido SET estado = 'C' WHERE mesaId = :mesaId AND estado = 'P'")
    fun cobrarMesa(mesaId: Long)

    @Query("UPDATE LineasPedido SET estado = 'C' WHERE id = :id  AND estado = 'P'")
    fun cobrarlinea(id: Long)

    @Query("SELECT id FROM LineasPedido WHERE descripcionT = :descripcion " +
            "AND precio = :precio AND estado in (:estado) LIMIT 1")
    fun findFirstByDescripcionAndPrecio(descripcion: String, precio: Double, estado: List<String>): Long?

}
