package com.valleapp.valletpvlib.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by valle on 13/10/14.
 */
abstract class DBSubTeclas(context: Context?) : DBBase(context, "subteclas") {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS subteclas (ID INTEGER PRIMARY KEY, " +
                    " Nombre TEXT, Incremento DOUBLE, IDTecla INTEGER," +
                    " descripcion_t TEXT, descripcion_r TEXT)"
        )
    }

    fun getAll(id: String): JSONArray {
        return filter("IDTecla=$id")
    }

    override fun inicializar() {
        val db = this.writableDatabase
        onCreate(db)
    }

    @SuppressLint("Range")
    override fun cursorToJSON(res: Cursor?): JSONObject? {
        val obj = JSONObject()
        try {
            obj.put("ID", res!!.getString(res.getColumnIndex("ID")))
            obj.put("tecla", res.getString(res.getColumnIndex("IDTecla")))
            obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")))
            obj.put("Incremento", res.getString(res.getColumnIndex("Incremento")))
            obj.put("descripcion_t", res.getString(res.getColumnIndex("descripcion_t")))
            obj.put("descripcion_r", res.getString(res.getColumnIndex("descripcion_r")))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj
    }

    override fun caragarValues(o: JSONObject?): ContentValues {
        val values = ContentValues()
        try {
            values.put("ID", o!!.getInt("id"))
            values.put("Nombre", o.getString("nombre"))
            values.put("Incremento", o.getString("incremento"))
            values.put("IDTecla", o.getString("tecla"))
            values.put("descripcion_r", o.getString("descripcion_r"))
            values.put("descripcion_t", o.getString("descripcion_t"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return values
    }
}