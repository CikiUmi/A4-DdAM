package com.ddam_a1.gestornotas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ddam_a1.gestornotas.modelClasses.Nota

// clase abstracta porque no se puede iniciar, debe sub-clasearse (herencia)

// esto significa que guardamos notas y es la versión 1 de la DB
// la clase extiende de room, justo es abstracta porque room implementa todo
@Database(entities = [Nota::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GestorDatabase : RoomDatabase() {

    abstract fun notaDao(): NotaDao //con esto ya no creamos la db, migraciones, etc.

    // usa Singleton para evitar múltiples instancias con Room db instancia
    companion object {

        // para no sobreescribir o tener más de una db (por suspend, hilos y corrutinas)
        // VOLATILE siempre va en memoria principal, nunca en caché
        @Volatile
        private var INSTANCE: GestorDatabase? = null

        fun getDatabase(
            context: Context,
        ): GestorDatabase {
            // Elvis mentioned chicos: devuelve instancia y si no:
            return  INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder (
                    context.applicationContext,
                    GestorDatabase::class.java,
                    "gestor_notas_database"
                )
                    //.setQueryExector() <- se puede definir acá
                    .fallbackToDestructiveMigration()
                    //mientras se hacen pruebas y correcciones
                    .build()
                INSTANCE = instance
                // regresar la instancia:
                instance
            }
        }
    }
}