package com.valleapp.vallecom.utilidades

import android.content.Context
import com.valleapp.vallecom.interfaces.INota
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONArray
import org.json.JSONObject

class Nota(mesa: JSONObject, private val cx: Context, private val controlador: INota) {

    private var comanda: MutableList<JSONObject> = mutableListOf()
    private var num = 0
    private var nombre: String = ""
    private var artSel: JSONObject? = null

    init {
        try {
            this.nombre = mesa.getString("Nombre")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cargarComanda()
    }

    fun getNum(): Int {
        return num
    }

    fun getArt(art: JSONObject): String {
        artSel = art
        return artSel.toString()
    }

    fun getLineas(): List<JSONObject> {
        return comanda
    }

    fun rmArt(art: JSONObject) {
        try {
            num--
            comanda.remove(art)
            guardarComanda()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addArt(art: JSONObject, can: Int) {
        try {
            num += can
            repeat(can) {
                val newArt = JSONObject(art.toString())
                newArt.put("Can", 1)
                comanda.add(newArt)
            }
            guardarComanda()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cargarComanda() {
        val json = JSON()
        try {
            val cm = json.deserializar("$nombre.dat", cx)
            comanda = mutableListOf()
            if (cm == null) {
                num = 0
            } else {
                val l = JSONArray(cm.get("lineas").toString())
                num = cm.getInt("num")
                for (i in 0 until l.length()) {
                    val art = l.getJSONObject(i)
                    comanda.add(art)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun guardarComanda() {
        val json = JSON()
        try {
            val cm = JSONObject()
            cm.put("num", num)
            cm.put("lineas", getLineas().toString())
            json.serializar("$nombre.dat", cm, cx)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        controlador.rellenarComanda()
    }

    fun eliminarComanda() {
        comanda = mutableListOf()
        num = 0
        cx.deleteFile("$nombre.dat")
        controlador.rellenarComanda()
    }

    fun addSug(sug: String, incremento: Double = 0.0) {
        try {
            val nombre = artSel?.getString("Descripcion") + " " + sug
            val precio = artSel?.getDouble("Precio") ?: 0.0
            artSel?.put("Precio", precio + incremento)
            artSel?.put("Descripcion", nombre)
            guardarComanda()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
