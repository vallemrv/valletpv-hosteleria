package com.valleapp.valletpvlib.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.json.JSONObject

class DBReceptores(context: Context) : DBBase(context, "receptores") {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS receptores (ID INTEGER PRIMARY KEY, nombre TEXT, nomimp TEXT)")
    }

    override fun cursorToJSON(res: Cursor): JSONObject {
        val receptor = JSONObject()
        try {
            receptor.put("nombre", res.getString(with(res) { getColumnIndex("nombre") }))
            receptor.put("nomimp", res.getString(with(res) { getColumnIndex("nomimp") }))
            receptor.put("ID", res.getString(with(res) { getColumnIndex("ID") }))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return receptor
    }

    override fun cargarValues(o: JSONObject): ContentValues {
        val v = ContentValues()
        try {
            v.put("nombre", o.getString("nombre"))
            v.put("nomimp", o.getString("nomimp"))
            v.put("ID", o.getString("id"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return v
    }

    fun getAll(): ArrayList<JSONObject> {
        val db = this.readableDatabase
        val res = db.rawQuery("select * from receptores WHERE nomimp!='Nulo' and nomimp!='None' and nomimp!='' and nombre!='Ticket' " , null)
        val ls = ArrayList<JSONObject>()
        try {
            res.moveToFirst()
            while (!res.isAfterLast) {
                ls.add(cursorToJSON(res))
                res.moveToNext()
            }
        } finally {
            res.close()
        }
        return ls
    }

}