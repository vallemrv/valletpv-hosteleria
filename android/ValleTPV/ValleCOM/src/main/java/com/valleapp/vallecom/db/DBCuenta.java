package com.valleapp.vallecom.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by valle on 13/10/14.
 */
public class DBCuenta extends DBBase  {

    public DBCuenta(Context context) {
        super(context, "cuenta");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS cuenta " +
            "(ID TEXT PRIMARY KEY, Estado TEXT, " +
            "Descripcion TEXT, descripcion_t TEXT, Precio DOUBLE, IDPedido INTEGER, " +
            "IDMesa INTEGER," +
            "IDArt INTEGER," +
            "nomMesa TEXT, IDZona TEXT," +
            "servido INTEGER, receptor INTEGER, camarero INTEGER )");

    }

    @SuppressLint("Range")
    @Override
    protected JSONObject cursorToJSON(Cursor res) {
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
            obj.put("camarero", res.getString(res.getColumnIndex("camarero")));
            obj.put("receptor", res.getString(res.getColumnIndex("receptor")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }


    public List<JSONObject> filterList(String cWhere, boolean gr) {
        List<JSONObject> lista = new ArrayList<>();
        try {
            String strWhere = "";
            if (cWhere != null){
                strWhere = " WHERE "+ cWhere;
            }

            String gr_str = "";
            if (gr){
                gr_str = "IDPedido, ";
            }
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res = db.rawQuery("SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total" +
                    " FROM cuenta " + strWhere +
                    " GROUP BY  IDArt, Descripcion, Precio, "+gr_str+" Estado ORDER BY ID DESC", null);
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

    public JSONArray filterByPedidos(String cWhere) {

        String strWhere = "";
        if (cWhere != null){
            strWhere = " WHERE "+ cWhere;
        }
        return execSql("SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total FROM cuenta " + strWhere +
                " GROUP BY  IDArt, Descripcion, Precio, Estado, IDPedido ORDER BY ID DESC");
    }

    public JSONArray filterByPedidos(String cWhere, String group_by) {

        String strWhere = "";
        if (cWhere != null){
            strWhere = " WHERE "+ cWhere;
        }
        return execSql("SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total FROM cuenta " + strWhere +
                " GROUP BY "+group_by+" ORDER BY ID DESC");
    }

    @Override
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
            values.put("camarero", o.getString("camarero"));
            values.put("receptor", o.getString("receptor"));
        }catch (Exception e){
            Log.d("CUENTA-CARGARVALUES",  e.getMessage());
        }
        return values;
    }

    public void actualizarMesa(JSONArray datos, String IDMesa){
        // Gets the data repository in write mode
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM cuenta WHERE IDMesa=" + IDMesa);
            for (int i=0;i < datos.length(); i++) {
                insert(datos.getJSONObject(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<JSONObject> getAll(String id, boolean gr) {
        return filterList("IDMesa ="+id+" AND (estado = 'N' or estado ='P')", gr);
    }

    public double getTotal(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        double s = 0.0;
        try {
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT SUM(Precio) AS TotalTicket " +
                    "FROM cuenta WHERE IDMesa=" + id+ " AND (estado = 'N' or estado ='P')", null);
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
            db.update("cuenta", values, "IDMesa="+id, null);
         }catch (SQLiteException e){
            e.printStackTrace();
         }

    }

    public JSONArray execSql(String sql) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery(sql, null);
        JSONArray ls = new JSONArray();
        res.moveToFirst();
        while (!res.isAfterLast()){
            ls.put(cursorToJSON(res));
            res.moveToNext();
        }
        return  ls;
    }


    public void moverLinea(JSONObject m, JSONObject linea) {
        try {
            SQLiteDatabase db = getWritableDatabase();

            boolean abierta = m.getString("abierta").equals("1");
            String idm = m.getString("ID");
            String idz = m.getString("IDZona");
            String nomMesa = m.getString("Nombre");
            String idlinea = linea.getString("ID");

            //camibar la linea de mesa.
            ContentValues p = new ContentValues();
            p.put("IDMesa", idm);
            p.put("IDZona", idz);
            p.put("nomMesa", nomMesa);
            db.update("cuenta", p, "ID = ?", new String[]{idlinea});

            if (!abierta){
                p = new ContentValues();
                p.put("abierta", "1");
                db.update("mesas", p, "ID = ?", new String[]{idm});
            }


            //Ver si hay que cerrar la mesas
            String idm2 = linea.getString("IDMesa");
            int count= count( db,"IDMesa="+idm2);
            if (count <= 0){
                p = new ContentValues();
                p.put("abierta", "0");
                db.update("mesas", p, "ID = ?", new String[]{idm2});
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void artServido(JSONObject obj) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues p = new ContentValues();
            p.put("servido", 1);
            db.update("cuenta", p, "IDArt = ? AND Descripcion = ? AND Precio = ? AND IDPedido = ? ",
                    new String[]{obj.getString("IDArt"), obj.getString("Descripcion"),
                            obj.getString("Precio"), obj.getString("IDPedido")});
        }catch (Exception e){
           e.printStackTrace();
        }

    }

    @SuppressLint("Range")
    public ArrayList<JSONObject> getPedidosChoices(String idMesa) {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<JSONObject>  ls = new ArrayList<> ();
        try {
            @SuppressLint("Recycle") Cursor res = db.rawQuery("SELECT *, COUNT(ID) AS Can FROM cuenta  WHERE estado ='P' AND IDMesa=" + idMesa +
                    " GROUP BY  IDArt, Descripcion, Precio, Estado, IDPedido ORDER BY IDPedido", null);


            res.moveToFirst();
            int max = 3;
            int count = 0;
            int id_aux = -1;
            StringBuilder subtilte = new StringBuilder();
            JSONObject o = null;

            while (!res.isAfterLast()){
                int idPedido = res.getInt(res.getColumnIndex("IDPedido"));
                String can = res.getString(res.getColumnIndex("Can"));
                String nombre =  res.getString(res.getColumnIndex("Descripcion"));
                if (id_aux != idPedido) {
                    id_aux = idPedido;
                    if (o != null && !subtilte.toString().equals("")){
                        subtilte.append(", etc..");
                        o.put("subtitle", subtilte.toString());
                    }
                    o = new JSONObject();
                    o.put("IDPedido", id_aux);
                    ls.add(o);
                    count = 0;
                    subtilte = new StringBuilder();
                }
                if (count < max){
                    if (!subtilte.toString().equals("")) subtilte.append(", ");
                    subtilte.append(can).append(" ").append(nombre);
                    count++;
                }
                res.moveToNext();
            }
            if (o != null && !subtilte.toString().equals("")){
                subtilte.append(", etc..");
                o.put("subtitle", subtilte.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return  ls;
    }


    public void servirPeido(String idp, String idr) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues p = new ContentValues();
            p.put("servido", 1);
            db.update("cuenta", p, "IDPedido = ?  AND receptor= ?", new String[]{idp, idr});
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addArt(String idm, List<JSONObject> nl, String idc, String nom_mesa) {
        SQLiteDatabase db = getWritableDatabase();
        @SuppressLint("Recycle") Cursor res = db.rawQuery("SELECT ID FROM receptores LIMIT 1", null);
        res.moveToFirst();
        int IDReceptor = res.getInt(0);

        try {
            for (JSONObject o : nl) {
                int can = o.getInt("Can");
                for(int i=0; i < can; i++) {
                    ContentValues values = new ContentValues();
                    values.put("ID", UUID.randomUUID().toString());
                    values.put("IDArt", o.getString("ID"));
                    values.put("Descripcion", o.getString("Descripcion"));
                    values.put("descripcion_t", o.getString("descripcion_t"));
                    values.put("Precio", o.getDouble("Precio"));
                    values.put("IDMesa", idm);
                    values.put("IDZona", "");
                    values.put("nomMesa",  nom_mesa);
                    values.put("IDPedido", "0");
                    values.put("Estado", "N");
                    values.put("servido", "0");
                    values.put("camarero", idc);
                    values.put("receptor", IDReceptor);
                    db.insert(tb_name, null, values);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
