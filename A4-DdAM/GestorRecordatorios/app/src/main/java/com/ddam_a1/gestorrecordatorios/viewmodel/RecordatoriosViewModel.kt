package com.ddam_a1.gestorrecordatorios.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.ddam_a1.gestorrecordatorios.modelClasses.DIAS_EN_PAPELERA
import com.ddam_a1.gestorrecordatorios.modelClasses.Recordatorio
import java.time.LocalDateTime

@HiltViewModel
class RecordatoriosViewModel @Inject constructor() : ViewModel() {

    //===== ATRIBUTOS Y variables? ===== (para separar ok? Porque no le sé bien todavía)

    // Primero se define una lista para poder utilizar los métodos.
    // El viewModel la crea y no la mata si cambia la UI :D

    // Y tmb se crean las listas para mandar a las interfaces.

    private val _recordatorios = mutableStateListOf<Recordatorio>()
    // por seguridad es private, para que no le estén moviendo
    // el _ es porque es buena práctica ponérselo cuando es privada

    val ListaRecordatorios: List<Recordatorio>
        get() = _recordatorios
            .filter { it.enPapelera == false }
            .sortedBy { it.fechaRecordatorio }
        // El it es como un i en los loops, sirve para ir iterando

    val ListaPapelera: List<Recordatorio>
        get() = _recordatorios
            .filter { it.enPapelera }
            .sortedByDescending { it.fechaEliminado }


    //===== MÉTODOS ===== (para separar ok? Porque no le sé bien todavía)

    fun agregar(recordatorio: Recordatorio): Boolean {
        if (recordatorio.titulo.isBlank()){ //Blank tmb capta los " "
            Log.d(TAG, "El recordatorio no puede tener un título vacío")
            return false
        }
        if (recordatorio.fechaRecordatorio.isBefore(LocalDateTime.now())){
            Log.d(TAG, "El recordatorio no puede ser en el pasado")
            return false
        }

        _recordatorios.add(recordatorio)
        Log.d(TAG, "Recoradtorio añadido !!")
        return true
    }

    // Guarda los cambios de uno que YA existe.
    // Se busca por id y se reemplaza completo: el objeto que llega del formulario
    // ya trae el mismo id (salio de un .copy()), asi que conserva su identidad.
    fun actualizar(recordatorio: Recordatorio): Boolean {
        if (recordatorio.titulo.isBlank()) return false

        val i = _recordatorios.indexOfFirst { it.id == recordatorio.id }
        if (i < 0) return false

        _recordatorios[i] = recordatorio
        return true
    }

    fun meterPapelera(id: String) : Boolean {
        // Idealmente se cambia el valor, pero son inmutables entonces:
        val i = _recordatorios.indexOfFirst { it.id == id } // busca la primera cosa con el id y guarda el index

        // si no lo encuentra (no está después del 0) devuelve -1, entonces:
        if (i < 0){
            Log.d(TAG, "El recordatorio no existe")
            return false
        }

        // Lo que se hace acá es meterlo, lo copia tal cual pero cambia el valor de papelera
        _recordatorios[i] = _recordatorios[i].copy(enPapelera = true, fechaEliminado = LocalDateTime.now())
        Log.d(TAG, "Recordatorio enviado a la papelera")
        return true
    }

    fun sacarPapelera(id: String) : Boolean {
        val i = _recordatorios.indexOfFirst { it.id == id }
        if (i < 0){
            Log.d(TAG, "El recordatorio no existe")
            return false
        }
        _recordatorios[i] = _recordatorios[i].copy(enPapelera = false,  fechaEliminado = null)
        Log.d(TAG, "Recordatorio enviado a la bandeja de entrada")
        return true
    }

    // Es el mismo cosito de la clase (el Elvis), puede No tener string y no devolver un Recordatorio, es opcional}

    // Yo sé pueden hacer funciones con el mismo nombre y diferentes parámentros
    // no me acuerdo del nombre. pero pues mmmm...
    // Es un intento del .getOrPut del map pero para listas

    // Acá no devuelve booleano, da el recordatorio
    fun leer(id: String?) : Recordatorio? {
        // Por si es CREACIÓN de uno (para la pantalla que es la misma de crear y leer)
        if (id == null ) { return null }

        // busca y regresa recordatorio
        return _recordatorios.find { it.id == id }
    }

    // FUNCIÓN QUE SE LLAMA AL ENTRAR
    // es para no programar cositas y eso como notis
    fun limpiarPapelera(){
        val fechaActual = LocalDateTime.now()

        // omg removeAll, tqm tqm
        // Otra vez el Elvis para evitar errores si no está definido, etc
        _recordatorios.removeAll { it.fechaEliminado?.plusDays(DIAS_EN_PAPELERA)?.isBefore(fechaActual) == true }
    }
}

// El TAG es la etiqueta con la que filtras en Logcat.
// `print()` NO sirve en Android: no escribe en Logcat, se pierde. Por eso las
// validaciones parecian mudas. Log.d si aparece, y se puede filtrar por TAG.
private const val TAG = "RecordatoriosVM"
