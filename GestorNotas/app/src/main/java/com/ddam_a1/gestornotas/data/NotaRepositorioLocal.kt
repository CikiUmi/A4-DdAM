package com.ddam_a1.gestornotas.data

import com.ddam_a1.gestornotas.modelClasses.Nota
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class NotaRepositorioLocal(private val notaDao: NotaDao) : NotaRepositorio {
    override fun leerBandejaStream() : Flow<List<Nota>> = notaDao.leerBandeja()

    override fun leerPapeleraStream() : Flow<List<Nota>> = notaDao.leerPapelera()

    override suspend fun leerNota(notaID: String): Nota? = notaDao.leerNota(notaID)

    override suspend fun agregar(nota: Nota) = notaDao.agregar(nota)

    override suspend fun actualizar(nota: Nota) = notaDao.update(nota)

    override suspend fun meterPapelera(notaID: String, cuando: LocalDateTime): Boolean = notaDao.meterPapelera(notaID,cuando) > 0

    override suspend fun sacarPapelera(notaID: String): Boolean = notaDao.sacarPapelera(notaID) > 0

    override suspend fun borrar(nota: Nota) = notaDao.borrar(nota)

    override suspend fun vaciarPapelera(limite: LocalDateTime): Boolean = notaDao.vaciarPapelera(limite) > 0
}