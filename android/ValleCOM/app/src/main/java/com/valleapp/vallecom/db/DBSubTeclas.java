package com.valleapp.vallecom.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by valle on 13/10/14.
 */
public class DBSubTeclas extends DBBase {


    public DBSubTeclas(Context context) {
        super(context, "subteclas");
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS subteclas (ID INTEGER PRIMARY KEY, " +
                " Nombre TEXT, Incremento DOUBLE, IDTecla INTEGER," +
                " descripcion_t TEXT, descripcion_r TEXT)");
    }


    public JSONArray getAll(String id)
    {
        return filter("IDTecla="+id );
    }


    @SuppressLint("Range")
    @Override
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
            obj.put("Incremento", res.getString(res.getColumnIndex("Incremento")));
            obj.put("descripcion_t", res.getString(res.getColumnIndex("descripcion_t")));
            obj.put("descripcion_r", res.getString(res.getColumnIndex("descripcion_r")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return  obj;
    }

    @Override
    protected ContentValues caragarValues(JSONObject o) {
        ContentValues values = new ContentValues();
        try {
            values.put("ID", o.getInt("id"));
            values.put("Nombre", o.getString("nombre"));
            values.put("Incremento",o.getString("incremento"));
            values.put("IDTecla", o.getString("tecla"));
            values.put("descripcion_r", o.getString("descripcion_r"));
            values.put("descripcion_t", o.getString("descripcion_t"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return values;
    }
}
