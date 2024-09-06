package com.valleapp.valletpv.interfaces;

import android.content.ContentValues;

public interface IControladorAutorizaciones {
    public void pedirAutorizacion(ContentValues params);
    public void pedirAutorizacion(String id);
}
