package com.valleapp.vallecom.activitys // Ensure this package matches your project structure

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.db.DBTeclas
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.concurrent.thread
import com.valleapp.valletpvlib.R as LibR

class BuscadorTeclas : ActivityBase(), TextWatcher {

    // Companion object for constants like TAG and Intent keys
    companion object {
        private const val TAG = "BuscadorTeclas"
        private const val EXTRA_TARIFA = "Tarifa"
        private const val EXTRA_ART = "art"
    }

    // Properties corresponding to Java fields
    private lateinit var tarifa: String // Initialized in onCreate
    private var lsart: JSONArray = JSONArray() // List of articles/keys found

    // Lazy initialization for DB helpers as they require context
    private val dbTeclas: DBTeclas by lazy { DBTeclas(this) }

    // Handler to update UI from background thread
    private val handlerBusqueda = Handler(Looper.getMainLooper()) {
        // Runs on UI thread
        rellenaBotonera()
        true // Indicate message was handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscador)

        // Safely get the Tarifa from intent extras, defaulting to "1" if not found
        tarifa = intent.getStringExtra(EXTRA_TARIFA) ?: run {
            Log.w(TAG, "$EXTRA_TARIFA extra missing or null, defaulting to '1'")
            "1" // Default value
        }

        // Find the TextView and add the listener
        findViewById<TextView>(R.id.txtBuscador)?.addTextChangedListener(this)
                ?: Log.e(TAG, "TextView with ID txtBuscador not found!")
    }


    /**
     * Populates the TableLayout with buttons based on the current content of `lsart`.
     */
    @SuppressLint("InflateParams") // Keep if required by layout structure
    private fun rellenaBotonera() {
        val tableLayout = findViewById<TableLayout>(R.id.pneBuscador)
        if (tableLayout == null) {
            Log.e(TAG, "TableLayout pneBuscador not found!")
            return
        }

        tableLayout.removeAllViews() // Clear previous buttons

        if (lsart.length() == 0) {
            Log.d(TAG, "lsart is empty, no buttons to display.")
            return // Nothing to display
        }

        try {
            // --- Layout Setup ---
            val tableLayoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
            )

            val metrics: DisplayMetrics = resources.displayMetrics
            val buttonHeightPx = (metrics.density * 120).toInt() // Calculate height in pixels

            val rowParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, buttonHeightPx
            ).apply {
                // Use apply scope function for cleaner setup
                setMargins(9, 3, 9, 3) // Use pixel values or convert dp if needed
            }

            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var currentRow = TableRow(this)
            tableLayout.addView(currentRow, tableLayoutParams)
            // --- End Layout Setup ---


