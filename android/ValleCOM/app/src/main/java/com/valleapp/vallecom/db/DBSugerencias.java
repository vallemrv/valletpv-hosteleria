package com.valleapp.vallecom.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by valle on 13/10/14.
 */
public class DBSugerencias extends DBBase {


    public String tb_name = "sugerencias";


    public DBSugerencias(Context context) {
        super(context, "sugerencias");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ tb_name +" (ID TEXT PRIMARY KEY, IDTecla TEXT, sugerencia TEXT )");
    }

    @SuppressLint("Range")
    @Override
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("ID", res.getString(res.getColumnIndex("ID")));
            obj.put("sugerencia", res.getString(res.getColumnIndex("sugerencia")));
            obj.put("tecla", res.getString(res.getColumnIndex("IDTecla")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    protected ContentValues caragarValues(JSONObject o) {
        ContentValues values = new ContentValues();
        try {
            values.put("ID", o.getInt("id"));
            values.put("IDTecla",o.getString("tecla"));
            values.put("sugerencia",o.getString("sugerencia"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return  values;
    }
    


    public JSONArray getAll()
    {
        return filter(null);
    }


}
