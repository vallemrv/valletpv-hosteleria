package com.valleapp.valletpv.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.valleapp.valletpv.interfaces.IBaseDatos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by valle on 13/10/14.
 */
public class DBSubTeclas extends SQLiteOpenHelper implements IBaseDatos {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";


    public DBSubTeclas(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS subteclas (ID INTEGER PRIMARY KEY, " +
                " Nombre TEXT, Incremento DOUBLE, IDTecla INTEGER," +
                " descripcion_t TEXT, descripcion_r TEXT)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS subteclas");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    @SuppressLint("Range")
    public JSONArray filter(String cWhere) {
        String w = "";
        if (cWhere != null){
            w = " WHERE "+cWhere;
        }
        JSONArray ls = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM subteclas "+w, null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            try{
                JSONObject obj = new JSONObject();
                obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
                obj.put("Incremento", res.getString(res.getColumnIndex("Incremento")));
                obj.put("descripcion_t", res.getString(res.getColumnIndex("descripcion_t")));
                obj.put("descripcion_r", res.getString(res.getColumnIndex("descripcion_r")));
                ls.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        return ls;
    }

    @Override
    public void rellenarTabla(JSONArray datos){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try {
           db.execSQL("DELETE FROM subteclas");
        }catch (SQLiteException e){
            this.onCreate(db);
        }
       // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i<datos.length();i++){
            // Create a new map of values, where column names are the keys
            try {
                 ContentValues values = new ContentValues();
                 values.put("ID", datos.getJSONObject(i).getInt("id"));
                 values.put("Nombre", datos.getJSONObject(i).getString("nombre"));
                 values.put("Incremento", datos.getJSONObject(i).getString("incremento"));
                 values.put("IDTecla", datos.getJSONObject(i).getString("tecla"));
                 values.put("descripcion_r", datos.getJSONObject(i).getString("descripcion_r"));
                 values.put("descripcion_t", datos.getJSONObject(i).getString("descripcion_t"));
                 db.insert("subteclas", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }


    public JSONArray getAll(String id)
    {
        return filter("IDTecla="+id );
    }

    @Override
    public void inicializar() {
        SQLiteDatabase db = this.getWritableDatabase();
        this.onCreate(db);
    }
}
