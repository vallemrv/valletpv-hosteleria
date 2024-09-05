package com.valleapp.valletpv.interfaces;

import android.content.ContentValues;

public interface IControladorAutorizaciones {
     void pedirAutorizacion(ContentValues params);
     void pedirAutorizacion(String id);
}
