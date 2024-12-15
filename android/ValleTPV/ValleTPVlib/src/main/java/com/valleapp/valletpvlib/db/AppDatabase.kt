package com.valleapp.valletpvlib.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.valleapp.valletpvlib.db.entities.Camarero
import com.valleapp.valletpvlib.db.entities.Cuenta
import com.valleapp.valletpvlib.db.entities.Mesa
import com.valleapp.valletpvlib.db.entities.Subtecla
import com.valleapp.valletpvlib.db.entities.Tecla
import com.valleapp.valletpvlib.db.entities.Seccion


import com.valleapp.valletpvlib.db.interfaces.CamareroDao
import com.valleapp.valletpvlib.db.interfaces.CuentaDao
import com.valleapp.valletpvlib.db.interfaces.MesaDao
import com.valleapp.valletpvlib.db.interfaces.SeccionDao
import com.valleapp.valletpvlib.db.interfaces.SubTeclaDao
import com.valleapp.valletpvlib.db.interfaces.TeclaDao
import com.valleapp.valletpvlib.db.interfaces.ZonaDao

@Database(entities = [Cuenta::class, Camarero::class, Mesa::class,
    Seccion::class, Subtecla::class, Tecla::class, ZonaDao::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cuentaDao(): CuentaDao
    abstract fun camareroDao(): CamareroDao
    abstract fun mesaDao(): MesaDao
    abstract fun seccionDao(): SeccionDao
    abstract fun subTeclaDao(): SubTeclaDao
    abstract fun teclaDao(): TeclaDao
    abstract fun zonaDao(): ZonaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "valletpv"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
