package com.ddam_a1.gestornotas.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.ddam_a1.gestornotas.modelClasses.DIAS_EN_PAPELERA
import com.ddam_a1.gestornotas.modelClasses.Nota
import java.time.LocalDateTime

@HiltViewModel
class NotasViewModel @Inject constructor() : ViewModel() {

    //===== ATRIBUTOS Y variables? ===== (para separar ok? Porque no le sé bien todavía)

    // Primero se define una lista para poder utilizar los métodos.
    // El viewModel la crea y no la mata si cambia la UI :D

    // Y tmb se crean las listas para mandar a las interfaces.

    //private val _notas: Flow<List<Nota>> =
    private val _notas = mutableStateListOf<Nota>()
    // por seguridad es private, para que no le estén moviendo
    // el _ es porque es buena práctica ponérselo cuando es privada

    val ListaNotas: List<Nota>
        get() = _notas
            .filter { it.enPapelera == false }
            .sortedBy { it.fechaNota }
        // El it es como un i en los loops, sirve para ir iterando

    val ListaPapelera: List<Nota>
        get() = _notas
            .filter { it.enPapelera }
            .sortedByDescending { it.fechaEliminado }


    //===== MÉTODOS ===== (para separar ok? Porque no le sé bien todavía)

    fun agregar(nota: Nota): Boolean {
        if (nota.titulo.isBlank()){ //Blank tmb capta los " "
            Log.d(TAG, "La nota no puede tener un título vacío")
            return false
        }
        if (nota.fechaNota.isBefore(LocalDateTime.now())){
            Log.d(TAG, "La nota no puede ser en el pasado")
            return false
        }

        _notas.add(nota)
        Log.d(TAG, "Nota añadida !!")
        return true
    }

    // Guarda los cambios de uno que YA existe.
    // Se busca por id y se reemplaza completo: el objeto que llega del formulario
    // ya trae el mismo id (salio de un .copy()), asi que conserva su identidad.
    fun actualizar(nota: Nota): Boolean {
        if (nota.titulo.isBlank()) return false

        val i = _notas.indexOfFirst { it.id == nota.id }
        if (i < 0) return false

        _notas[i] = nota
        return true
    }

    fun meterPapelera(id: String) : Boolean {
        // Idealmente se cambia el valor, pero son inmutables entonces:
        val i = _notas.indexOfFirst { it.id == id } // busca la primera cosa con el id y guarda el index

        // si no lo encuentra (no está después del 0) devuelve -1, entonces:
        if (i < 0){
            Log.d(TAG, "La nota no existe")
            return false
        }

        // Lo que se hace acá es meterlo, lo copia tal cual pero cambia el valor de papelera
        _notas[i] = _notas[i].copy(enPapelera = true, fechaEliminado = LocalDateTime.now())
        Log.d(TAG, "Nota enviada a la papelera")
        return true
    }

    fun sacarPapelera(id: String) : Boolean {
        val i = _notas.indexOfFirst { it.id == id }
        if (i < 0){
            Log.d(TAG, "La nota no existe")
            return false
        }
        _notas[i] = _notas[i].copy(enPapelera = false,  fechaEliminado = null)
        Log.d(TAG, "Nota enviada a la bandeja de entrada")
        return true
    }

    // Es el mismo cosito de la clase (el Elvis), puede No tener string y no devolver un Nota, es opcional}

    // Yo sé pueden hacer funciones con el mismo nombre y diferentes parámentros
    // no me acuerdo del nombre. pero pues mmmm...
    // Es un intento del .getOrPut del map pero para listas

    // Acá no devuelve booleano, da la nota
    fun leer(id: String?) : Nota? {
        // Por si es CREACIÓN de uno (para la pantalla que es la misma de crear y leer)
        if (id == null ) { return null }

        // busca y regresa nota
        return _notas.find { it.id == id }
    }

    // FUNCIÓN QUE SE LLAMA AL ENTRAR
    // es para no programar cositas y eso como notis
    fun limpiarPapelera(){
        val fechaActual = LocalDateTime.now()

        // omg removeAll, tqm tqm
        // Otra vez el Elvis para evitar errores si no está definido, etc
        _notas.removeAll { it.fechaEliminado?.plusDays(DIAS_EN_PAPELERA)?.isBefore(fechaActual) == true }
    }
}

// El TAG es la etiqueta con la que filtras en Logcat.
// `print()` NO sirve en Android: no escribe en Logcat, se pierde. Por eso las
// validaciones parecian mudas. Log.d si aparece, y se puede filtrar por TAG.
private const val TAG = "NotasVM"
