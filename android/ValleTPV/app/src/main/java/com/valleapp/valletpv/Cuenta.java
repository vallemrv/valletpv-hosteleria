package com.valleapp.valletpv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.valleapp.valletpv.dlg.DlgPedirAutorizacion;
import com.valleapp.valletpv.interfaces.IAutoFinish;
import com.valleapp.valletpv.interfaces.IControladorAutorizaciones;
import com.valleapp.valletpv.interfaces.IControladorCuenta;
import com.valleapp.valletpv.tools.JSON;
import com.valleapp.valletpv.tools.ServicioCom;
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorTicket;
import com.valleapp.valletpv.db.DbCuenta;
import com.valleapp.valletpv.db.DbMesas;
import com.valleapp.valletpv.db.DbSecciones;
import com.valleapp.valletpv.db.DbTeclas;
import com.valleapp.valletpv.dlg.DlgCobrar;
import com.valleapp.valletpv.dlg.DlgSepararTicket;
import com.valleapp.valletpv.dlg.DlgVarios;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Cuenta extends Activity implements TextWatcher, IControladorCuenta, IControladorAutorizaciones, IAutoFinish {

    private String server = "";
    DbSecciones dbSecciones;
    DbTeclas dbTeclas;
    DbCuenta dbCuenta;
    DbMesas dbMesas;

    JSONObject cam = null;
    JSONObject mesa = null;

    List<JSONObject> lineas = null;
    JSONArray lsartresul = null;

    Double totalMesa = 0.00;

    String tipo = "";
    String sec = "";
    int cantidad = 1;
    Boolean reset = true;
    Boolean stop = false;
    Timer timerAutoCancel = new Timer();

    private long autoCancel = 10000;

    ServicioCom myServicio;

    final Context cx = this;

    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                myServicio = ((ServicioCom.MyBinder) iBinder).getService();
                myServicio.setExHandler("lineaspedido", handlerHttp);
                dbCuenta = (DbCuenta) myServicio.getDb("lineaspedido");
                dbMesas = (DbMesas) myServicio.getDb("mesas");
                dbSecciones = (DbSecciones) myServicio.getDb("secciones");
                dbTeclas = (DbTeclas) myServicio.getDb("teclas");
                rellenarSecciones();
                rellenarTicket();


                if (tipo.equals("c"))
                    mostarCobrar(dbCuenta.filter("IDMesa=" + mesa.getInt("ID")), totalMesa);

                get_cuenta();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler mostrarBusqueda= new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            rellenarArticulos(lsartresul);
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler handlerHttp = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                setEstadoAutoFinish(true, false);
                String res = msg.getData().getString("RESPONSE");
                if (res != null) {
                    JSONArray datos = new JSONArray(res);
                    synchronized (dbCuenta) {
                        dbCuenta.replaceMesa(datos, mesa.getString("ID"));
                        rellenarTicket();
                    }
                }else{
                    get_cuenta();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    //Inicializacion de estados y vista onResume
    private void rellenarSecciones() {

        try{

            JSONArray lssec = dbSecciones.getAll();

            if(lssec.length()>0){

                LinearLayout ll = (LinearLayout)findViewById(R.id.pneSecciones);
                ll.removeAllViews();

                DisplayMetrics metrics = getResources().getDisplayMetrics();

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,Math.round(metrics.density * 100));

                params.setMargins(5,5,5,5);


                for (int i = 0; i < lssec.length(); i++) {
                    JSONObject  z =  lssec.getJSONObject(i);

                    if(sec.equals("") && i==0) sec =  z.getString("ID");

                    Button btn = new Button(cx);
                    btn.setId(z.getInt("ID"));
                    btn.setSingleLine(false);
                    btn.setText(z.getString("Nombre"));
                    btn.setTag(z.getString("ID"));
                    btn.setTextSize(16);
                    String[] rgb = z.getString("RGB").trim().split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

                    btn.setOnClickListener(view -> {
                        setEstadoAutoFinish(true, false);
                        sec =  view.getTag().toString();
                        try {
                            JSONArray  lsart = dbTeclas.getAll(sec,mesa.getInt("Tarifa"));
                            rellenarArticulos(lsart);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });

                    btn.setOnLongClickListener(view -> {
                        asociarBotonera(view);
                        return false;
                    });
                    ll.addView(btn, params);
                }

                JSONArray lsart = dbTeclas.getAll(sec,mesa.getInt("Tarifa"));
                rellenarArticulos(lsart);

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void cargarPreferencias() {
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            if(!pref.isNull("sec")) {
                sec = pref.getString("sec");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void get_cuenta(){
        if(myServicio!=null & mesa != null) {
            try {
                myServicio.get_cuenta(handlerHttp, mesa.getString("ID"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Mostrar datos de articulos y ticket
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void rellenarArticulos(JSONArray lsart) {
        try {

            if(lsart.length()>0){

                TableLayout ll = (TableLayout)findViewById(R.id.pneArt);
                ll.removeAllViews();

                DisplayMetrics metrics = getResources().getDisplayMetrics();

                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,Math.round(metrics.density * 100));


                LinearLayout.LayoutParams rowparams = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT);

                rowparams.setMargins(5,5,5,5);
                rowparams.weight = 1;

                LinearLayout row = new LinearLayout(cx);
                row.setOrientation(LinearLayout.HORIZONTAL);


                ll.addView(row, params);


                for (int i = 0; i < lsart.length(); i++) {

                    final JSONObject  m =  lsart.getJSONObject(i);


                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);
                    @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.boton_art, null);
                    Button btn = (Button)v.findViewById(R.id.btnArt);

                    btn.setId(i);
                    btn.setTag(m);

                    btn.setText(m.getString("Nombre")+"\n"+String.format("%01.2f €",m.getDouble("Precio")));

                    String[] rgb = m.getString("RGB").split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

                    btn.setOnClickListener(view -> {
                        try {
                            JSONObject art = (JSONObject) view.getTag();
                            art.put("Can", cantidad);
                            pedirArt(art);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    });
                    row.addView(v, rowparams);

                    if (((i+1) % 5) == 0) {
                        row = new LinearLayout(cx);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setMinimumHeight(130);

                        ll.addView(row, params);
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    private void rellenarTicket() {
        try {
            synchronized (dbCuenta) {
                resetCantidad();

                TextView l = findViewById(R.id.lblPrecio);
                ListView lst = findViewById(R.id.lstCamareros);

                lineas = dbCuenta.getAll(mesa.getString("ID"));
                totalMesa = dbCuenta.getTotal(mesa.getString("ID"));

                l.setText(String.format("%01.2f €", totalMesa));

                lst.setAdapter(new AdaptadorTicket(cx, (ArrayList<JSONObject>) lineas, this));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Dialogos
    public void mostrarVarios(View v) {
        setEstadoAutoFinish(true, true);
        DlgVarios dlg= new DlgVarios(this, this);
        dlg.show();
    }

    public void mostrarSeparados(View v) {
        // TODO Auto-generated method stub
        if( totalMesa > 0) {
            try {
                setEstadoAutoFinish(true, true);
                this.aparcar(mesa.getString("ID"), dbCuenta.getNuevos(mesa.getString("ID")));
                lineas = dbCuenta.getAll(mesa.getString("ID"));
                DlgSepararTicket dlg = new DlgSepararTicket(this,this);
                dlg.setTitle("Separar ticket " + mesa.getString("Nombre"));
                dlg.setLineasTicket(lineas);
                dlg.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Acciones con el servidor
    public void preImprimir(View v){
        try{
            setEstadoAutoFinish(false, false);
            aparcar(mesa.getString("ID"), dbCuenta.getNuevos(mesa.getString("ID")));
            lineas = dbCuenta.getAll(mesa.getString("ID"));
            if(totalMesa>0) {
                ContentValues p = new ContentValues();
                p.put("idm", mesa.getString("ID"));
                if(myServicio!=null) myServicio.preImprimir(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void abrirCajon(View v){
        setEstadoAutoFinish(false, false);
        if(myServicio!=null) myServicio.abrirCajon();
    }

    public void cobrarMesa(View v){
        try {
            setEstadoAutoFinish(true, true);
            aparcar(mesa.getString("ID"), dbCuenta.getNuevos(mesa.getString("ID")));
            JSONArray l = dbCuenta.filter("IDMesa="+mesa.getString("ID"));
            mostarCobrar(l, totalMesa);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void aparcar(String idm, JSONArray nuevos) throws JSONException {
        if(nuevos.length()>0) {
            ContentValues p = new ContentValues();
            p.put("idm", idm);
            p.put("idc", cam.getString("ID"));
            p.put("pedido", nuevos.toString());
            if (myServicio != null) {
                myServicio.nuevoPedido(p);
                dbCuenta.aparcar(idm);
                dbMesas.abrirMesa(idm);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void clickCantidad(View v){
        setEstadoAutoFinish(true, false);
        cantidad = Integer.parseInt(((Button) v).getText().toString());
        TextView lbl = findViewById(R.id.lblCantida);
        lbl.setText("Cantidad "+cantidad);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void pedirArt(JSONObject art) {
        try {
            setEstadoAutoFinish(true, false);
            dbCuenta.addArt(mesa.getInt("ID"), art);
            rellenarTicket();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);
        cargarPreferencias();

        EditText bus = findViewById(R.id.txtBuscar);
        bus.addTextChangedListener(this);

        try {
           server = getIntent().getExtras().getString("url");
           cam = new JSONObject(getIntent().getExtras().getString("cam"));
           mesa = new JSONObject(getIntent().getExtras().getString("mesa"));
           tipo = getIntent().getExtras().getString("op");
           TextView title = (TextView)findViewById(R.id.txtTitulo);
           title.setText(cam.getString("Nombre") +  " -- "+mesa.getString("Nombre"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {  }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        reset = true;
        if(charSequence.length()>0) {
            try {
                final String str = charSequence.toString();
                final String t =  mesa.getString("Tarifa");
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    lsartresul = dbTeclas.findLike(str, t);
                    mostrarBusqueda.sendEmptyMessage(1);
                 }).start();
             } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) { }

    @Override
    protected void onPause() {
        if(timerAutoCancel!=null){
            timerAutoCancel.cancel();
            timerAutoCancel = null;
        }
        try{
            String idm = mesa.getString("ID");
            aparcar(idm, dbCuenta.getNuevos(idm));
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(timerAutoCancel!=null) {
            timerAutoCancel.cancel();
            timerAutoCancel=null;
        }
        unbindService(mConexion);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        try {

             if(timerAutoCancel==null) timerAutoCancel = new Timer();
             timerAutoCancel.schedule(new TimerTask()
            {
                @Override
                public void run() {
                    if(!stop) {
                        if (!reset) finish();
                        else reset = false;
                    }
                }
            },5000,autoCancel);

            Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
            intent.putExtra("url", server);
            bindService(intent, mConexion, Context.BIND_AUTO_CREATE);



            } catch (Exception e) {
                e.printStackTrace();
            }
        super.onResume();
    }

    @Override
    public void setEstadoAutoFinish(boolean r, boolean s) {
       reset = r;
       stop = s;
    }

    @Override
    public void mostarCobrar(JSONArray lsart, Double totalCobro) {
        if(totalCobro>0) {
            try {
                setEstadoAutoFinish(true, true);
                DlgCobrar dlg = new DlgCobrar(this, this);
                dlg.setTitle("Cobrar " + mesa.getString("Nombre"));
                dlg.setDatos(lsart, totalCobro);
                dlg.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cobrar(JSONArray lsart, Double totalCobro, Double entrega) {
        try {
            setEstadoAutoFinish(true, false);
            ContentValues p = new ContentValues();
            p.put("idm", mesa.getString("ID"));
            p.put("idc", cam.getString("ID"));
            p.put("entrega", Double.toString(entrega));
            p.put("art", lsart.toString());
            dbCuenta.eliminar(mesa.getString("ID"), lsart);
            if(myServicio!=null) {
                myServicio.cobrarCuenta(p);
                if (totalCobro - totalMesa == 0) {
                    dbMesas.cerrarMesa(mesa.getString("ID"));
                    finish();
                } else {
                    rellenarTicket();
                }
                sendMessageMesaCobrada(entrega, entrega-totalCobro);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }




    @Override
    public void clickMostrarBorrar(final JSONObject art) {

            setEstadoAutoFinish(true,true);

            final Dialog dlg = new Dialog(cx);
            dlg.setContentView(R.layout.borrar_art);

            dlg.setOnCancelListener(dialogInterface -> {
                setEstadoAutoFinish(true,false);
            });

            dlg.setTitle("Borrar articulos");
            final EditText motivo = dlg.findViewById(R.id.txtMotivo);
            final Button error =  dlg.findViewById(R.id.btnError);
            final Button simpa =  dlg.findViewById(R.id.btnSimpa);
            final Button inv = dlg.findViewById(R.id.btnInv);
            final Button ok =  dlg.findViewById(R.id.btnOk);
            final ImageButton edit =  dlg.findViewById(R.id.btnEdit);
            final ImageButton exit =  dlg.findViewById(R.id.btnSalir);

            final LinearLayout pneEdit =  dlg.findViewById(R.id.pneEditarMotivo);
            final TextView txtInfo = dlg.findViewById(R.id.txt_info_borrar);
            try {
                Integer canArt = art.getInt("Can");
                if (cantidad > canArt) cantidad = canArt;
                art.put("Can", cantidad);
                txtInfo.setText("Borrar " + cantidad + " "+art.getString("Nombre"));
                resetCantidad();
            }catch (Exception e){
                e.printStackTrace();
            }

            pneEdit.setVisibility(View.GONE);

            exit.setOnClickListener(view -> dlg.cancel());

            edit.setOnClickListener(view -> {
                if(pneEdit.getVisibility() == View.VISIBLE)  pneEdit.setVisibility(View.GONE);
                else pneEdit.setVisibility(View.VISIBLE);
            });

            ok.setOnClickListener(view -> {
                if (motivo.getText().length() > 0) {
                    borrarLinea(art, motivo.getText().toString());
                    dlg.cancel();

                }
            });

            error.setOnClickListener(view -> {
                borrarLinea(art, error.getText().toString());
                dlg.cancel();


            });

            simpa.setOnClickListener(view -> {
                borrarLinea(art, simpa.getText().toString());
                dlg.cancel();
            });

            inv.setOnClickListener(view -> {
                borrarLinea(art, inv.getText().toString());
                dlg.cancel();
           });

            dlg.show();

    }

    @Override
    public void borrarArticulo(JSONObject art) throws JSONException {
         reset = true;
         art.put("Can",1);
         dbCuenta.eliminar(mesa.getString("ID"), new JSONArray().put(art));
         rellenarTicket();
    }

    @Override
    public void pedirAutorizacion(ContentValues params) {
        myServicio.pedirAutorizacion(params, handlerHttp, "procesar_autorizacion");
    }

    @Override
    public void pedirAutorizacion(String id) {

    }

    // Utilidades
    public void resetCantidad(){
        cantidad = 1;
        TextView lbl = (TextView) findViewById(R.id.lblCantida);
        lbl.setText("Cantidad "+ cantidad);
    }

    private void asociarBotonera(View view) {
        reset = true;
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            pref.put("sec",Integer.toString(view.getId()));
            json.serializar("preferencias.dat", pref, cx);
            Toast toast= Toast.makeText(getApplicationContext(),
                    "Asocicion realizada", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 200);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void borrarLinea(final JSONObject art, String motivo){
        try{

            if (myServicio != null){
                JSONObject p = new JSONObject();
                p.put("idm",  mesa.getString("ID"));
                p.put("Precio", art.getString("Precio"));
                p.put("idArt", art.getString("IDArt"));
                p.put("can", art.getString("Can"));
                p.put("idc", cam.getString("ID"));
                p.put("motivo", motivo);
                p.put("Estado", art.getString("Estado"));
                p.put("Nombre", art.getString("Nombre"));
                DlgPedirAutorizacion dlg = new DlgPedirAutorizacion(cx, this,
                        myServicio.getDb("camareros"), this,
                        p, "borrar_linea");
                dlg.show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendMessageMesaCobrada(Double entrega, double cambio) {
        Handler handlerMesas = myServicio.getExHandler("camareros");
        Message msg = handlerMesas.obtainMessage();
        Bundle bundle = msg.getData();
        if (bundle == null) bundle = new Bundle();
        bundle.putString("op", "show_info_cobro");
        bundle.putDouble("entrega", entrega );
        bundle.putDouble("cambio", cambio);
        msg.setData(bundle);
        handlerMesas.sendMessage(msg);
    }



}