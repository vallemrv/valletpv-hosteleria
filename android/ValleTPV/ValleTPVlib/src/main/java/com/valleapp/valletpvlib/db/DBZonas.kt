package com.valleapp.valletpvlib.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject

class DBZonas(context: Context) : DBBase(context, "zonas") {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS zonas (
                ID INTEGER PRIMARY KEY, 
                Nombre TEXT, 
                RGB TEXT, 
                Tarifa INTEGER)"""
        )
    }

    fun getAll(): JSONArray {
        return filter(null)
    }

    override fun cursorToJSON(res: Cursor): JSONObject {
        val cam = JSONObject()
        try {
            cam.put("Nombre", res.getString(res.getColumnIndexOrThrow("Nombre")))
            cam.put("ID", res.getString(res.getColumnIndexOrThrow("ID")))
            cam.put("Tarifa", res.getString(res.getColumnIndexOrThrow("Tarifa")))
            cam.put("RGB", res.getString(res.getColumnIndexOrThrow("RGB")))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cam
    }

    override fun cargarValues(o: JSONObject): ContentValues {
        val values = ContentValues()
        try {
            values.put("ID", o.getInt("id"))
            values.put("Nombre", o.getString("nombre"))
            values.put("RGB", o.getString("rgb"))
            values.put("Tarifa", o.getString("tarifa"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return values
    }


}
