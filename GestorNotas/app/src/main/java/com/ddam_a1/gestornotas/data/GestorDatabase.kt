package com.ddam_a1.gestornotas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ddam_a1.gestornotas.modelClasses.Imagen
import com.ddam_a1.gestornotas.modelClasses.Nota

// clase abstracta porque no se puede iniciar, debe sub-clasearse (herencia)

// esto significa que guardamos notas e imágenes, y es la versión 2 de la DB
// la clase extiende de room, justo es abstracta porque room implementa todo
//
// SUBIO A 2 PORQUE CAMBIO EL ESQUEMA (entró la tabla `imagenes`).
// El número de versión es cómo Room sabe si la base del celular está al día. Si
// el celular tiene la 1 y el código pide la 2, Room busca un camino de la 1 a la
// 2. Ese camino es la MIGRACION_1_2 de más abajo.
@Database(entities = [Nota::class, Imagen::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GestorDatabase : RoomDatabase() {

    abstract fun notaDao(): NotaDao //con esto ya no creamos la db, migraciones, etc.

    abstract fun imagenDao(): ImagenDao

    // usa Singleton para evitar múltiples instancias con Room db instancia
    companion object {

        // para no sobreescribir o tener más de una db (por suspend, hilos y corrutinas)
        // VOLATILE siempre va en memoria principal, nunca en caché
        @Volatile
        private var INSTANCE: GestorDatabase? = null

        // ====================================================
        //  MIGRACION 1 -> 2 : nace la tabla `imagenes`
        //
        //  `fallbackToDestructiveMigration()` ya estaba puesto, y hace lo que
        //  dice: si no hay camino de una versión a otra, BORRA la base y la
        //  vuelve a crear vacía. Cómodo mientras no hay nada que perder, pero
        //  ahora ya tienes notas de verdad adentro.
        //
        //  Una migración es literalmente el SQL que lleva la base vieja al
        //  esquema nuevo. Aquí no hay que tocar `notas` para nada: sólo falta
        //  una tabla, así que se crea y ya. Las notas ni se enteran.
        //
        //  EL SQL TIENE QUE QUEDAR IDENTICO al que Room generaría solo. Al abrir
        //  la base, Room compara el esquema real contra el que espera, columna
        //  por columna, y si algo no cuadra truena en vez de seguir con una base
        //  a medias. Por eso los tipos van en mayúsculas y el nombre del índice
        //  sigue su fórmula: index_<tabla>_<columna>.
        //
        //  Si al correr te reclama que la migración no manejó bien el esquema,
        //  desinstala la app del emulador y vuelve a instalar: eso arranca de
        //  cero en la versión 2 y ya no pasa por aquí.
        // ====================================================
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `imagenes` (" +
                        "`_id` TEXT NOT NULL, " +
                        "`nota_id` TEXT NOT NULL, " +
                        "`ruta` TEXT NOT NULL, " +
                        "`agregada` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`_id`), " +
                        "FOREIGN KEY(`nota_id`) REFERENCES `notas`(`_id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_imagenes_nota_id` " +
                        "ON `imagenes` (`nota_id`)"
                )
            }
        }

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
                    // El camino de la 1 a la 2. VA ANTES del fallback: mientras
                    // exista una migración, Room la usa y NO tira nada.
                    .addMigrations(MIGRACION_1_2)
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