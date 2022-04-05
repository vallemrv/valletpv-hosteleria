package com.valleapp.comandas.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.valleapp.comandas.interfaces.IBaseDatos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


/**
 * Created by valle on 13/10/14.
 */
public class DbMesas extends SQLiteOpenHelper implements IBaseDatos {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";


    public DbMesas(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS mesas " +
                "(ID INTEGER PRIMARY KEY, Nombre TEXT, RGB TEXT, " +
                "abierta TEXT,  IDZona INTEGER, " +
                "num INTEGER, flag TEXT default '', Orden INTEGER)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS mesas");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void rellenarTabla(JSONArray datos){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try{
          db.execSQL("DELETE FROM mesas");
        }catch (SQLiteException e){
            this.onCreate(db);
        }
        // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i<datos.length();i++){
            // Create a new map of values, where column names are the keys
            try {
                String abierta = datos.getJSONObject(i).getString("abierta").toLowerCase(Locale.ROOT);
                if (abierta.equals("true")) abierta = "1";
                else if (abierta.equals("false")) abierta = "0";
                ContentValues values = new ContentValues();
                values.put("ID", datos.getJSONObject(i).getInt("ID"));
                values.put("Nombre", datos.getJSONObject(i).getString("Nombre"));
                values.put("IDZona", datos.getJSONObject(i).getInt("IDZona"));
                values.put("RGB", datos.getJSONObject(i).getString("RGB"));
                values.put("abierta",abierta);
                values.put("num", datos.getJSONObject(i).getInt("num"));
                values.put("Orden", datos.getJSONObject(i).getInt("Orden"));
                db.insert("mesas", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        db.close();
    }
    @SuppressLint("Range")
    public JSONArray getAll(String id)
    {
        return filter("IDZona = "+id);
    }


    public void abrirMesa(String idm) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE mesas SET abierta='1', num=0 WHERE ID="+idm);
        db.close();
    }

    public void cerrarMesa(String idm) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE mesas SET abierta='0', num=0 WHERE ID="+idm);
        db.close();
    }

    public JSONArray getAllMenosUna(String id, String idm) {
        return filter("IDZona = "+id+" AND ID !=  "+idm);
    }

    @Override
    public void resetFlag(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues v = new ContentValues();
        v.put("flag", "");
        db.update("mesas", v, "ID=?", new String[]{String.valueOf(id)});
    }

    @SuppressLint("Range")
    @Override
    public JSONArray filter(String cWhere) {
        JSONArray lista = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        String strWhere = "";
        if (cWhere != null){
            strWhere = " WHERE "+cWhere;
        }

        Cursor res =  db.rawQuery( "SELECT * FROM mesas "+strWhere+" ORDER BY Orden DESC", null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            try{
                JSONObject obj = new JSONObject();
                int num = res.getInt(res.getColumnIndex("num"));
                String RGB = res.getString(res.getColumnIndex("RGB"));
                obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
                obj.put("IDZoma", res.getString(res.getColumnIndex("IDZona")));
                obj.put("RGB", num<=0 ? RGB : "255,0,0");
                obj.put("abierta", res.getString(res.getColumnIndex("abierta")));
                obj.put("ID", res.getString(res.getColumnIndex("ID")));
                obj.put("num", num);
                lista.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        res.close();db.close();
        return lista;
    }


}
