package com.valleapp.valletpvlib.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query
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
    var descripcion_t: String = "",
    var precio: Double = 0.0,
    var pedido_id: Long? = -1,
    var mesa_id: Long? = -1,
    var tecla_id: Long? = -1,
    var nomMesa: String? = "",
    var zona_id: Long? = -1,
    var servido: Boolean? = false,
    var receptor_id: Long? = -1,
    var camarero_id: Long? = -1,
    var UID: String? = "",
    var pk: Long? = -1
) : BaseEntity() {

    private fun loadJson(json: JSONObject) {
        pk = json.getLong("id")
        println("LineaPedido: $pk")
        estado = json.getString("estado")
        descripcion = json.getString("descripcion")
        descripcion_t = json.getString("descripcion_t")
        precio = json.getDouble("precio")
        pedido_id = json.getLong("pedido_id")
        mesa_id = json.getLong("mesa_id")
        tecla_id = json.getLong("tecla_id")
        nomMesa = json.getString("nomMesa")
        zona_id = json.getLong("zona_id")
        servido = json.getBoolean("servido")
        receptor_id = json.getLong("receptor_id")
        camarero_id = json.getLong("camarero_id")
        UID = json.getString("UID")
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
        return "' $descripcion - $precio - $pk - $id '"
    }
}

@Dao
interface LineasDao : IBaseDao<LineaPedido> {

    @Query("SELECT * FROM LineasPedido")
    fun getListaLive(): LiveData<List<LineaPedido>>

    @Query("SELECT pk as id, estado, descripcion_t," +
            " descripcion, precio, pedido_id," +
            " mesa_id, tecla_id, nomMesa," +
            " zona_id, servido, receptor_id, camarero_id, UID FROM LineasPedido")
    override fun getAll(): List<LineaPedido>

    @Query("DELETE FROM LineasPedido WHERE pk = :id")
    override fun deleteById(id: Long)

    @Query("DELETE FROM LineasPedido")
    fun deleteAll()

    @Query(
        "SELECT COUNT(id) as cantidad, descripcion_t as descripcion, precio, COUNT(id) * precio as total  " +
                "FROM LineasPedido WHERE mesa_id = :mesaId AND estado in ('P', 'N') GROUP BY tecla_id, descripcion_t, precio, estado"
    )
    fun getLineaCuentas(mesaId: Long): LiveData<List<LineaCuenta>>
    @Query("SELECT id,  descripcion, descripcion_t, tecla_id, precio " +
            "  FROM LineasPedido Where mesa_id = :mesaId AND estado = 'N'")
    fun getNuevas(mesaId: Long): List<LineaPedido>

}
