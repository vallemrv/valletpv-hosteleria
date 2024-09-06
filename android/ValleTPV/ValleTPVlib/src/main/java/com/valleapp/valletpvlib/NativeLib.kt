package com.valleapp.valletpvlib

class NativeLib {

    /**
     * A native method that is implemented by the 'valletpvlib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'valletpvlib' library on application startup.
        init {
            System.loadLibrary("valletpvlib")
        }
    }
}