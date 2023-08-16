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
abstract class DBMesas(context: Context?) : DBBase(context, "mesas") {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS mesas " +
                    "(ID INTEGER PRIMARY KEY, Nombre TEXT, RGB TEXT, " +
                    "abierta TEXT,  IDZona INTEGER, " +
                    "num INTEGER, Orden INTEGER)"
        )
    }

    @SuppressLint("Range")
    override fun cursorToJSON(res: Cursor?): JSONObject? {
        val obj = JSONObject()
        try {
            val num = res!!.getInt(res.getColumnIndex("num"))
            val RGB = res.getString(res.getColumnIndex("RGB"))
            val nom = res.getString(res.getColumnIndex("Nombre"))
            obj.put("Nombre", nom ?: "")
            obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")))
            obj.put("IDZona", res.getString(res.getColumnIndex("IDZona")))
            obj.put("RGB", if (num <= 0) RGB else "255,0,0")
            obj.put("abierta", res.getString(res.getColumnIndex("abierta")))
            obj.put("ID", res.getString(res.getColumnIndex("ID")))
            obj.put("num", num)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj
    }

    override fun caragarValues(o: JSONObject?): ContentValues {
        val values = ContentValues()
        try {
            var abierta = o!!.getString("abierta").lowercase()
            if (abierta == "true") abierta = "1" else if (abierta == "false") abierta = "0"
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

    @SuppressLint("Range")
    fun getAll(id: String): JSONArray {
        return filter("IDZona = $id")
    }

    fun abrirMesa(idm: String) {
        val db = this.writableDatabase
        db.execSQL("UPDATE mesas SET abierta='1', num=0 WHERE ID=$idm")
    }

    fun cerrarMesa(idm: String) {
        val db = this.writableDatabase
        db.execSQL("UPDATE mesas SET abierta='0', num=0 WHERE ID=$idm")
    }

    fun getAllMenosUna(id: String, idm: String): JSONArray {
        return filter("IDZona = $id AND ID !=  $idm")
    }

    @SuppressLint("Range")
    override fun filter(cWhere: String?): JSONArray {
        val lista = JSONArray()
        val db = this.readableDatabase
        var strWhere = ""
        if (cWhere != null) {
            strWhere = " WHERE $cWhere"
        }
        val res = db.rawQuery("SELECT * FROM mesas $strWhere ORDER BY Orden DESC", null)
        res.moveToFirst()
        while (!res.isAfterLast) {
            lista.put(cursorToJSON(res))
            res.moveToNext()
        }
        return lista
    }

    fun marcarRojo(id: String) {
        val db = writableDatabase
        val v = ContentValues()
        v.put("num", "1")
        db.update("mesas", v, "ID = ?", arrayOf(id))
    }
}