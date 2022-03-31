package com.valleapp.valletpv.Util;

import android.content.ContentValues;
import android.os.Handler;

public class Instrucciones {

    public ContentValues getParams() {
        return params;
    }

    public String getUrl() {
        return url;
    }

    public Handler getHandler() {
        return handler;
    }

    public String getOp() {
        return op;
    }

    String url;
    Handler handler;
    ContentValues params;
    String op = "";

    public Instrucciones(ContentValues params, String url, Handler handler, String op){
        this.params = params; this.url = url; this.handler = handler; this.op = op;
    }
    public Instrucciones(ContentValues params, String url){
        this.params = params; this.url = url; this.handler = null;
    }
}
