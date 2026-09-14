package com.ddam_a1.gestornotas.di

import android.content.Context
import com.ddam_a1.gestornotas.data.GestorDatabase
import com.ddam_a1.gestornotas.data.NotaDao
import com.ddam_a1.gestornotas.data.NotaRepositorio
import com.ddam_a1.gestornotas.data.NotaRepositorioLocal
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// provides para Hilt :dDD

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun proveerBaseDatos(@ApplicationContext context: Context): GestorDatabase =
        GestorDatabase.getDatabase(context)

    @Provides
    fun proveerDao(db: GestorDatabase): NotaDao = db.notaDao()

    @Provides
    @Singleton
    fun proveerRepositorio(dao: NotaDao): NotaRepositorio = NotaRepositorioLocal(dao)
}