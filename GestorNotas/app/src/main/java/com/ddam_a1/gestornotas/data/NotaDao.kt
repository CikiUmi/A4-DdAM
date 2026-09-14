package com.ddam_a1.gestornotas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.ddam_a1.gestornotas.modelClasses.Nota
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

// consultas, como queries

// Suspend: lo hace y termina <- es seguro llamarlas desde corrutinas, room lo mueve a otro hilo
// Flow: se queda ejecutando o esperando, es constante
// NUNCA VAN JUNTOS !!! porque explota, no tiene sentido
@Dao
interface NotaDao {

    // CRUD: Métodos abstractos en el dao, los nombres son los del @ColumnInfo

    // leer notas de bandeja y regresar lista
    // 0 es FALSE en SQLite según android studio warnings...
    @Query("SELECT * FROM notas WHERE en_papelera = 0 ORDER BY fecha_nota ASC")
    fun leerBandeja(): Flow<List<Nota>>

    // leer las de papelera y regresar lista
    @Query("SELECT * FROM notas WHERE en_papelera = 1 ORDER BY fecha_eliminado ASC")
    fun leerPapelera(): Flow<List<Nota>>

    // lee una nota específica con el ID
    @Query("SELECT * FROM notas WHERE _id = :notaID")
    suspend fun leerNota(notaID: String): Nota?

    // añadir una
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregar(nota: Nota)

    /**
     * Guarda una nota exista o no: la mete si es nueva, la actualiza si ya está.
     *
     * POR QUE HACIA FALTA OTRA. Tus dos métodos cubren un caso cada uno y la
     * pantalla tiene que adivinar cuál usar:
     *   `agregar` con IGNORE, si la nota YA existe, no hace NADA en silencio.
     *   `update` en cambio no sirve si la nota todavía no existe.
     * Al adjuntar una foto hay que asegurarse de que la nota exista ANTES (la
     * llave foránea lo exige) sin perder lo que ya escribiste. `@Upsert` hace
     * justo eso: UPdate + inSERT.
     *
     * Y OJO CON REPLACE. Si `agregar` usara `OnConflictStrategy.REPLACE`, al
     * guardar sobre una nota existente SQLite haría un DELETE seguido de un
     * INSERT. Como las imágenes tienen ON DELETE CASCADE, ese DELETE invisible
     * se llevaría las fotos de la nota. `@Upsert` no borra nada. Es una trampa
     * clásica de Room y ya no te puede tocar, pero vale la pena saberla.
     */
    @Upsert
    suspend fun guardar(nota: Nota)

    // actualizar
    @Update
    suspend fun update(nota: Nota)

    // echar a papelera
    @Query("UPDATE notas SET fecha_eliminado = :cuando, en_papelera = 1 WHERE _id = :notaID")
    suspend fun meterPapelera(notaID: String, cuando: LocalDateTime): Int

    // sacar de papelera
    @Query("UPDATE notas SET fecha_eliminado = null, en_papelera = 0 WHERE _id = :notaID")
    suspend fun sacarPapelera(notaID: String): Int

    // borrar una
    @Delete
    suspend fun borrar(nota: Nota)

    // borrar las que ya expiraron en papelera
    @Query("DELETE FROM notas WHERE fecha_eliminado <= :limite")
    suspend fun vaciarPapelera(limite: LocalDateTime): Int
}