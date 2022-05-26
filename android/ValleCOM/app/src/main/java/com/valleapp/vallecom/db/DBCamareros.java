package com.valleapp.vallecom.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by valle on 13/10/14.
 */
public class DBCamareros extends DBBase {

    public DBCamareros(Context context) {
        super(context, "camareros");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE IF NOT EXISTS camareros (ID INTEGER PRIMARY KEY, nombre TEXT, activo TEXT," +
                                                          "pass_field TEXT, " +
                                                          "autorizado TEXT, " +
                                                          "permisos TEXT)");
    }

    @SuppressLint("Range")
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject cam = new JSONObject();
        try {
            cam.put("Nombre", res.getString(res.getColumnIndex("nombre")));
            cam.put("ID", res.getString(res.getColumnIndex("ID")));
            cam.put("Pass", res.getString(res.getColumnIndex("pass_field")));
            cam.put("autorizado", res.getString(res.getColumnIndex("autorizado")));
            cam.put("permisos", res.getString(res.getColumnIndex("permisos")));
        }catch (Exception e) {
            e.printStackTrace();
        }
       return cam;
    }

    @Override
    protected ContentValues caragarValues(JSONObject obj) {
        ContentValues v = new ContentValues();
        try {
            String id = obj.getString("id");
            v.put("ID", id);
            v.put("activo", obj.getString("activo"));
            v.put("pass_field", obj.getString("pass_field"));
            v.put("autorizado", obj.getString("autorizado"));
            v.put("nombre", obj.getString("nombre") + " " + obj.getString("apellidos"));
            v.put("permisos", obj.getString("permisos"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return v;
    }



    public JSONArray getAll()
    {
        return filter("activo=1");
    }



    public ArrayList<JSONObject> getAutorizados(Boolean a)
    {
        String autorizado = a ? "1" : "0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from camareros WHERE activo='1' and autorizado = '"+ autorizado + "'", null );
        return cargarRegistros(res);
    }

    @SuppressLint("Range")
    private ArrayList<JSONObject> cargarRegistros(Cursor res) {

        ArrayList<JSONObject>  lscam = new ArrayList<>();
        res.moveToFirst();

        while(!res.isAfterLast()){
            lscam.add(cursorToJSON(res));
            res.moveToNext();
        }
        return lscam;
    }



    public boolean is_autorizado(JSONObject cam) {
        SQLiteDatabase db = getReadableDatabase();
        int count = 0;
        try {
            @SuppressLint("Recycle") Cursor mCount = db.rawQuery("select count(*) from camareros WHERE ID=" + cam.getString("ID") + " AND autorizado = '1'", null);
            mCount.moveToFirst();
            count = mCount.getInt(0);
        }catch (Exception e){
            e.printStackTrace();
        }

        return count > 0;
    }


}
