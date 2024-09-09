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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.valleapp.valletpv.adaptadoresDatos.AdaptadorSettings;
import com.valleapp.valletpv.cashlogyActivitis.CambioCashlogyActivity;
import com.valleapp.valletpvlib.db.DBCamareros;
import com.valleapp.valletpvlib.db.DBCuenta;
import com.valleapp.valletpvlib.db.DBMesas;
import com.valleapp.valletpvlib.db.DBZonas;
import com.valleapp.valletpv.dlg.DlgMensajes;
import com.valleapp.valletpv.dlg.DlgPedirAutorizacion;
import com.valleapp.valletpv.dlg.DlgSelCamareros;
import com.valleapp.valletpv.interfaces.IAutoFinish;
import com.valleapp.valletpv.interfaces.IControlMensajes;
import com.valleapp.valletpv.interfaces.IControladorAutorizaciones;
import com.valleapp.valletpvlib.ServicioCom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Mesas extends Activity implements IAutoFinish, IControladorAutorizaciones, IControlMensajes {

    final Context cx = this;
    final long periodFinish = 5000;

    // Variables para salir con temporizador
    Timer autoSalir = new Timer();
    boolean stop = false;
    boolean reset = false;

    DBZonas dbZonas;
    DBMesas dbMesas;
    DBCuenta dbCuenta;

    JSONObject cam = null;
    JSONObject zn = null;
    JSONArray lsTicket = null;

    Dialog dlgListadoTicket;
    String IDTicket = "";

    AdaptadorSettings adaptadorSettings;
    ListView lista_setting;
    ServicioCom myServicio;

    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServicioCom.MyBinder)iBinder).getService();
            if(myServicio!=null){
                myServicio.setExHandler("mesas", handleHttp);
                myServicio.setExHandler("mesasabiertas", handleHttp);
                dbMesas = (DBMesas) myServicio.getDb("mesas");
                dbCuenta = (DBCuenta) myServicio.getDb("lineaspedido");
                dbZonas = (DBZonas) myServicio.getDb("zonas");
                zn = myServicio.getZona();
                rellenarZonas();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };


    @SuppressLint("HandlerLeak")
    private final Handler handleHttp = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            try {
                String op = msg.getData().getString("op");
                if (op == null) {
                     rellenarZonas();
                }
                else  {
                    String res = msg.getData().getString("RESPONSE");
                    switch (op){
                        case "get_lista_ticket":
                            lsTicket = new JSONArray(res);
                            mostrarListadoTicket(lsTicket);
                            break;
                        case "get_lineas_ticket":
                            mostrarlineasTicket(res);
                            break;
                        case "get_lista_receptores":
                            if (lista_setting != null) {
                                JSONArray lista = new JSONArray(res);
                                ArrayList<JSONObject> alista = new ArrayList<>();
                                for (int i = 0; i < lista.length(); i++) {
                                    alista.add(lista.getJSONObject(i));
                                }
                                adaptadorSettings = new AdaptadorSettings(cx, alista);
                                lista_setting.setAdapter(adaptadorSettings);
                            }
                            break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private void rellenarZonas() {
        try {

            JSONArray lszonas = dbZonas.getAll();
            if(lszonas.length()>0){

                LinearLayout ll = findViewById(R.id.pneZonas);
                ll.removeAllViews();

                DisplayMetrics metrics = getResources().getDisplayMetrics();

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,Math.round(metrics.density * 100));

                params.setMargins(5,5,5,5);

                for (int i = 0; i < lszonas.length(); i++) {
                    JSONObject  z =  lszonas.getJSONObject(i);

                    if(zn==null) zn=z;

                    Button btn = new Button(cx);
                    btn.setId(i);
                    btn.setSingleLine(false);
                    btn.setText(z.getString("Nombre"));
                    btn.setTag(z);
                    String[] rgb = z.getString("RGB").trim().split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

                    btn.setOnClickListener(view -> {
                        zn = (JSONObject)view.getTag();
                        myServicio.setZona(zn);
                        rellenarMesas();
                    });
                    ll.addView(btn, params);
                }

                rellenarMesas();

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void rellenarMesas() {
        try {
            reset = true; //reseteamos contador auto salida
            String znID = zn.getString("ID");
            JSONArray lsmesas = dbMesas.getAll(znID);
            if(lsmesas.length()>0){

                TableLayout ll = findViewById(R.id.pneMesas);
                ll.removeAllViews();

                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);

                DisplayMetrics metrics = getResources().getDisplayMetrics();


                TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                        Math.round(metrics.density * 160),
                        Math.round(metrics.density * 160));

                rowparams.setMargins(5,5,5,5);


                TableRow row = new TableRow(cx);
                ll.addView(row, params);

                for (int i = 0; i < lsmesas.length(); i++) {

                    JSONObject  m =  lsmesas.getJSONObject(i);
                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);

                    @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.boton_mesa, null);

                    ImageButton btnCm = v.findViewById(R.id.btnCambiarMesa);
                    ImageButton btnC = v.findViewById(R.id.btnAceptar);
                    ImageButton btnRm = v.findViewById(R.id.btnBorrarMesa);
                    LinearLayout panel = v.findViewById(R.id.pneBtnMesa);
                    String abierta = m.getString("abierta");

                    if(abierta.equals("0")) panel.setVisibility(View.GONE);
                    else inicializarBtnAux(btnC, btnCm, btnRm, m);

                    m.put("Tarifa",zn.getString("Tarifa"));

                    Button btn = v.findViewById(R.id.btnMesa);

                    btn.setId(i);
                    btn.setText(m.getString("Nombre"));

                    btn.setTag(m);

                    String[] rgb = m.getString("RGB").trim().split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

                    btn.setOnClickListener(view -> {
                        Intent intent = new Intent(cx, Cuenta.class);
                        JSONObject obj = (JSONObject)view.getTag();
                        intent.putExtra("op", "m");
                        intent.putExtra("cam", cam.toString());
                        intent.putExtra("mesa", obj.toString());
                        startActivity(intent);
                    });
                    row.addView(v, rowparams);

                    if (((i+1) % 5) == 0) {
                        row = new TableRow(cx);
                        ll.addView(row, params);
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void inicializarBtnAux(ImageButton btnC, ImageButton btnCm, ImageButton btnRm,   final JSONObject m) {
        btnCm.setTag(m);btnC.setTag(m);btnRm.setTag(m);
        btnCm.setOnLongClickListener(view -> {
            clickJuntarMesa(view);
            return false;
        });
    }

    public void clickAddCamareros(View v) {
        try {
            stop = true; //paramos contador
            final DlgSelCamareros sel_cam = new DlgSelCamareros(cx, myServicio, false, this);
            DBCamareros dbCamareros = (DBCamareros) myServicio.getDb("camareros");
            sel_cam.setNoautorizados(dbCamareros.getAutorizados(false));
            sel_cam.setAutorizados(dbCamareros.getAutorizados(true));
            sel_cam.setTitle("Elegir camareros");
            sel_cam.show();
            sel_cam.get_btn_ok().setOnClickListener(v1 -> {
                sel_cam.cancel();
               setEstadoAutoFinish(true, false);
            });
        }catch (Exception e){
            Log.e("MESAS_ERR", e.toString());
        }

    }

    public  void clickSettings(View v){
        setEstadoAutoFinish(true,true);
        final Dialog settings = new Dialog(cx);

        settings.setOnCancelListener(dialogInterface -> setEstadoAutoFinish(true,false));
        settings.setTitle("Opciones impresión...");
        settings.setContentView(R.layout.dialog_settings);
        lista_setting = settings.findViewById(R.id.lista_settings);
        ImageButton salir = settings.findViewById(R.id.btn_guardar);
        salir.setOnClickListener(v1 -> {
            if (adaptadorSettings != null){
                JSONArray lista = adaptadorSettings.lista;
                if(myServicio!=null) myServicio.setSettings(lista.toString());

            }
            settings.cancel();

        });
        if(myServicio!=null) myServicio.getSettings(handleHttp);
        settings.show();

    }

    public void clickListaTicket(View v){
        stop = true;
        dlgListadoTicket = new Dialog(this);
        dlgListadoTicket.setOnCancelListener(dialogInterface -> {
            stop = false;reset=true;
        });
        dlgListadoTicket.setContentView(R.layout.listado_ticket);
        dlgListadoTicket.setTitle("Lista de ticket");
        Window window = dlgListadoTicket.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        final ImageButton imp = dlgListadoTicket.findViewById(R.id.btnImprimir);
        final ImageButton impFactura = dlgListadoTicket.findViewById(R.id.btnImprimirFactura);
        final ImageButton salir = dlgListadoTicket.findViewById(R.id.btn_salir_monedas);
        final ImageButton ls =  dlgListadoTicket.findViewById(R.id.btnListado);
        imp.setVisibility(View.GONE);
        ls.setVisibility(View.GONE);
        impFactura.setVisibility(View.GONE);

        salir.setOnClickListener(view -> dlgListadoTicket.cancel());

        ls.setOnClickListener(view -> {
            imp.setVisibility(View.GONE);
            ls.setVisibility(View.GONE);
            impFactura.setVisibility(View.GONE);
            mostrarListadoTicket(lsTicket);
        });
        imp.setOnClickListener(view -> {
            if(myServicio!=null) myServicio.imprimirTicket(IDTicket);
            dlgListadoTicket.cancel();
        });
        impFactura.setOnClickListener(view -> {
            if(myServicio!=null) myServicio.imprimirFactura(IDTicket);
            dlgListadoTicket.cancel();
        });
        if(myServicio!=null) myServicio.getListaTickets(handleHttp);
        dlgListadoTicket.show();
    }

    public  void clickVerTicket(View v){
        IDTicket =  v.getTag().toString();
        dlgListadoTicket.findViewById(R.id.btnImprimir).setVisibility(View.VISIBLE);
        dlgListadoTicket.findViewById(R.id.btnListado).setVisibility(View.VISIBLE);
        dlgListadoTicket.findViewById(R.id.btnImprimirFactura).setVisibility(View.VISIBLE);
        if(myServicio!=null){
            myServicio.getLineasTicket(handleHttp, IDTicket);
        }
    }

    public void clickSendMensajes(View v){
        try {
            setEstadoAutoFinish(true, true);
            DlgMensajes dlg = new DlgMensajes(cx, this);
            dlg.setOnCancelListener(param -> {
                setEstadoAutoFinish(true, false);
            });
            dlg.show();
            DBCamareros db = new DBCamareros(cx);
            List<JSONObject> lista = db.getAutorizados(true);
            JSONObject o = new JSONObject();
            o.put("ID", -1);
            o.put("Nombre", "PARA TODOS");
            lista.add(o);
            dlg.mostrarReceptores(lista);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void mostrarListadoTicket(JSONArray ls) {
      try{
          if(ls.length()>0){
            LinearLayout ll =  dlgListadoTicket.findViewById(R.id.pneListados);
            ll.removeAllViews();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(5,5,5,5);

            for (int i = 0; i < ls.length(); i++) {
                JSONObject  z =  ls.getJSONObject(i);

                LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.item_cabecera_ticket, null);

                TextView  n = v.findViewById(R.id.lblNumTicket);
                TextView  f = v.findViewById(R.id.lblHoraFecha);
                TextView  m = v.findViewById(R.id.lblNombre);
                TextView  e = v.findViewById(R.id.lblEntrega);
                TextView  t = v.findViewById(R.id.lblTotal);

                m.setText(z.getString("Mesa"));
                f.setText(z.getString("Fecha")+" - "+z.getString("Hora"));
                e.setText(String.format("%01.2f €",z.getDouble("Entrega")));
                t.setText(String.format("%01.2f €", z.getDouble( "Total")));
                n.setText(z.getString("ID"));

                v.findViewById(R.id.btnVerTicket).setTag(z.getString("ID"));
                ll.addView(v, params);

            }
        }
      }catch (Exception e){
        e.printStackTrace();
      }

    }

    @SuppressLint("DefaultLocale")
    private  void mostrarlineasTicket(String res){
        try{
            JSONObject ticket = new JSONObject(res);
            JSONArray ls = ticket.getJSONArray("lineas");
            Double Total = ticket.getDouble("total");


            if(ls.length()>0){
                LinearLayout ll = dlgListadoTicket.findViewById(R.id.pneListados);
                ll.removeAllViews();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(5,5,5,5);
                for (int i = 0; i < ls.length(); i++) {
                    JSONObject  z =  ls.getJSONObject(i);

                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);
                    @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.item_linea_ticket, null);

                    TextView  c = v.findViewById(R.id.lblCan);
                    TextView  p = v.findViewById(R.id.lblPrecio);
                    TextView  n = v.findViewById(R.id.lblNombre);
                    TextView  t = v.findViewById(R.id.lblTotal);

                    c.setText(z.getString("Can"));
                    p.setText(String.format("%01.2f €",z.getDouble("Precio")));
                    n.setText(z.getString("Nombre"));
                    t.setText(String.format("%01.2f €",z.getDouble("Total")));

                    ll.addView(v, params);

                }

                LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.item_linea_total, null);
                TextView  t = v.findViewById(R.id.lblTotalTicket);
                t.setText(String.format("%01.2f €",Total));

                ll.addView(v, params);

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void clickCobrarMesa(View v){
        JSONObject m = (JSONObject)v.getTag();
        Intent intent = new Intent(cx, Cuenta.class);
        intent.putExtra("op", "c");
        intent.putExtra("cam", cam.toString());
        intent.putExtra("mesa", m.toString());
        startActivity(intent);
    }

    public void clickAbrirCaja(View v) {
        setEstadoAutoFinish(true, false);

        if (myServicio != null && myServicio.usaCashlogy()) {
            // Si usa Cashlogy, mostrar la actividad CambioCashlogyActivity
            Intent intent = new Intent(this, CambioCashlogyActivity.class);
            startActivity(intent);
        } else {
            // Si no usa Cashlogy, proceder con la lógica actual
            assert myServicio != null;
            DBCamareros dbCamareros = (DBCamareros) myServicio.getDb("camareros");
            if (!dbCamareros.getConPermiso("abrir_cajon").isEmpty()) {
                try {
                    JSONObject p = new JSONObject();
                    p.put("idc", cam.getString("ID"));
                    DlgPedirAutorizacion dlg = new DlgPedirAutorizacion(cx, this,
                            dbCamareros, this,
                            p, "abrir_cajon");
                    dlg.show();
                } catch (JSONException e) {
                    Log.e("MESAS_ERR", e.toString());
                }
            } else {
                if (myServicio != null) {
                    myServicio.abrirCajon();
                }
            }
        }
    }


    public void clickCambiarMesa(View v){
        Intent intent = new Intent(cx, OpMesas.class);
        intent.putExtra("mesa", v.getTag().toString());
        intent.putExtra("op", "cambiar");
        startActivity(intent);
    }

    public void clickJuntarMesa(View v){
        Intent intent = new Intent(cx, OpMesas.class);
        intent.putExtra("mesa", v.getTag().toString());
        intent.putExtra("op", "juntar");
        startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    public void clickBorrarMesa(View v){
        setEstadoAutoFinish(true,true);

        final JSONObject m = (JSONObject)v.getTag();
        final Dialog dlg = new Dialog(cx);

        dlg.setOnCancelListener(dialogInterface -> setEstadoAutoFinish(true,false));

        dlg.setContentView(R.layout.borrar_art);
        dlg.setTitle("Borrar Mesa ");
        final EditText motivo = dlg.findViewById(R.id.txtMotivo);
        final Button error = dlg.findViewById(R.id.btnError);
        final Button simpa =  dlg.findViewById(R.id.btnSimpa);
        final Button inv = dlg.findViewById(R.id.btnInv);
        final ImageButton ok =  dlg.findViewById(R.id.btn_ok);
        final ImageButton edit =  dlg.findViewById(R.id.btnEdit);
        final ImageButton exit = dlg.findViewById(R.id.btn_salir_monedas);
        final LinearLayout pneEdit =  dlg.findViewById(R.id.pneEditarMotivo);
        final TextView txtInfo = dlg.findViewById(R.id.txt_info_borrar);
        txtInfo.setText("Borrado de mesa completa");
        pneEdit.setVisibility(View.GONE);


            exit.setOnClickListener(view -> dlg.cancel());

            edit.setOnClickListener(view -> {
                if(pneEdit.getVisibility() == View.VISIBLE)  pneEdit.setVisibility(View.GONE);
                else pneEdit.setVisibility(View.VISIBLE);
            });

            ok.setOnClickListener(view -> {
                if (motivo.getText().length() > 0) {
                    try {
                        dlg.cancel();
                        final String idm = m.getString("ID");
                        borrarMesa(idm, motivo.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            error.setOnClickListener(view -> {
                try{
                    dlg.cancel();
                    borrarMesa(m.getString("ID"), error.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
             });

            simpa.setOnClickListener(view -> {
                try {
                    dlg.cancel();
                    borrarMesa(m.getString("ID"), simpa.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            inv.setOnClickListener(view -> {

                try {
                    dlg.cancel();
                    borrarMesa(m.getString("ID"), inv.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

         dlg.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesas);
        try {
            cam = new JSONObject(getIntent().getStringExtra("cam"));
            TextView title = findViewById(R.id.lblTitulo);
            title.setText(cam.getString("nombre")+" "+cam.getString("apellidos"));
        } catch (JSONException e) {
            Log.e("MESAS_ERR", e.toString());
        }


        autoSalir.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!stop){
                    if(!reset){
                        finish();
                    }
                    else reset = false;
                }
            }
        }, 5000, periodFinish);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConexion);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        stop = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        stop = false;
        Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        if (dbZonas != null) {
            rellenarZonas();
        }

        super.onResume();
    }

    @Override
    public void setEstadoAutoFinish(boolean reset, boolean stop) {
        this.reset = reset;
        this.stop = stop;
    }

    @Override
    public void pedirAutorizacion(ContentValues p) {
        myServicio.pedirAutorizacion(p);
    }

    @Override
    public void pedirAutorizacion(String id) {

    }

    @Override
    public void sendMensaje(String IDReceptor, String men){
        setEstadoAutoFinish(true, false);
        try {
            ContentValues p = new ContentValues();
            p.put("idreceptor", IDReceptor);
            p.put("accion", "informacion");
            p.put("mensaje", men);
            p.put("autor", cam.getString("Nombre"));
            myServicio.sendMensaje(p);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Utilidades
    private void borrarMesa(String idm, String motivo){
        try {

            if (myServicio != null){
                DBCamareros dbCamareros = (DBCamareros) myServicio.getDb("camareros");
                if(dbCamareros.getConPermiso("borrar_mesa").size() > 0) {
                    JSONObject p = new JSONObject();
                    p.put("motivo", motivo);
                    p.put("idm", idm);
                    p.put("idc", cam.getString("ID"));
                    DlgPedirAutorizacion dlg = new DlgPedirAutorizacion(cx, this,
                            dbCamareros, this,
                            p, "borrar_mesa");
                    dlg.show();
                }else{
                    ContentValues p = new ContentValues();
                    p.put("motivo", motivo);
                    p.put("idm", idm);
                    p.put("idc", cam.getString("ID"));
                    dbCuenta.eliminar(idm);
                    dbMesas.cerrarMesa(idm);
                    myServicio.rmMesa(p);
                    rellenarMesas();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}

