package com.ddam_a1.gestornotas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ddam_a1.gestornotas.modelClasses.Imagen
import kotlinx.coroutines.flow.Flow

/**
 * El DAO de las imagenes. Igualito al de notas, nada mas corto.
 *
 * Fijate que NO hay un "dame la nota con sus fotos": las dos tablas se consultan
 * por separado y la pantalla junta las piezas. Es lo mas simple que funciona, y
 * de paso hace que la lista de fotos sea un `Flow` propio: al agregar una, la
 * pantalla se entera sola, sin recargar la nota.
 */
@Dao
interface ImagenDao {

    /**
     * Las fotos de una nota, en vivo.
     *
     * `Flow` y no `suspend`: esta es la consulta que mira la pantalla. Cuando se
     * inserte una imagen nueva, Room vuelve a correr este SELECT y lo emite. Por
     * eso agregar una foto no necesita avisarle a nadie.
     */
    @Query("SELECT * FROM imagenes WHERE nota_id = :notaID ORDER BY agregada ASC")
    fun imagenesDe(notaID: String): Flow<List<Imagen>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun agregar(imagen: Imagen)

    @Delete
    suspend fun borrar(imagen: Imagen)
}
