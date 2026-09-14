package com.ddam_a1.gestornotas.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddam_a1.gestornotas.data.NotaRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.ddam_a1.gestornotas.modelClasses.DIAS_EN_PAPELERA
import com.ddam_a1.gestornotas.modelClasses.Nota
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@HiltViewModel
class NotasViewModel @Inject constructor(
        private val repo: NotaRepositorio
) : ViewModel() {

    // MAYÚSCULA AL INICIO: ANOTACIÓN O TIPO
    // MINÚSCULA AL INICIO: FUNCIÓN O PROPIEDAD

    //===== ATRIBUTOS Y variables? ===== (para separar ok? Porque no le sé bien todavía)

    // agarra el StateIn para traducir el flow:
    // viewModelScope es lit el scope que tiene (mientras exista el ViewModel)
    // while suscribed es para que aguante 5 segundos mientras no se ve el elemento (para no reiniciar en rotación)
    // emptylist() es el valor mientras jala los datos, vacío para que no truene
    val listaNotas: StateFlow<List<Nota>> = repo.leerBandejaStream().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
        )

    val listaPapelera: StateFlow<List<Nota>> = repo.leerPapeleraStream().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )


    //===== MÉTODOS =====
    fun agregar(nota: Nota): Boolean {
        if (nota.titulo.isBlank()){ //Blank tmb capta los " "
            Log.d(TAG, "La nota no puede tener un título vacío")
            return false
        }

        viewModelScope.launch { repo.agregar(nota) }
        Log.d(TAG, "Nota añadida !!")
        return true
    }

    // Guarda los cambios de uno que YA existe.
    fun actualizar(nota: Nota): Boolean {
        if (nota.titulo.isBlank()) return false

        viewModelScope.launch { repo.actualizar(nota) }
        return true
    }

    fun meterPapelera(id: String) {
        viewModelScope.launch { repo.meterPapelera(id, LocalDateTime.now()) }
        Log.d(TAG, "Nota enviada a la papelera")
    }

    fun sacarPapelera(id: String) {
        viewModelScope.launch { repo.sacarPapelera(id) }
        Log.d(TAG, "Nota enviada a la bandeja de entrada")
    }

    // Es el mismo cosito de la clase (el Elvis), puede No tener string y no devolver un Nota, es opcional}

    // Yo sé pueden hacer funciones con el mismo nombre y diferentes parámentros
    // no me acuerdo del nombre. pero pues mmmm...
    // Es un intento del .getOrPut del map pero para listas

    // Acá no devuelve booleano, da la nota
    // es suspend porque espera junto a la del repo, no como launch que manda y acaba la función
    // leer, a diferencia de actualizar u otros, SÍ jala algo de la base. Por eso espera.
    suspend fun leer(id: String?) : Nota? {
        // Por si es CREACIÓN de uno (para la pantalla que es la misma de crear y leer)
        if (id == null ) { return null }

        // busca y regresa nota
         return repo.leerNota(notaID = id)
    }

    // FUNCIÓN QUE SE LLAMA AL ENTRAR
    // es para no programar cositas y eso como notis
    fun limpiarPapelera(){
        // si la fecha es menor a esto entonces mueren :D
        val fechaExpirado = LocalDateTime.now().minusDays(DIAS_EN_PAPELERA)

        // omg removeAll, tqm tqm
        // Otra vez el Elvis para evitar errores si no está definido, etc
        viewModelScope.launch { repo.vaciarPapelera(fechaExpirado) }
    }
}

// El TAG es la etiqueta con la que filtras en Logcat.
// `print()` NO sirve en Android: no escribe en Logcat, se pierde. Por eso las
// validaciones parecen mudas. Log.d sí aparece, y se puede filtrar por TAG.
private const val TAG = "NotasVM"
