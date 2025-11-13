package com.valleapp.valletpvlib.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

open class DBMesas(context: Context) : DBBase(context, "mesas") {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS mesas (
                ID INTEGER PRIMARY KEY, 
                Nombre TEXT, 
                RGB TEXT, 
                abierta TEXT,  
                IDZona INTEGER, 
                num INTEGER, 
                Orden INTEGER)"""
        )
    }

    override fun cursorToJSON(res: Cursor): JSONObject {
        val obj = JSONObject()
        try {
            val num = res.getInt(res.getColumnIndexOrThrow("num"))
            val RGB = res.getString(res.getColumnIndexOrThrow("RGB"))
            val nom = res.getString(res.getColumnIndexOrThrow("Nombre"))
            obj.put("Nombre", nom ?: "")
            obj.put("IDZona", res.getString(res.getColumnIndexOrThrow("IDZona")))
            obj.put("RGB", if (num <= 0) RGB else "255,0,0")
            obj.put("abierta", res.getString(res.getColumnIndexOrThrow("abierta")))
            obj.put("ID", res.getString(res.getColumnIndexOrThrow("ID")))
            obj.put("Orden", res.getString(res.getColumnIndexOrThrow("Orden")))
            obj.put("num", num)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj
    }

    override fun cargarValues(o: JSONObject): ContentValues {
        val values = ContentValues()
        try {
            var abierta = o.getString("abierta").lowercase(Locale.ROOT)
            abierta = when (abierta) {
                "true" -> "1"
                "false" -> "0"
                else -> abierta
            }
            values.put("ID", o.getInt("ID"))
            values.put("Nombre", o.getString("Nombre"))
            values.put("IDZona", o.getInt("IDZona"))
            values.put("RGB", o.getString("RGB"))
            values.put("abierta", abierta)
            values.put("num", o.getInt("num"))
            values.put("Orden", o.getInt("Orden"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return values
    }

    fun getAll(id: String): JSONArray {
        return filter("IDZona = $id")
    }

    fun abrirMesa(idm: String) {
        val db = writableDatabase
        db.execSQL("UPDATE mesas SET abierta='1', num=0 WHERE ID=$idm")
    }

    fun cerrarMesa(idm: String) {
        val db = writableDatabase
        db.execSQL("UPDATE mesas SET abierta='0', num=0 WHERE ID=$idm")
    }

    fun getAllMenosUna(id: String, idm: String): JSONArray {
        return filter("IDZona = $id AND ID !=  $idm")
    }

    override fun filter(cWhere: String?): JSONArray {
        val lista = JSONArray()
        val db = readableDatabase
        val strWhere = cWhere?.let { " WHERE $it" } ?: ""

        val res = db.rawQuery("SELECT * FROM mesas $strWhere ORDER BY Orden DESC", null)
        try {
            while (res.moveToNext()) {
                lista.put(cursorToJSON(res))
            }
        } finally {
            res.close()
        }
        return lista
    }

    fun marcarRojo(id: String) {
        val db = writableDatabase
        val v = ContentValues().apply {
            put("num", "1")
        }
        db.update("mesas", v, "ID = ?", arrayOf(id))
    }

}
