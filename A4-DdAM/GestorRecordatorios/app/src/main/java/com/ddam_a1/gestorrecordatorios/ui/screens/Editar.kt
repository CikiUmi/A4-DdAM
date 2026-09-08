package com.ddam_a1.gestorrecordatorios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestorrecordatorios.R
import com.ddam_a1.gestorrecordatorios.modelClasses.Recordatorio
import com.ddam_a1.gestorrecordatorios.modelClasses.nivelPrioridad
import com.ddam_a1.gestorrecordatorios.ui.theme.GestorRecordatoriosTheme
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// ============================================================
//  PUNTO 5 · FORMULARIO CON VALIDACIONES
//
//  Solo compact y medium: es una columna que crece hasta un ancho maximo y se
//  queda centrada. No hay variante de tablet a dos columnas.
// ============================================================

private val SEPARACION_CAMPOS = 20.dp

private const val MAX_TITULO = 60
private const val MAX_DESCRIPCION = 300

/**
 * Cuanto espera la validacion antes de quejarse, en milisegundos.
 *
 * ESTE NUMERO ES LA RAZON DE SER DEL LaunchedEffect.
 *
 * Si validaras de forma directa (un `if` en medio del composable, o un
 * `derivedStateOf`), el error apareceria en la PRIMERA letra: escribes "E" para
 * "Entregar" y la app ya te grito. Se siente hostil.
 *
 * `LaunchedEffect` te da una CORRUTINA atada a sus llaves. Cada vez que el texto
 * cambia, Compose CANCELA la corrutina anterior y lanza una nueva. Entonces el
 * `delay` de abajo casi nunca termina... hasta que dejas de escribir 400ms.
 * Ahi si corre la validacion. Eso se llama "debounce", y sin corrutinas habria
 * que armarlo a mano con Handlers y banderas.
 */
private const val RETARDO_VALIDACION = 400L

private val F_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val F_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Formulario de crear / editar.
 *
 * @param original el recordatorio que se esta editando, o NULL si es uno nuevo.
 *        Es el mismo `String?` que recibe `leer(id)` en tu ViewModel: un id nulo
 *        significa "vengo a crear". Una sola pantalla, dos modos.
 * @param onGuardar recibe el recordatorio ya armado y validado
 */
