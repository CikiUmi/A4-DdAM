package com.ddam_a1.gestornotas.di

import android.content.Context
import com.ddam_a1.gestornotas.data.GestorDatabase
import com.ddam_a1.gestornotas.data.ImagenDao
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

    // Un dao nuevo = un @Provides nuevo. Hilt empata por TIPO DE RETORNO, así
    // que con esto ya sabe fabricar un ImagenDao cuando alguien lo pida.
    @Provides
    fun proveerImagenDao(db: GestorDatabase): ImagenDao = db.imagenDao()

    // Y como el repositorio ahora necesita los dos, se le piden los dos. Hilt
    // resuelve la cadena solo: pide GestorDatabase, de ahí saca los dos daos, y
    // con los dos arma el repositorio.
    @Provides
    @Singleton
    fun proveerRepositorio(dao: NotaDao, imagenDao: ImagenDao): NotaRepositorio =
        NotaRepositorioLocal(dao, imagenDao)
}