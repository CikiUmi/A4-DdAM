package com.ddam_a1.gestornotas.modelClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

// ============================================================
//  UNA IMAGEN DE UNA NOTA
//
//  UNA NOTA -> MUCHAS IMAGENES
//
//  En SQL eso NO se guarda como una lista dentro de la nota: una columna guarda
//  un valor, no una coleccion. Se guarda al reves, y es lo unico raro de todo
//  esto: cada imagen apunta a SU nota con `nota_id`. La nota no sabe cuantas
//  fotos tiene; se le pregunta a la tabla de imagenes "dame las que apunten a
//  este id". Eso es una relacion uno-a-muchos.
//
//  LA LLAVE FORANEA (foreignKeys)
//
//  Le dice a SQLite: "`nota_id` tiene que ser el `_id` de una nota que EXISTA".
//  Gracias a eso no se pueden guardar fotos de una nota fantasma.
//
//  Y `onDelete = CASCADE` es el regalo: cuando una nota se borra de verdad (la
//  papelera al cumplir sus 5 dias), sus renglones de imagen se borran SOLOS.
//  Sin esto habria que acordarse de limpiarlos a mano en cada lugar que borra
//  una nota, y el dia que se te olvide uno, la base se llena de basura invisible.
//
//  EL INDICE (indices)
//
//  La consulta de siempre va a ser "todas las imagenes con este nota_id". Sin
//  indice, SQLite lee la tabla entera cada vez. Con indice, va directo. Room
//  ademas te MARCA UN WARNING si declaras una llave foranea sin indexar.
// ============================================================

@Entity(
    tableName = "imagenes",
    foreignKeys = [
        ForeignKey(
            entity = Nota::class,
            parentColumns = ["_id"],      // la columna en `notas`
            childColumns = ["nota_id"],   // la columna de aqui que apunta alla
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["nota_id"])]
)
data class Imagen(
    @PrimaryKey @ColumnInfo(name = "_id")
    val id: String = UUID.randomUUID().toString(),

    /** A cual nota pertenece. Es el `_id` de la nota, no un objeto Nota. */
    @ColumnInfo(name = "nota_id")
    val notaId: String,

    /**
     * Donde esta el archivo, ya COPIADO dentro de la app.
     *
     * No se guarda la direccion de la galeria a proposito. Si guardaras el uri
     * de la foto original, el dia que la borres del celular tu nota se quedaria
     * con un hueco gris. Un diario no puede depender de que no limpies tu
     * galeria: al elegirla, la app se queda con su propia copia.
     */
    @ColumnInfo(name = "ruta")
    val ruta: String,

    /** Para poder ordenarlas siempre igual. Sin ORDER BY, SQLite no promete nada. */
    @ColumnInfo(name = "agregada")
    val agregada: LocalDateTime = LocalDateTime.now()
)
