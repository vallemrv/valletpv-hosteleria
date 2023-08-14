package com.valleapp.valletpvlib.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import org.json.JSONArray
import org.json.JSONObject

class DBMesasAbiertas(context: Context?) : DBMesas(context) {
    override fun filter(cWhere: String): JSONArray {
        var cWhere: String? = cWhere
        if (cWhere != null && cWhere != "") {
            cWhere += " and abierta=1"
        } else {
            cWhere = "abierta=1"
        }
        return super.filter(cWhere)
    }

    override fun rellenarTabla(objs: JSONArray) {
        val sqlDb = writableDatabase
        try {
            var initialValues = ContentValues()
            initialValues.put("abierta", 0)
            initialValues.put("num", "0") //Cerramos todas las mesas
            sqlDb.update("mesas", initialValues, null, null)
            for (i in 0 until objs.length()) {
                val o = objs.getJSONObject(i)
                initialValues = ContentValues()
                val id = o.getString("ID")
                initialValues.put("abierta", 1)
                initialValues.put("num", o.getString("num"))
                sqlDb.update("mesas", initialValues, "ID=?", arrayOf(id))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("Range")
    override fun cursorToJSON(res: Cursor?): JSONObject? {
        val obj = JSONObject()
        try {
            val num = res!!.getInt(res.getColumnIndex("num"))
            obj.put("abierta", res.getString(res.getColumnIndex("abierta")))
            obj.put("ID", res.getString(res.getColumnIndex("ID")))
            obj.put("num", num)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj
    }

    override fun inicializar() {}
    override fun rm(o: JSONObject) {}
    override fun insert(o: JSONObject) {}
    override fun update(o: JSONObject) {
        try {
            val id: String
            id = if (o.has("ID")) {
                o.getString("ID")
            } else {
                o.getString("id")
            }
            val dbsql = writableDatabase
            val values = ContentValues()
            values.put("abierta", o.getString("abierta"))
            values.put("num", o.getInt("num"))
            dbsql.update("mesas", values, "ID=?", arrayOf(id))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}