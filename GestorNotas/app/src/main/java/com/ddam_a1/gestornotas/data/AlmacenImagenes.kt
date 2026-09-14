package com.ddam_a1.gestornotas.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

// ============================================================
//  DONDE VIVEN LAS FOTOS
//
//  La base de datos guarda la RUTA de una foto, no la foto. Meter imágenes
//  dentro de SQLite se puede (columna BLOB) pero es mala idea: cada consulta
//  arrastraría megas, y una `Nota` de 3 MB rompería el límite de memoria de un
//  cursor de Android. La regla de siempre: los archivos al disco, la base guarda
//  dónde quedaron.
//
//  POR QUE SE COPIA Y NO SE GUARDA EL URI DE LA GALERIA
//
//  Cuando eliges una foto, Android te presta un permiso TEMPORAL para leer ese
//  archivo. Ese préstamo se acaba, y además la foto sigue siendo de la galería:
//  si la borras o mueves, tu nota se queda con un hueco gris. Copiándola a la
//  carpeta privada de la app, la nota se queda con su propia copia y ya nadie se
//  la puede quitar. Para un diario eso no es un lujo, es el punto.
//
//  `context.filesDir` es privado de la app: nadie más lo ve, y se borra solo
//  cuando desinstalas. Tampoco necesita ningún permiso.
// ============================================================

private const val CARPETA = "imagenes"
private const val TAG = "AlmacenImg"

object AlmacenImagenes {

    /** La carpeta de las fotos, creada si no existía. */
    private fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { mkdirs() }

    /**
     * Copia la foto elegida y devuelve la ruta de la copia, o null si algo falló.
     *
     * Es `suspend` con `Dispatchers.IO` porque copiar un archivo es trabajo de
     * disco: en el hilo principal congelaría la pantalla lo que dure la copia, y
     * con una foto de varios megas eso se ve.
     *
     * Devuelve `String?` en vez de lanzar una excepción para que quien llame
     * pueda decidir: aquí, si sale null, simplemente no se adjunta nada y la
     * nota sigue como estaba.
     */
    suspend fun guardar(context: Context, origen: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val destino = File(carpeta(context), "${UUID.randomUUID()}.jpg")

            // `use` cierra los dos streams pase lo que pase, incluso si truena a
            // media copia. Sin él, un error dejaría el archivo abierto.
            context.contentResolver.openInputStream(origen)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: return@withContext null

            destino.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo copiar la imagen", e)
            null
        }
    }

    /**
     * Borra el archivo de una foto que se quitó de una nota.
     *
     * Se llama aparte de borrar el renglón de la base porque son dos mundos
     * distintos: SQLite no sabe de archivos. Si esto falla no pasa gran cosa —
     * queda un archivo suelto ocupando espacio, no un error visible.
     */
    fun borrar(ruta: String) {
        runCatching { File(ruta).delete() }
            .onFailure { Log.e(TAG, "No se pudo borrar $ruta", it) }
    }
}
