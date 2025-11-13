package com.valleapp.vallecom.utilidades

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

import com.tu.paquete.CustomToast
import com.valleapp.valletpvlib.db.DBCamareros
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


// Add 'open' to allow inheritance, common for base classes
open class ActivityBase : AppCompatActivity() {
        protected var server: String? = null // Replace with your actual server URL
        protected var cam: JSONObject? = null // Replace with your actual JSONObject

        // 'cx' field is redundant as 'this' is already the Context in an Activity.
        // You can remove it and use 'this' directly where 'cx' was used.
        // Kept here for direct translation:
        protected val cx: Context = this

        protected var myServicio: ServiceCOM? = null
        protected var customToast: CustomToast = CustomToast(this)

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

        }

        override fun onResume() {
                super.onResume() // It's generally recommended to call super first in onResume

                // Use Kotlin's null-safety features (?.let) for cleaner checks
                // This block executes only if both myServicio and cam are not null.
                myServicio?.let { service ->
                        val cam = service.getCam() // Assuming cam is a JSONObject or similar
                        cam?.let { currentCam -> // currentCam is the non-null cam
                                try {
                                        // Use safe cast 'as?' which returns null if cast fails, instead of crashing
                                        val aux = service.getDb("camareros") as? DBCamareros

                                        if (aux != null) {
                                                // Use string templates for easier string formatting
                                                val filterQuery = "ID=${currentCam.getString("ID")} AND autorizado = '1'"
                                                val ls: JSONArray = aux.filter(filterQuery) // Assuming filter returns non-null JSONArray

                                                if (ls.length() == 0) {
                                                        // User might not be authorized anymore, finish the activity
                                                        finish()
                                                }else{
                                                        Log.d("ActivityBase", "User is authorized")
                                                }
                                        } else {
                                                // Handle the case where getDb didn't return a DBCamareros instance
                                                Log.e("ActivityBase", "Failed to get DBCamareros instance from service")
                                        }

                                } catch (e: JSONException) {
                                        // Catch specific exceptions if possible
                                        e.printStackTrace()
                                        // Consider finishing the activity or showing an error message
                                        // finish()
                                } catch (e: Exception) {
                                        // Catch broader exceptions
                                        e.printStackTrace()
                                        // finish()
                                }
                        }
                }
        }

}