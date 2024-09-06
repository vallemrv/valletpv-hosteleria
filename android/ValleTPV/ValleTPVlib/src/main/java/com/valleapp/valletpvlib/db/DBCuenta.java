package com.valleapp.valletpvlib.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by valle on 13/10/14.
 */
public class DBCuenta extends DBBase{

    public DBCuenta(Context context) {
        super(context, "cuenta");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
          db.execSQL("CREATE TABLE IF NOT EXISTS cuenta " +
                "(ID TEXT PRIMARY KEY, Estado TEXT, " +
                "Descripcion TEXT, descripcion_t TEXT, " +
                "Precio DOUBLE, IDPedido INTEGER, " +
                "IDMesa INTEGER," +
                "IDArt INTEGER," +
                "nomMesa TEXT, IDZona TEXT," +
                "servido INTEGER )");
    }

    @Override
    public void rellenarTabla(JSONArray objs) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM "+tb_name +" WHERE Estado != 'N'");
        for (int i= 0 ; i < objs.length(); i++){
            // Create a new map of values, where column names are the keys
            try {
                insert(objs.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public JSONArray filterGroup(String cWhere) {
        JSONArray lista = new JSONArray();
        try {
            String strWhere = "";
            if (cWhere != null){
                strWhere = " WHERE "+ cWhere;
            }
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res = db.rawQuery("SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total" +
                    " FROM cuenta " + strWhere +
                    " GROUP BY  IDArt, Descripcion, Precio, Estado ORDER BY ID DESC", null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                lista.put(cursorToJSON(res));
                res.moveToNext();
            }

        }catch (SQLiteException e){
            e.printStackTrace();
        }
        return lista;
    }

    public List<JSONObject> filterList(String cWhere) {
        List<JSONObject> lista = new ArrayList<>();
        try {
            String strWhere = "";
            if (cWhere != null){
                strWhere = " WHERE "+ cWhere;
            }
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res = db.rawQuery("SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total" +
                    " FROM cuenta " + strWhere +
                    " GROUP BY  IDArt, Descripcion, Precio, Estado ORDER BY ID DESC", null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                lista.add(cursorToJSON(res));
                res.moveToNext();
            }

        }catch (SQLiteException e){
            e.printStackTrace();
        }
        return lista;
    }


    protected ContentValues caragarValues(JSONObject o){
        ContentValues values = new ContentValues();
        try {
            values.put("ID", o.getString("ID"));
            values.put("IDArt", o.getInt("IDArt"));
            values.put("Descripcion", o.getString("Descripcion"));
            values.put("descripcion_t", o.getString("descripcion_t"));
            values.put("Precio", o.getDouble("Precio"));
            values.put("IDMesa", o.getString("IDMesa"));
            values.put("IDZona", o.getString("IDZona"));
            values.put("nomMesa", o.getString("nomMesa"));
            values.put("IDPedido", o.getString("IDPedido"));
            values.put("Estado", o.getString("Estado"));
            values.put("servido", o.getString("servido"));
        }catch (Exception e){
            Log.d("CUENTA-CARGARVALUES",  e.getMessage());
        }
        return values;
    }

    public List<JSONObject> getAll(String id) {
        return filterList("IDMesa ="+id+" AND (Estado = 'N' or Estado = 'P')" );
    }

    public double getTotal(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        double s = 0.0;
        try {
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT SUM(Precio) AS TotalTicket " +
                    "FROM cuenta WHERE IDMesa=" + id+ " AND (Estado = 'N' or Estado = 'P')", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
                s = cursor.getDouble(0);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return s;
    }

    public void cambiarCuenta(String id, String id1) {
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            ContentValues values = new ContentValues();
            values.put("IDMesa", id1);
            db.update("cuenta", values, "IDMesa="+id +" AND estado != 'N'", null);
        }catch (SQLiteException e){
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    protected JSONObject cursorToJSON(Cursor res){

        JSONObject obj = new JSONObject();
        try{
            obj.put("ID", res.getString(res.getColumnIndex("ID")));
            obj.put("Descripcion", res.getString(res.getColumnIndex("Descripcion")));
            obj.put("descripcion_t", res.getString(res.getColumnIndex("descripcion_t")));
            int index_can = res.getColumnIndex("Can");
            if (index_can >=  0) {
                obj.put("Can", res.getString(index_can));
            }
            int index_total = res.getColumnIndex("Total");
            if (index_total >= 0){
                obj.put("Total", res.getString(index_total));
                obj.put("CanCobro", 0);
            }
            obj.put("Precio", res.getString(res.getColumnIndex("Precio")));
            obj.put("IDArt", res.getString(res.getColumnIndex("IDArt")));
            obj.put("Estado", res.getString(res.getColumnIndex("Estado")));
            obj.put("nomMesa", res.getString(res.getColumnIndex("nomMesa")));
            obj.put("IDPedido", res.getString(res.getColumnIndex("IDPedido")));
            obj.put("servido", res.getString(res.getColumnIndex("servido")));
            obj.put("IDZona", res.getString(res.getColumnIndex("IDZona")));
            obj.put("IDMesa", res.getString(res.getColumnIndex("IDMesa")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }



    public void replaceMesa(JSONArray datos, String IDMesa){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.execSQL("DELETE FROM cuenta WHERE IDMesa=" + IDMesa +" AND Estado != 'N'");
            for(int i= 0; i < datos.length(); i++){
                insert(datos.getJSONObject(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addArt(int IDMesa,  JSONObject art){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
         // Insert the new row, returning the primary key value of the new row
        try {
            int can = art.getInt("Can");
            for(int i=0;i<can;i++) {
                ContentValues values = new ContentValues();
                values.put("ID",  UUID.randomUUID().toString());
                values.put("IDArt", art.getInt("ID"));
                values.put("Descripcion", art.getString("Descripcion"));
                values.put("descripcion_t", art.getString("descripcion_t"));
                values.put("Precio", art.getDouble("Precio"));
                values.put("IDMesa", IDMesa);
                values.put("Estado", "N");
                db.insert("cuenta", null, values);
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void eliminar(String IDMesa, JSONArray lsart){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (int i = 0; i < lsart.length(); i++){
                JSONObject art = lsart.getJSONObject(i);
                String sql = "DELETE FROM cuenta WHERE ID IN (SELECT ID FROM cuenta WHERE IDMesa="
                               + IDMesa + " AND IDArt=" + art.getString("IDArt")
                                        + " AND Descripcion='" + art.getString("Descripcion")+"'"
                                        + " AND Precio=" + art.getString("Precio")
                                        + " LIMIT "+art.getString("Can")+")";
                db.execSQL(sql);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void eliminar(String IDMesa){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM cuenta WHERE IDMesa=" + IDMesa);
        }catch (SQLiteException e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("Range")
    public JSONArray getNuevos(String id) {

        JSONArray lista = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor res = db.rawQuery("SELECT *, COUNT(ID) AS Can FROM cuenta WHERE IDMesa=" + id +" AND Estado = 'N'"+
                    " Group by IDArt, Descripcion, Precio, Estado", null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                lista.put(cursorToJSON(res));
                res.moveToNext();
            }
        }catch (SQLiteException e){
           e.printStackTrace();
        }
        return lista;
    }
}

