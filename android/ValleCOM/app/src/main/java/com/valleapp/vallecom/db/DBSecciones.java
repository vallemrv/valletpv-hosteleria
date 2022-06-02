package com.valleapp.vallecom.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by valle on 13/10/14.
 */
public class DBSecciones extends DBBase {


    public DBSecciones(Context context) {
        super(context, "secciones");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS secciones (ID INTEGER PRIMARY KEY, " +
                "nombre TEXT, " +
                "icono TEXT, es_promocion TEXT, descuento TEXT)");
    }


    @SuppressLint("Range")
    @Override
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("nombre", res.getString(res.getColumnIndex("nombre")));
            obj.put("ID", res.getString(res.getColumnIndex("ID")));
            obj.put("icono", res.getString(res.getColumnIndex("icono")));
            obj.put("es_promocion", res.getString(res.getColumnIndex("es_promocion")));
            obj.put("descuento", res.getString(res.getColumnIndex("descuento")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }



    public JSONArray getAll()
    {
       return  filter(null);
    }




    protected ContentValues caragarValues (JSONObject o){
       ContentValues values = new ContentValues();
        try{
            values.put("ID", o.getInt("id"));
            values.put("nombre", o.getString("nombre"));
            values.put("icono", o.getString("icono"));
            values.put("es_promocion", o.getString("es_promocion"));
            values.put("descuento", o.getString("descuento"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return  values;
    }
}
