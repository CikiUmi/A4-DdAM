package com.ddam_a1.gestornotas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
    @Query("SELECT * FROM nota WHERE en_papelera = 0 ORDER BY fecha_nota ASC")
    fun leerBandeja(): Flow<List<Nota>>

    // leer las de papelera y regresar lista
    @Query("SELECT * FROM nota WHERE en_papelera = 1 ORDER BY fecha_eliminado ASC")
    fun leerPapelera(): Flow<List<Nota>>

    // lee una nota específica con el ID
    @Query("SELECT * FROM nota WHERE _id = :notaID")
    suspend fun leerNota(notaID: String): Nota?

    // añadir una
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregar(nota: Nota)

    // actualizar
    @Update
    suspend fun update(nota: Nota)

    // echar a papelera
    @Query("UPDATE nota SET fecha_eliminado = :cuando, en_papelera = 1 WHERE _id = :notaID")
    suspend fun meterPapelera(cuando: LocalDateTime, notaID: String): Int

    // sacar de papelera
    @Query("UPDATE nota SET fecha_eliminado = null, en_papelera = 0 WHERE _id = :notaID")
    suspend fun sacarPapelera(notaID: String): Int

    // borrar una
    @Delete
    suspend fun borrar(nota: Nota)

    // borrar las que ya expiraron en papelera
    @Query("DELETE FROM nota WHERE fecha_eliminado <= :limite")
    suspend fun vaciarPapelera(limite: LocalDateTime): Int
}