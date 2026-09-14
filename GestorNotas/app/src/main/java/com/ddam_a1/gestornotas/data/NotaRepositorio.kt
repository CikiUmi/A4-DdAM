package com.ddam_a1.gestornotas.data

import com.ddam_a1.gestornotas.modelClasses.Nota
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

//archivo que asigna la implementación del DAO
// es como el DAO pero no hay tantos imports, es una capa de abstracción

interface NotaRepositorio {

    // leer las de bandeja y regresar lista
    fun leerBandejaStream() : Flow<List<Nota>>

    // leer las de papelera y regresar lista
    fun leerPapeleraStream() : Flow<List<Nota>>

    // lee una nota específica con el ID
    suspend fun leerNota(notaID: String): Nota?

    // añadir una nota
    suspend fun agregar(nota: Nota)

    // actualizar
    suspend fun actualizar(nota: Nota)

    // echar a papelera
    suspend fun meterPapelera(notaID: String, cuando: LocalDateTime): Boolean

    // sacar de papelera
    suspend fun sacarPapelera(notaID: String): Boolean

    // borrar una
    suspend fun borrar(nota: Nota)

    // borrar las que ya expiraron en papelera
    suspend fun vaciarPapelera(limite: LocalDateTime): Boolean
}