@Composable
fun Editar(
    original: Recordatorio?,
    margenes: Margenes,
    onGuardar: (Recordatorio) -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esNuevo = original == null

    // ---- ESTADO DEL FORMULARIO ----
    // rememberSaveable y no remember: si el usuario gira el telefono a la mitad
    // de escribir, no puede perder lo que llevaba.
    var titulo by rememberSaveable { mutableStateOf(original?.titulo ?: "") }
    var descripcion by rememberSaveable { mutableStateOf(original?.descripcion ?: "") }
    var fechaTexto by rememberSaveable {
        mutableStateOf((original?.fechaRecordatorio?.toLocalDate() ?: LocalDate.now()).format(F_FECHA))
    }
    var horaTexto by rememberSaveable {
        mutableStateOf((original?.fechaRecordatorio?.toLocalTime() ?: LocalTime.of(9, 0)).format(F_HORA))
    }
    var prioridad by rememberSaveable { mutableStateOf(original?.prioridad ?: nivelPrioridad.NULA) }

    // ---- "YA LO TOCO" ----
    // Sin esto, el formulario se abriria con "El titulo es obligatorio" en rojo
    // antes de que el usuario escriba nada. Regla: no reclames por un campo que
    // la persona todavia no ha tocado.
    var tocoTitulo by rememberSaveable { mutableStateOf(false) }
    var tocoFecha by rememberSaveable { mutableStateOf(false) }

    // ---- LOS ERRORES ----
    // null = sin error. Es un String? y no un Boolean porque el mensaje forma
    // parte del error: "obligatorio" y "muy largo" no se arreglan igual.
    var errorTitulo by remember { mutableStateOf<String?>(null) }
    var errorDescripcion by remember { mutableStateOf<String?>(null) }
    var errorFecha by remember { mutableStateOf<String?>(null) }

    // Intento de convertir los dos textos en una fecha real.
    // Devuelve null si algo no cuadra: "32/13/2026", "25:00", letras...
    val fechaHora: LocalDateTime? = remember(fechaTexto, horaTexto) {
        parsearFechaHora(fechaTexto, horaTexto)
    }

    // ---- VALIDACIONES ----
    // Una por campo. Cada una se relanza sola cuando cambia alguna de sus llaves.

    LaunchedEffect(titulo, tocoTitulo) {
        if (!tocoTitulo) {
            errorTitulo = null
            return@LaunchedEffect
        }
        delay(RETARDO_VALIDACION)          // <- si sigues escribiendo, nunca llega aqui
        errorTitulo = when {
            titulo.isBlank() -> "El titulo es obligatorio"
            titulo.length > MAX_TITULO -> "Maximo $MAX_TITULO caracteres"
            else -> null
        }
    }

    LaunchedEffect(descripcion) {
        delay(RETARDO_VALIDACION)
        errorDescripcion =
            if (descripcion.length > MAX_DESCRIPCION) "Maximo $MAX_DESCRIPCION caracteres"
            else null
    }

    LaunchedEffect(fechaTexto, horaTexto, tocoFecha) {
        if (!tocoFecha) {
            errorFecha = null
            return@LaunchedEffect
        }
        delay(RETARDO_VALIDACION)
        errorFecha = when {
            fechaHora == null -> "Usa dd/mm/aaaa y hh:mm"
            // Solo se exige futuro al CREAR. Al editar uno viejo seria absurdo
            // obligar a moverle la fecha solo porque ya paso.
            esNuevo && fechaHora.isBefore(LocalDateTime.now()) -> "La fecha ya paso"
            else -> null
        }
    }

    // ---- SE PUEDE GUARDAR? ----
    // Ojo: esto NO mira los errores de arriba, recalcula la verdad al instante.
    // Los errores estan retrasados 400ms a proposito (para no molestar mientras
    // escribes); el boton no puede tener ese retraso, o dejarias guardar basura
    // durante ese ratito.
    val sePuedeGuardar =
        titulo.isNotBlank() &&
        titulo.length <= MAX_TITULO &&
        descripcion.length <= MAX_DESCRIPCION &&
        fechaHora != null &&
        (!esNuevo || !fechaHora.isBefore(LocalDateTime.now()))

    // ---- LA INTERFAZ ----
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())   // con el teclado abierto no cabe todo
            .imePadding()                            // sube el contenido sobre el teclado
            .padding(horizontal = margenes.lateral)
            .padding(top = margenes.superior)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = margenes.anchoMaximoFormulario)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = if (esNuevo) "Nuevo recordatorio" else "Editar recordatorio",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(SEPARACION_CAMPOS))

            // ---- TITULO ----
            OutlinedTextField(
                value = titulo,
                onValueChange = {
                    titulo = it
                    tocoTitulo = true
                },
                label = { Text("Titulo") },
                singleLine = true,
                isError = errorTitulo != null,
                supportingText = {
                    // Un solo hueco que dice el error O el contador. Nunca los dos:
                    // si hay error, es lo unico que importa leer.
                    Text(errorTitulo ?: "${titulo.length} / $MAX_TITULO")
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(SEPARACION_CAMPOS))

            // ---- DESCRIPCION ----
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripcion") },
                isError = errorDescripcion != null,
                supportingText = {
                    Text(errorDescripcion ?: "Opcional")
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
            )

            Spacer(Modifier.height(SEPARACION_CAMPOS))

            // ---- FECHA Y HORA ----
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fechaTexto,
                    onValueChange = {
                        fechaTexto = it
                        tocoFecha = true
                    },
                    label = { Text("Fecha") },
                    placeholder = { Text("dd/mm/aaaa") },
                    singleLine = true,
                    isError = errorFecha != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(2f)
                )
                OutlinedTextField(
                    value = horaTexto,
                    onValueChange = {
                        horaTexto = it
                        tocoFecha = true
                    },
                    label = { Text("Hora") },
                    placeholder = { Text("hh:mm") },
                    singleLine = true,
                    isError = errorFecha != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // El error de fecha y hora es UNO solo, debajo de los dos campos:
            // "la fecha ya paso" no es culpa de un campo ni del otro, es de la
            // combinacion. Por eso los dos se pintan en rojo juntos.
            if (errorFecha != null) {
                Text(
                    text = errorFecha!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(SEPARACION_CAMPOS))

            // ---- PRIORIDAD ----
            Text(
                text = "Prioridad",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                nivelPrioridad.entries.forEach { nivel ->
                    ChipPrioridad(
                        nivel = nivel,
                        seleccionado = nivel == prioridad,
                        onClick = { prioridad = nivel }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ---- BOTONES ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancelar) { Text("Cancelar") }

                Spacer(Modifier.size(12.dp))

                Button(
                    // Deshabilitado es mejor que dejar picar y luego reclamar:
                    // el usuario ve que falta algo ANTES de intentar.
                    enabled = sePuedeGuardar,
                    onClick = {
                        val cuando = fechaHora ?: return@Button
                        // Si es nuevo, Recordatorio() genera un id fresco.
                        // Si es edicion, .copy() conserva el id original: por eso
                        // `actualizar` lo encuentra en la lista.
                        val base = original ?: Recordatorio()
                        onGuardar(
                            base.copy(
                                titulo = titulo.trim(),
                                descripcion = descripcion.trim(),
                                fechaRecordatorio = cuando,
                                prioridad = prioridad
                            )
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (esNuevo) "Crear" else "Guardar")
                }
            }

            Spacer(Modifier.height(margenes.inferior))   // aire sobre la barra inferior
        }
    }
}

// ============================================================
//  Piezas
// ============================================================

/**
 * Convierte los dos textos en una fecha real, o null si no se puede.
 *
 * El try/catch aqui SI hace falta (a diferencia del que quitamos de tu
 * ViewModel): `parse` lanza excepcion con cualquier cosa que el usuario escriba,
 * y eso no es un bug, es lo normal mientras alguien teclea.
 */
private fun parsearFechaHora(fecha: String, hora: String): LocalDateTime? = try {
    LocalDateTime.of(
        LocalDate.parse(fecha.trim(), F_FECHA),
        LocalTime.parse(hora.trim(), F_HORA)
    )
} catch (e: DateTimeParseException) {
    null
}

/**
 * Boton de prioridad.
 *
 * Dos senales, nunca solo el color: el chip seleccionado cambia de FONDO ademas
 * del color del icono. Si la unica diferencia fuera el color, alguien con
 * daltonismo no distinguiria "media" de "alta".
 */
@Composable
private fun ChipPrioridad(
    nivel: nivelPrioridad,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorNivel = when (nivel) {
        nivelPrioridad.ALTA -> MaterialTheme.colorScheme.error
        nivelPrioridad.MEDIA -> MaterialTheme.colorScheme.primary
        nivelPrioridad.BAJA -> MaterialTheme.colorScheme.tertiary
        nivelPrioridad.NULA -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val etiqueta = when (nivel) {
        nivelPrioridad.ALTA -> "Alta"
        nivelPrioridad.MEDIA -> "Media"
        nivelPrioridad.BAJA -> "Baja"
        nivelPrioridad.NULA -> "Sin prioridad"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .heightIn(min = 48.dp)             // minimo tactil de Material
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (seleccionado) MaterialTheme.colorScheme.secondaryContainer
                else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (seleccionado) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp)
    ) {
        if (nivel != nivelPrioridad.NULA) {
            Icon(
                painter = painterResource(R.drawable.ic_prioridad),
                contentDescription = null,       // lo dice el texto de al lado
                tint = colorNivel,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelLarge,
            color = if (seleccionado) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================================
//  Previews
// ============================================================

private const val FONDO_FORM = 0xFFEAE2D4

@Preview(name = "Nuevo recordatorio", showBackground = true, backgroundColor = FONDO_FORM, widthDp = 412, heightDp = 900)
@Composable
private fun EditarNuevoPreview() {
    GestorRecordatoriosTheme {
        Editar(original = null, margenes = margenesPara(412.dp), onGuardar = {}, onCancelar = {})
    }
}

@Preview(name = "Editando uno", showBackground = true, backgroundColor = FONDO_FORM, widthDp = 412, heightDp = 900)
@Composable
private fun EditarExistentePreview() {
    GestorRecordatoriosTheme {
        Editar(
            original = Recordatorio(
                titulo = "Entregar la practica de DdAM",
                descripcion = "Subir el APK y la documentacion al aula virtual.",
                fechaRecordatorio = LocalDateTime.now().plusDays(1),
                prioridad = nivelPrioridad.ALTA
            ),
            margenes = margenesPara(412.dp),
            onGuardar = {},
            onCancelar = {}
        )
    }
}