            // --- Button Creation Loop ---
            for (i in 0 until lsart.length()) {
                val itemJson = lsart.optJSONObject(i) // Use optJSONObject for safety
                if (itemJson == null) {
                    Log.w(TAG, "Item at index $i in lsart is null or not a JSONObject")
                    continue // Skip this item
                }

                // Inflate button layout view
                // Consider inflating with parent `currentRow` and attachToRoot=false for better practice
                // val view = inflater.inflate(R.layout.btn_art, currentRow, false)
                val view = inflater.inflate(LibR.layout.btn_art, null) // Stick to original for now
                val button = view.findViewById<Button>(LibR.id.boton_art)

                if (button == null) {
                    Log.e(TAG, "Button with ID boton_art not found in inflated layout btn_art!")
                    continue // Skip if button isn't found in layout
                }

                // --- Configure Button ---
                button.id = i // Set ID if needed elsewhere
                button.tag = itemJson // Store the JSONObject in the tag
                button.isSingleLine = false // Allow multiple lines
                button.text = itemJson.optString("descripcion_r").trim().replace(" ", "\n")  // Use optString for safety


                itemJson.getInt("hay_existencias").let { hayExistencias ->
                    button.isEnabled = hayExistencias == 1
                    if (hayExistencias == 1) {
                        view.findViewById<ImageView>(LibR.id.ic_no_hay_existencias).visibility = View.GONE
                    }else{
                        view.findViewById<ImageView>(LibR.id.ic_no_hay_existencias).visibility = View.VISIBLE
                    }
                }

                // --- Set Background and Click Listener ---
                if (itemJson.has("RGB")) {
                    // Main article/key with specific color
                    val rgbString = itemJson.optString("RGB")
                    val rgbParts = rgbString.split(",")
                    if (rgbParts.size == 3) {
                        // Safely parse RGB values
                        val r = rgbParts[0].trim().toIntOrNull()
                        val g = rgbParts[1].trim().toIntOrNull()
                        val b = rgbParts[2].trim().toIntOrNull()

                        if (r != null && g != null && b != null) {
                            try {
                                button.setBackgroundColor(Color.rgb(r, g, b))
                            } catch (iae: IllegalArgumentException) {
                                Log.e(TAG, "Invalid RGB values ($r, $g, $b) for item: ${itemJson.optString("Nombre")}", iae)
                                button.setBackgroundResource(LibR.drawable.bg_pink) // Fallback color
                            }
                        } else {
                            Log.w(TAG, "Could not parse RGB values from '$rgbString' for item: ${itemJson.optString("Nombre")}")
                            button.setBackgroundResource(LibR.drawable.bg_pink) // Fallback color
                        }
                    } else {
                        Log.w(TAG, "Invalid RGB string format '$rgbString' for item: ${itemJson.optString("Nombre")}")
                        button.setBackgroundResource(LibR.drawable.bg_pink) // Fallback color
                    }

                    // Click listener for main articles/keys
                    button.setOnClickListener { clickedView ->
                        handleMainKeyClick(clickedView.tag as? JSONObject)
                    }

                }

                currentRow.addView(view, rowParams) // Add button view to the current row

                // Start a new row every 3 items
                if ((i + 1) % 3 == 0 && i < lsart.length() - 1) {
                    currentRow = TableRow(this)
                    tableLayout.addView(currentRow, tableLayoutParams)
                }
            }
            // --- End Button Creation Loop ---

        } catch (e: Exception) {
            Log.e(TAG, "Error occurred while populating button grid", e)
            // Optionally clear the layout again or show an error message
            tableLayout.removeAllViews()
        }
    }

    /**
     * Handles clicks on buttons representing main articles/keys (those with RGB).
     */
    private fun handleMainKeyClick(itemJson: JSONObject?) {
        if (itemJson == null) {
            Log.e(TAG, "Clicked main key button's tag was null or not a JSONObject.")
            return
        }

        try {
            // Create a mutable copy to modify and store as the selection
            val mutableCopy = JSONObject(itemJson.toString())
            // Store the copy as the current main selection

            if (mutableCopy.optString("tipo") == "SP") {
                // If it's a direct product (SP), finish and return it
                mutableCopy.put("Descripcion", mutableCopy.optString("descripcion_r"))
                returnResult(mutableCopy)
            } else {
                // If it has sub-keys, load them and refresh the button grid
                lsart = dbTeclas.getAllSub(mutableCopy.optString("ID"))
                rellenaBotonera()
            }
        } catch (e: JSONException) {
            Log.e(TAG, "JSONException handling main key click for item: ${itemJson.optString("Nombre")}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception handling main key click for item: ${itemJson.optString("Nombre")}", e)
        }
    }



    /**
     * Finishes the activity and returns the selected/modified article JSONObject as a string.
     */
    private fun returnResult(resultJson: JSONObject) {
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_ART, resultJson.toString())
        setResult(RESULT_OK, resultIntent)
        finish()
    }


    // --- TextWatcher Implementation ---

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // No action needed
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Debounce search: Wait a bit after text changes before searching
        // Remove any pending search messages
        handlerBusqueda.removeCallbacksAndMessages(null)
        if (!s.isNullOrBlank()) {
            val searchText = s.toString()
            // Schedule search after a delay (e.g., 500ms)
            // Post a delayed message instead of starting a new thread immediately
            // Allows cancelling if user types quickly
            val msg = handlerBusqueda.obtainMessage(SEARCH_MESSAGE, searchText)
            handlerBusqueda.sendMessageDelayed(msg, SEARCH_DELAY_MS)

            thread { // Import kotlin.concurrent.thread
                 try {
                     // Check if text hasn't changed again during sleep? Not easily done here.
                     val currentText = findViewById<TextView>(R.id.txtBuscador)?.text?.toString()
                     if (searchText == currentText) { // Basic check if text is still the same
                         lsart = dbTeclas.findLike(searchText, tarifa)
                         handlerBusqueda.sendEmptyMessage(UPDATE_UI_MESSAGE) // Use specific message code
                     }
                 } catch (e: InterruptedException) {
                     Log.w(TAG, "Search thread interrupted", e)
                     Thread.currentThread().interrupt() // Re-interrupt thread
                 } catch (e: Exception) {
                     Log.e(TAG, "Error in background search thread", e)
                 }finally {
                     Thread.sleep(1000) // Delay search - consider debouncing with Handler instead
                 }
            }

        } else {
            // Text is empty, clear results
            lsart = JSONArray() // Clear the list
            handlerBusqueda.sendEmptyMessage(UPDATE_UI_MESSAGE) // Update UI immediately
        }
    }

    override fun afterTextChanged(s: Editable?) {
        // No action needed
    }


    // --- Debounced Search Logic using Handler (Recommended) ---
    // Define message code and delay for debouncing
    private val SEARCH_MESSAGE = 1
    private val UPDATE_UI_MESSAGE = 0 // Use 0 for simple UI update from handler itself
    private val SEARCH_DELAY_MS = 500L // 500 milliseconds delay

    // Override handleMessage for debounced search
    private val searchHandler = Handler(Looper.getMainLooper()) { msg ->
            when (msg.what) {
        SEARCH_MESSAGE -> {
            val searchText = msg.obj as? String
            if (searchText != null) {
                // Run actual DB search in background
                thread {
                    try {
                        lsart = dbTeclas.findLike(searchText, tarifa)
                        // Post result back to UI thread using the *other* handler
                        handlerBusqueda.sendEmptyMessage(UPDATE_UI_MESSAGE)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during DB search for '$searchText'", e)
                        // Optionally clear results or show error on UI thread
                        lsart = JSONArray()
                        handlerBusqueda.sendEmptyMessage(UPDATE_UI_MESSAGE)
                    }
                }
            }
            true // Handled
        }
        UPDATE_UI_MESSAGE -> {
            // This is handled by the main `handlerBusqueda` declared earlier
            false // Let the other handler process it
        }
            else -> false // Not handled
    }
    }

    // Remember to remove callbacks in onDestroy to prevent leaks if using the searchHandler approach
    override fun onDestroy() {
        super.onDestroy()
        searchHandler.removeCallbacksAndMessages(null)
        handlerBusqueda.removeCallbacksAndMessages(null) // Also clear the UI handler
    }

}