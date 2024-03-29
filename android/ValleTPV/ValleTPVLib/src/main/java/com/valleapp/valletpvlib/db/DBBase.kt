package com.valleapp.valletpvlib.db

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import org.json.JSONObject


interface IBaseEntity {

    fun executeAccion(json: JSONObject, dao: IBaseDao<out BaseEntity>, op: String)


}

@Entity
open class BaseEntity : IBaseEntity {
    @PrimaryKey(autoGenerate = true)
    open var id: Long = 0
    override fun executeAccion(json: JSONObject, dao: IBaseDao<out BaseEntity>, op: String) {
        TODO("Not yet implemented")
    }

}

interface IBaseDao<T : BaseEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: T)

    fun deleteById(id: Long)

    fun getAll(): List<T>

}


@Database(
    entities = [Camarero::class, Mesa::class, Zona::class, Tecla::class, Seccion::class, LineaPedido::class],
    version = 2, exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun camareroDao(): CamareroDao
    abstract fun mesasDao(): MesasDao
    abstract fun zonasDao(): ZonasDao
    abstract fun teclasDao(): TeclasDao
    abstract fun seccionesDao(): SeccionesDao

    abstract fun lineasDao(): LineasDao


    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "valletpv_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance

            }
        }
    }

}