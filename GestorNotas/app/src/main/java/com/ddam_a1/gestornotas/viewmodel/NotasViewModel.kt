package com.ddam_a1.gestornotas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddam_a1.gestornotas.data.NotaRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.ddam_a1.gestornotas.modelClasses.DIAS_EN_PAPELERA
import com.ddam_a1.gestornotas.modelClasses.Imagen
import com.ddam_a1.gestornotas.modelClasses.Nota
import kotlinx.coroutines.flow.Flow
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

        // El Log va DENTRO del launch: launch no espera, arranca la corrutina y
        // sigue. Si lo dejas afuera, imprime "añadida" antes de que el insert
        // haya tocado la base. Adentro, sí dice la verdad.
        viewModelScope.launch {
            repo.agregar(nota)
            Log.d(TAG, "Nota añadida !!")
        }
        return true
    }

    // Guarda los cambios de uno que YA existe.
    fun actualizar(nota: Nota): Boolean {
        if (nota.titulo.isBlank()) return false

        viewModelScope.launch { repo.actualizar(nota) }
        return true
    }

    fun meterPapelera(id: String) {
        viewModelScope.launch {
            repo.meterPapelera(id, LocalDateTime.now())
            Log.d(TAG, "Nota enviada a la papelera")
        }
    }

    fun sacarPapelera(id: String) {
        viewModelScope.launch {
            repo.sacarPapelera(id)
            Log.d(TAG, "Nota enviada a la bandeja de entrada")
        }
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

    // ===== IMAGENES =====

    /**
     * Las fotos de una nota, en vivo.
     *
     * Devuelve el `Flow` tal cual, SIN `stateIn`. Las dos listas de arriba sí lo
     * usan porque son fijas (bandeja y papelera son siempre las mismas); ésta
     * depende de CUAL nota estés viendo, y un StateFlow por id habría que
     * guardarlo en un mapa y limpiarlo a mano. La pantalla lo convierte con
     * `collectAsState(emptyList())` y se acabó.
     */
    fun imagenesDe(notaId: String): Flow<List<Imagen>> = repo.imagenesDe(notaId)

    /**
     * Adjunta una foto YA COPIADA dentro de la app.
     *
     * Recibe la nota completa, no sólo su id, y eso es a propósito: la llave
     * foránea exige que la nota EXISTA antes de guardar una imagen que la
     * apunte. Si acabas de abrir una nota nueva y lo primero que haces es poner
     * una foto, esa nota todavía no está en la base. Por eso primero `guardar`
     * (el Upsert) y luego la imagen: en ese orden, siempre hay a quién apuntar.
     *
     * Efecto secundario a tener presente: adjuntar una foto guarda la nota
     * aunque el título esté vacío. Es deliberado — si te tomaste la molestia de
     * elegir una imagen, la nota ya vale la pena.
     */
    fun agregarImagen(nota: Nota, ruta: String) {
        viewModelScope.launch {
            repo.guardar(nota)
            repo.agregarImagen(Imagen(notaId = nota.id, ruta = ruta))
            Log.d(TAG, "Imagen adjuntada a ${nota.id}")
        }
    }

    /** Quita una foto de la nota. El archivo lo borra quien llama. */
    fun quitarImagen(imagen: Imagen) {
        viewModelScope.launch {
            repo.borrarImagen(imagen)
            Log.d(TAG, "Imagen quitada")
        }
    }

    /** Guarda una nota exista o no. Es lo que usa la pantalla de Vista. */
    fun guardar(nota: Nota): Boolean {
        if (nota.titulo.isBlank()) {
            Log.d(TAG, "La nota no puede tener un título vacío")
            return false
        }
        viewModelScope.launch {
            repo.guardar(nota)
            Log.d(TAG, "Nota guardada !!")
        }
        return true
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
