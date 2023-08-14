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
class DBSecciones(context: Context?) : DBBase(context, "secciones") {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS secciones (ID INTEGER PRIMARY KEY, Nombre TEXT,Orden INTEGER, RGB TEXT)")
    }

    val all: JSONArray
        get() = filter(null)

    fun filter(cWhere: String?): JSONArray {
        val db = this.readableDatabase
        var w = ""
        if (cWhere != null) {
            w = " WHERE $cWhere"
        }
        val res = db.rawQuery("select * from $tb_name $w ORDER BY orden DESC", null)
        res.moveToFirst()
        val list = JSONArray()
        while (!res.isAfterLast) {
            try {
                list.put(cursorToJSON(res))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            res.moveToNext()
        }
        return list
    }

    override fun inicializar() {
        val db = this.writableDatabase
        onCreate(db)
    }

    @SuppressLint("Range")
    override fun cursorToJSON(res: Cursor?): JSONObject? {
        val obj = JSONObject()
        try {
            obj.put("Nombre", res!!.getString(res.getColumnIndex("Nombre")))
            obj.put("ID", res.getString(res.getColumnIndex("ID")))
            obj.put("RGB", res.getString(res.getColumnIndex("RGB")))
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
            values.put("RGB", o.getString("rgb"))
            values.put("Orden", o.getString("orden"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return values
    }
}