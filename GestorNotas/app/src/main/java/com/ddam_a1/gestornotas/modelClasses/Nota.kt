package com.ddam_a1.gestornotas.modelClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

/**
 * @Entity
 *     data class Nota(@PrimaryKey @ColumnInfo(name = "string") val string: String)
 *     */

/**
 * Dias que una nota vive en la papelera antes de borrarse solo.
 *
 * Vive AQUI, junto al modelo, porque lo usan dos lugares muy separados:
 *   - `limpiarPapelera()` en el ViewModel, para decidir a quien tirar
 *   - la tarjeta, para escribir "se elimina el 12/09/2026"
 *
 * Antes estaba escrito a mano en los dos. Si algun dia cambiabas uno y olvidabas
 * el otro, la tarjeta prometeria una fecha y el ViewModel borraria en otra. Ese
 * tipo de bug no truena: solo miente.
 */
const val DIAS_EN_PAPELERA = 5L

// Definir los enums

@Entity
data class Nota(
    @PrimaryKey @ColumnInfo (name = "_id") val id: String = UUID.randomUUID().toString(),
    //val id: String = UUID.randomUUID().toString(),
    @ColumnInfo (name = "titulo")val titulo : String = "",
    @ColumnInfo (name = "contenido")val contenido : String = "",
    @ColumnInfo (name = "en_papelera") val enPapelera: Boolean = false,
    @ColumnInfo (name = "fecha_nota") val fechaNota: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo (name = "fecha_eliminado") val fechaEliminado: LocalDateTime? = null,
)

// El ?: (Elvis Operator /gen) significa que revisa si hay un valor, si no, asigna uno.
// Sirve para asignar cosas. Ej. val nombre ?: = "user anónimo".


// Parece basura pero el val es por una razón ok?
// Se supone que si actualizas un objeto nada más, los datos cambian
// Pero creo que compose como que no le sabe y no actualiza la UI
// Entonces va de borrar y crear objetos
// Son ligeros, no pasa mucho creo :D