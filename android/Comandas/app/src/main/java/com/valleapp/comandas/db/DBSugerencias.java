package com.valleapp.comandas.db;

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
        super(context);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ tb_name +" (ID TEXT PRIMARY KEY, IDTecla TEXT, sugerencia TEXT )");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + tb_name);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    @SuppressLint("Range")
    @Override
    public JSONArray filter(String cWhere){
        JSONArray ls = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        if (cWhere != null) cWhere = " WHERE "+ cWhere;
        else cWhere = "";
        Cursor res =  db.rawQuery( "select * from "+ tb_name + cWhere, null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            try{
                JSONObject obj = new JSONObject();
                obj.put("ID", res.getString(res.getColumnIndex("ID")));
                obj.put("sugerencia", res.getString(res.getColumnIndex("sugerencia")));
                obj.put("IDTecla", res.getString(res.getColumnIndex("IDTecla")));
                ls.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            res.moveToNext();

        }
        res.close();
        db.close();
        return ls;
    }

    @Override
    public void rellenarTabla(JSONArray tb){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try{
          db.execSQL("DELETE FROM "+ tb_name);
        }catch (SQLiteException e){
            this.onCreate(db);
        }
       // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i < tb.length(); i++){
            // Create a new map of values, where column names are the keys
            try {
                ContentValues values = new ContentValues();
                values.put("ID", tb.getJSONObject(i).getInt("id"));
                values.put("IDTecla", tb.getJSONObject(i).getString("tecla"));
                values.put("sugerencia", tb.getJSONObject(i).getString("sugerencia"));
                 db.insert(tb_name, null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        db.close();
    }

    public JSONArray getAll()
    {
        return filter(null);
    }


}
