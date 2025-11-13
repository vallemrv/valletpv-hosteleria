package com.valleapp.valletpvlib.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject

class DBTeclas(context: Context) : DBBase(context, "teclas") {

    private var tarifa: Int = 1

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS teclas (" +
                        "ID INTEGER PRIMARY KEY, Nombre TEXT, P1 DOUBLE, P2 DOUBLE, Precio DOUBLE, " +
                        "RGB TEXT, IDSeccion INTEGER, Tag TEXT, Orden INTEGER,  tipo TEXT, " +
                        "descripcion_t TEXT, descripcion_r TEXT, IDParentTecla INTEGER, hay_existencias INTEGER DEFAULT 1)"
        )
    }

    private fun cargarRegistros(sql: String): JSONArray {
        val ls = JSONArray()
        val db = readableDatabase
        val res = db.rawQuery(sql, null)
        try {
            res.moveToFirst()
            while (!res.isAfterLast) {
                ls.put(cursorToJSON(res))
                res.moveToNext()
            }
        } finally {
            res.close()
        }
        return ls
    }

    fun getAll(id: String, tarifa: Int): JSONArray {
        this.tarifa = tarifa
        return cargarRegistros("SELECT * FROM teclas WHERE IDSeccion=$id  ORDER BY Orden DESC")
    }

    fun getAllSub(idParentTecla: String): JSONArray {
       return cargarRegistros("SELECT * FROM teclas WHERE IDParentTecla=$idParentTecla  ORDER BY Orden DESC")
    }

    fun findLike(str: String, t: String): JSONArray {
        this.tarifa = t.toInt()
        return cargarRegistros("SELECT * FROM teclas WHERE Nombre LIKE '%$str%' OR Tag LIKE '%$str%' ORDER BY Orden DESC LIMIT 20")
    }


    override fun inicializar() {
        val db = writableDatabase
        onCreate(db)
    }

    @SuppressLint("Range")
    override fun cursorToJSON(res: Cursor): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("Nombre", res.getString(res.getColumnIndexOrThrow("Nombre")))
            obj.put("ID", res.getString(res.getColumnIndexOrThrow("ID")))
            obj.put("RGB", res.getString(res.getColumnIndexOrThrow("RGB")))
            obj.put("tipo", res.getString(res.getColumnIndexOrThrow("tipo")))
            obj.put("IDParentTecla", res.getInt(res.getColumnIndexOrThrow("IDParentTecla")))
            obj.put("orden", res.getInt(res.getColumnIndexOrThrow("Orden")))
            obj.put("hay_existencias", res.getInt(res.getColumnIndexOrThrow("hay_existencias")))
            val descripcionT = res.getString(res.getColumnIndexOrThrow("descripcion_t"))
            obj.put("descripcion_t",  descripcionT)
            obj.put("descripcion_r", res.getString(res.getColumnIndexOrThrow("descripcion_r")))
            obj.put("P1", res.getDouble(res.getColumnIndexOrThrow("P1")))
            obj.put("P2", res.getDouble(res.getColumnIndexOrThrow("P2")))
            obj.put("IDSeccionCom", res.getInt(res.getColumnIndexOrThrow("IDSeccion")))
            if (tarifa == 2) obj.put("Precio", res.getString(res.getColumnIndexOrThrow("P2")))
            else obj.put("Precio", res.getString(res.getColumnIndexOrThrow("P1")))

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj
    }


    override fun cargarValues(o: JSONObject): ContentValues  {
        val values = ContentValues()
         try {
            values.put("ID", o.getInt("ID"))
            values.put("IDSeccion", o.getInt("IDSeccionCom"))
            values.put("Nombre", o.getString("nombre"))
            values.put("P1", o.getDouble("p1"))
            values.put("P2", o.getDouble("p2"))
            values.put("Precio", o.getDouble("p1")) // esto hay que replazarlo en versiones futras
            values.put("RGB", o.getString("RGB"))
            values.put("Tag", o.getString("tag"))
            values.put("Orden", o.getString("orden"))
            values.put("tipo", o.getString("tipo"))
            values.put("descripcion_t", o.getString("descripcion_t"))
            values.put("descripcion_r", o.getString("descripcion_r"))
            values.put("IDParentTecla", o.optInt("IDParentTecla", 0))
            values.put("hay_existencias", o.optInt("hay_existencias", 1))

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return values
    }
}