package com.valleapp.valletpvlib.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID
import androidx.core.database.sqlite.transaction

class DBCuenta(context: Context) : DBBase(context, "cuenta") {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
                """CREATE TABLE IF NOT EXISTS cuenta 
                    (ID TEXT PRIMARY KEY, Estado TEXT, 
                    Descripcion TEXT, descripcion_t TEXT, 
                    Precio DOUBLE, IDPedido INTEGER, 
                    IDMesa INTEGER, IDArt INTEGER,
                    receptor INTEGER,
                    camarero INTEGER, 
                    nomMesa TEXT, IDZona TEXT, 
                    servido INTEGER )"""
        )
    }

    override fun rellenarTabla(objs: JSONArray) {
        val db = writableDatabase
        db.execSQL("DELETE FROM $tbName WHERE Estado != 'N'")
        for (i in 0 until objs.length()) {
            try {
                insert(objs.getJSONObject(i))
            } catch (e: JSONException) {
                Log.e("ERROR_DB_CUENTA", e.toString())
            }
        }
    }

    fun getLineasByPedido(cWhere: String?): JSONArray{
        val lista = JSONArray()
        var res: Cursor? = null
        try{
            val strWhere = cWhere?.let { " WHERE $it" } ?: ""
            val db = readableDatabase
            res = db.rawQuery(
                """SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total
                           FROM cuenta $strWhere 
                           GROUP BY IDArt, Descripcion, Precio, Estado, 
                                    IDPedido, Camarero
                           ORDER BY IDPedido DESC""", null
            )
            while (res.moveToNext()) {
                lista.put(cursorToJSON(res))
            }
        } catch (e: SQLiteException) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        } finally {
            res?.close()
        }
        return lista
    }

    fun filterGroup(cWhere: String?): JSONArray {
        val lista = JSONArray()
        var res: Cursor? = null
        try {
            val strWhere = cWhere?.let { " WHERE $it" } ?: ""
            val db = readableDatabase
            res = db.rawQuery(
                    """SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total
                       FROM cuenta $strWhere 
                       GROUP BY IDArt, Descripcion, Precio, Estado 
                       ORDER BY ID DESC""", null
            )
            while (res.moveToNext()) {
                lista.put(cursorToJSON(res))
            }
        } catch (e: SQLiteException) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        } finally {
            res?.close()
        }
        return lista
    }

    fun getLineasTicket(idMesa: String): List<JSONObject> {
        val lista = mutableListOf<JSONObject>()
        var res: Cursor? = null
        try {
            val db = readableDatabase
            res = db.rawQuery(
                """SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total
                   FROM cuenta 
                   WHERE IDMesa = ? AND Estado IN ('N', 'P', 'E')
                   GROUP BY descripcion_t, Precio, Estado 
                   ORDER BY ID DESC""",
                arrayOf(idMesa)
            )
            while (res.moveToNext()) {
                lista.add(cursorToJSON(res))
            }
        } catch (e: SQLiteException) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        } finally {
            res?.close()
        }
        return lista
    }

    fun enviarLineas(idMesa: String) {
        try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("Estado", "E")
            }
            db.update("cuenta", values, "IDMesa = ? AND Estado = 'N'", arrayOf(idMesa))
        } catch (e: SQLiteException) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        }
    }

    private fun filterList(cWhere: String?): List<JSONObject> {
        val lista = mutableListOf<JSONObject>()
        var res: Cursor? = null
        try {
            val strWhere = cWhere?.let { " WHERE $it" } ?: ""
            val db = readableDatabase
            res = db.rawQuery(
                    """SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total
                       FROM cuenta $strWhere 
                       GROUP BY IDArt, Descripcion, Precio, Estado 
                       ORDER BY ID DESC""", null
            )
            while (res.moveToNext()) {
                lista.add(cursorToJSON(res))
            }
        } catch (e: SQLiteException) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        } finally {
            res?.close()
        }
        return lista
    }

    override fun cargarValues(o: JSONObject): ContentValues {
        val values = ContentValues()
        try {
            values.put("ID", o.getString("ID"))
            values.put("IDArt", o.getInt("IDArt"))
            values.put("Descripcion", o.getString("Descripcion"))
            values.put("descripcion_t", o.getString("descripcion_t"))
            values.put("Precio", o.getDouble("Precio"))
            values.put("IDMesa", o.getString("IDMesa"))
            values.put("IDZona", o.getString("IDZona"))
            values.put("nomMesa", o.getString("nomMesa"))
            values.put("IDPedido", o.getString("IDPedido"))
            values.put("Estado", o.getString("Estado"))
            values.put("servido", o.getString("servido"))
            values.put("receptor", o.getString("receptor"))
            values.put("camarero", o.getString("camarero"))

        } catch (e: Exception) {
            Log.e("CUENTA-CARGARVALUES", e.toString())
        }
        return values
    }

    fun getAll(): List<JSONObject> {
        return filterList(null)
    }


    fun getAllByMesa(id: String?): List<JSONObject> {
        return filterList("IDMesa = $id AND (Estado = 'N' OR Estado = 'P' OR Estado = 'E')")
    }

    // Modified getTotal to include 'E' state
    fun getTotal(id: String): Double {
        var s = 0.0
        var cursor: Cursor? = null
        try {
            val db = readableDatabase
            cursor = db.rawQuery(
                """SELECT SUM(Precio) AS TotalTicket 
               FROM cuenta 
               WHERE IDMesa = ? 
               AND (Estado = 'N' OR Estado = 'P' OR Estado = 'E')""",
                arrayOf(id)
            )
            if (cursor.moveToFirst() && cursor.columnCount > 0) {
                s = cursor.getDouble(0)
            }
        } catch (e: SQLiteException) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        } finally {
            cursor?.close()
        }
        return s
    }


    fun cambiarCuenta(id: String, id1: String) {
        try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("IDMesa", id1)
            }
            db.update(
                "cuenta",
                values,
                "IDMesa = ? AND Estado != 'N'",
                arrayOf(id)
            )
        } catch (e: SQLiteException) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        }
    }


    override fun cursorToJSON(res: Cursor): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("ID", res.getString(res.getColumnIndexOrThrow("ID")))
            obj.put("Descripcion", res.getString(res.getColumnIndexOrThrow("Descripcion")))
            obj.put("descripcion_t", res.getString(res.getColumnIndexOrThrow("descripcion_t")))

            res.getColumnIndex("Can").takeIf { it >= 0 }?.let {
                obj.put("Can", res.getString(it))
            }

            res.getColumnIndex("Total").takeIf { it >= 0 }?.let {
                obj.put("Total", res.getString(it))
                obj.put("CanCobro", 0)
            }

            obj.put("Precio", res.getString(res.getColumnIndexOrThrow("Precio")))
            obj.put("IDArt", res.getString(res.getColumnIndexOrThrow("IDArt")))
            obj.put("Estado", res.getString(res.getColumnIndexOrThrow("Estado")))
            obj.put("nomMesa", res.getString(res.getColumnIndexOrThrow("nomMesa")))
            obj.put("IDPedido", res.getString(res.getColumnIndexOrThrow("IDPedido")))
            obj.put("servido", res.getString(res.getColumnIndexOrThrow("servido")))
            obj.put("IDZona", res.getString(res.getColumnIndexOrThrow("IDZona")))
            obj.put("IDMesa", res.getString(res.getColumnIndexOrThrow("IDMesa")))
            obj.put("camarero", res.getString(res.getColumnIndexOrThrow("camarero")))
            obj.put("receptor", res.getString(res.getColumnIndexOrThrow("receptor")))

        } catch (e: Exception) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        }
        return obj
    }



    fun replaceMesa(datos: JSONArray, idMesa: String) {
        try {
            val db = writableDatabase
            db.execSQL("DELETE FROM cuenta WHERE IDMesa = $idMesa AND Estado != 'N'")
            for (i in 0 until datos.length()) {
                insert(datos.getJSONObject(i))
            }
        } catch (e: Exception) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        }
    }

    fun addArt(idMesa: Int, art: JSONObject) {
        try {
            val db = writableDatabase
            val can = art.getInt("Can")
            repeat(can) {
                val values = ContentValues().apply {
                    put("ID", UUID.randomUUID().toString())
                    put("IDArt", art.getInt("ID"))
                    put("Descripcion", art.getString("Descripcion"))
                    put("descripcion_t", art.getString("descripcion_t"))
                    put("Precio", art.getDouble("Precio"))
                    put("IDMesa", idMesa)
                    put("Estado", "N")
                }
                db.insert("cuenta", null, values)
            }
        } catch (e: JSONException) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        }
    }

    fun getNuevos(id: String): JSONArray {
        val lista = JSONArray()
        val db = readableDatabase
        var res: Cursor? = null
        try {
            val query = """
            SELECT *, COUNT(ID) AS Can 
            FROM cuenta 
            WHERE IDMesa = ? AND Estado = 'N' 
            GROUP BY IDArt, Descripcion, Precio, Estado
        """
            res = db.rawQuery(query, arrayOf(id))

            while (res.moveToNext()) {
                lista.put(cursorToJSON(res))
            }
        } catch (e: SQLiteException) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        } finally {
            res?.close()
        }

        return lista
    }


    fun eliminar(idMesa: String) {
        try {
            val db = writableDatabase
            db.delete("cuenta", "IDMesa = ? AND Estado != 'N'", arrayOf(idMesa))
        } catch (e: Exception) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        }
    }

    fun eliminar(idMesa: String, lsart: JSONArray): List<Long> {
        val db = writableDatabase
        val idsBorrados = mutableListOf<Long>()

        // Iniciar una transacción para asegurar que todas las operaciones se completen o ninguna
        db.transaction {
            try {
                for (i in 0 until lsart.length()) {
                    val art = lsart.getJSONObject(i)
                    val idArt = art.getString("IDArt")
                    val descripcion = art.getString("Descripcion")
                    val precio = art.getString("Precio")
                    val cantidad = art.getString("Can")

                    // --- PASO 1: SELECT para obtener los IDs que se van a borrar ---
                    val idsParaBorrar = mutableListOf<Long>()
                    val sqlSelect = """
                SELECT ID FROM cuenta 
                WHERE IDMesa = ? 
                AND IDArt = ?
                AND Descripcion = ?
                AND Precio = ?
                LIMIT ?
            """
                    // Usamos '?' para pasar los argumentos de forma segura
                    val selectionArgs = arrayOf(idMesa, idArt, descripcion, precio, cantidad)

                    val cursor = rawQuery(sqlSelect, selectionArgs)
                    cursor.use { // 'use' cierra el cursor automáticamente
                        while (it.moveToNext()) {
                            idsParaBorrar.add(it.getLong(it.getColumnIndexOrThrow("ID")))
                        }
                    }

                    // --- PASO 2: DELETE usando los IDs encontrados ---
                    if (idsParaBorrar.isNotEmpty()) {
                        // Creamos una cadena de '?' para la cláusula IN, ej: (?,?,?)
                        val placeholders = idsParaBorrar.joinToString(separator = ", ") { "?" }
                        val sqlDelete = "DELETE FROM cuenta WHERE ID IN ($placeholders)"

                        // Convertimos los IDs (Long) a String para los argumentos
                        val deleteArgs = idsParaBorrar.map { it.toString() }.toTypedArray()

                        execSQL(sqlDelete, deleteArgs)

                        // Añadimos los IDs borrados a nuestra lista de resultados
                        idsBorrados.addAll(idsParaBorrar)
                    }
                }
                // Si todo ha ido bien, marcamos la transacción como exitosa
            } catch (e: Exception) {
                Log.e("ERROR_DB_CUENTA", "Error eliminando artículos: $e")
            } finally {
                // Finalizamos la transacción (hace commit si fue exitosa, si no, hace rollback)
            }
        }

        return idsBorrados
    }

    fun artServido(arr: JSONArray) {
        val db = writableDatabase
        try {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val p = ContentValues()
                p.put("servido", 1)
                db.update(
                    "cuenta", p, "IDArt = ? AND Descripcion = ? AND Precio = ? AND IDPedido = ? ",
                    arrayOf<String?>(
                        obj.getString("IDArt"), obj.getString("Descripcion"),
                        obj.getString("Precio"), obj.getString("IDPedido")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        }
    }

    fun getPedidosChoices(idMesa: String) : List<JSONObject> {
        val lista = mutableListOf<JSONObject>()
        var res: Cursor? = null
        
        try {
            val db = readableDatabase
            
            // Primero obtenemos todos los pedidos únicos para la mesa
            val pedidosQuery = """
                SELECT DISTINCT IDPedido 
                FROM cuenta 
                WHERE IDMesa = ? AND Estado = 'P' AND Precio > 0
                ORDER BY IDPedido
            """
            
            res = db.rawQuery(pedidosQuery, arrayOf(idMesa))
            
            while (res.moveToNext()) {
                val idPedido = res.getString(0)
                
                // Para cada pedido, obtenemos los artículos agrupados
                val articulosQuery = """
                    SELECT Descripcion, COUNT(*) as cantidad
                    FROM cuenta 
                    WHERE IDMesa = ? AND IDPedido = ? AND Estado = 'P' AND Precio > 0
                    GROUP BY Descripcion, IDArt
                    ORDER BY Descripcion
                """
                
                var resArticulos: Cursor? = null
                try {
                    resArticulos = db.rawQuery(articulosQuery, arrayOf(idMesa, idPedido))
                    
                    val articulos = mutableListOf<String>()
                    while (resArticulos.moveToNext()) {
                        val descripcion = resArticulos.getString(0)
                        val cantidad = resArticulos.getInt(1)
                        
                        val articuloText = if (cantidad > 1) {
                            "$cantidad $descripcion"
                        } else {
                            descripcion
                        }
                        articulos.add(articuloText)
                    }
                    
                    // Crear el subtitle según las reglas
                    val subtitle = when {
                        articulos.isEmpty() -> ""
                        articulos.size <= 3 -> articulos.joinToString(", ")
                        else -> "${articulos.take(3).joinToString(", ")}, etc"
                    }
                    
                    val pedidoJson = JSONObject().apply {
                        put("IDPedido", idPedido)
                        put("subtitle", subtitle)
                    }
                    
                    lista.add(pedidoJson)
                    
                } finally {
                    resArticulos?.close()
                }
            }
                
            } catch (e: SQLiteException) {
                Log.e("ERROR_DB_CUENTA", e.toString())
            } finally {
                res?.close()
            }
        
     return lista
    }

    fun cambiarLinea(idLinea: String, idMesa: String) {
        try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("IDMesa", idMesa)
            }
            db.update(
                "cuenta",
                values,
                "ID = ?",
                arrayOf(idLinea)
            )
        }catch (e: SQLiteException) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        }
    }

    override fun filter(cWhere: String?): JSONArray {
        val lista = JSONArray()
        var res: Cursor? = null
        try {
            val strWhere = cWhere?.let { " WHERE $it" } ?: ""
            val db = readableDatabase
            res = db.rawQuery(
                """SELECT * FROM cuenta $strWhere ORDER BY ID DESC""", null
            )
            while (res.moveToNext()) {
                lista.put(cursorToJSON(res))
            }
        } catch (e: SQLiteException) {
            Log.e("ERROR_DB_CUENTA", e.toString())
        } finally {
            res?.close()
        }
        return lista
    }

}