package com.ddam_a1.gestorrecordatorios.ui.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestorrecordatorios.R
import com.ddam_a1.gestorrecordatorios.modelClasses.DIAS_EN_PAPELERA
import com.ddam_a1.gestorrecordatorios.modelClasses.Recordatorio
import com.ddam_a1.gestorrecordatorios.modelClasses.nivelPrioridad
import com.ddam_a1.gestorrecordatorios.ui.theme.GestorRecordatoriosTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// ============================================================
//  MEDIDAS — tomadas de Figma (componente "Card Recordatorios")
//
//  En el Figma la tarjeta mide 800x151 con estos valores internos:
//    - padding de 24 por todos lados
//    - la fecha y el título van pegados (sin hueco entre ellos)
//    - 16 de separación entre el título y la descripción
// ============================================================

internal val RADIO_TARJETA = 20.dp
internal val PADDING_TARJETA = 24.dp
internal val SEPARACION_TITULO_DESCRIPCION = 16.dp

/**
 * Sombra de la tarjeta.
 *
 * En Figma una sombra se define con cuatro valores (x, y, desenfoque, color).
 * En Compose le das UN solo número, la "elevación", y Android calcula la sombra
 * con su propio modelo de luz. Por eso una sombra de Figma no se traduce, se APROXIMA:
 * subes o bajas este número hasta que se vea como tu diseño.
 */
internal val ELEVACION_TARJETA = 3.dp

/**
 * Los estados de la tarjeta, calcados de las variantes de Figma.
 *
 * Es un `enum` y no varios booleanos sueltos (¿`seleccionado` + `enPapelera`?)
 * porque los estados son EXCLUYENTES: una tarjeta no puede estar seleccionada
 * Y en la papelera al mismo tiempo. Con booleanos ese caso imposible sí se podría
 * escribir, y tarde o temprano alguien lo escribe.
 */
enum class EstadoCard { NORMAL, SELECCIONADO, EN_PAPELERA }

/**
 * Tarjeta de un recordatorio. Se pliega y despliega al tocarla.
 *
 * DECISIONES IMPORTANTES
 *
 * 1. El estado "expandida" vive AQUÍ ADENTRO (`remember`). Cada tarjeta se acuerda
 *    sola de si está abierta, y la pantalla no tiene que llevar la cuenta.
 *
 * 2. El `estado` (normal / seleccionado / papelera) SÍ entra por parámetro, porque
 *    eso no lo decide la tarjeta: lo decide la pantalla según dónde esté.
 *
 * 3. `Modifier.animateContentSize()` anima el plegado solo, sin programar nada.
 *
 * @param recordatorio los datos que se van a mostrar
 * @param estado cuál de las variantes de se dibuja (papelera, desplegado, seleccionado, etc)
 * @param onEditar si le pasas algo, aparece un botón "Editar" al desplegarse
 * @param onClick si le pasas algo, tocar la tarjeta hace ESO en vez de plegarla.
 *        Sirve para la vista doble de tablet: ahí tocar una tarjeta la SELECCIONA
 *        y el detalle se ve en el panel de al lado, así que la tarjeta ya no
 *        necesita desplegarse ni mostrar su flecha. Una tarjeta que delega su
 *        click no administra su propio desplegado, y por eso tampoco dibuja el
 *        control de desplegarse: un botón que no hace nada confunde más que
 *        ayudar.
 */
@Composable
fun CardRecordatorio(
    recordatorio: Recordatorio,
    modifier: Modifier = Modifier,
    estado: EstadoCard = EstadoCard.NORMAL,
    onEditar: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    // Si alguien más se encarga del click, esta tarjeta nunca se despliega sola.
    val sePliega = onClick == null

    var expandida by remember { mutableStateOf(false) }

    // La flecha gira 180° al desplegarse. `animateFloatAsState` hace la transiciónn con esto:
    val rotacionFlecha by animateFloatAsState(
        targetValue = if (expandida) 180f else 0f,
        label = "rotacionFlecha"
    )

    val colorTexto = colorTextoDe(estado)
    val colorSecundario = colorSecundarioDe(estado)

    Card(
        shape = RoundedCornerShape(RADIO_TARJETA),
        colors = CardDefaults.cardColors(containerColor = colorFondoDe(estado)),
        elevation = CardDefaults.cardElevation(defaultElevation = ELEVACION_TARJETA),
        modifier = modifier
            .fillMaxWidth()
            .clickable { if (sePliega) expandida = !expandida else onClick!!() }
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(PADDING_TARJETA)) {

            // ---- La línea de fecha, arriba a la derecha ----
            // En la bandeja dice "hace 2 h" en gris.
            // En la papelera dice "se elimina el 12/09/2026" en ROJO.
            // Es el mismo hueco contando dos cosas distintas según dónde estés.
            Text(
                text = if (estado == EstadoCard.EN_PAPELERA)
                    textoEliminacion(recordatorio.fechaEliminado)
                else
                    hace(recordatorio.fechaRecordatorio),
                style = MaterialTheme.typography.labelSmall,
                color = if (estado == EstadoCard.EN_PAPELERA)
                    MaterialTheme.colorScheme.error
                else colorSecundario,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            // Sin separador aquí: la fecha y el título van pegados,
            // porque son un mismo bloque de información.

            // ---- Prioridad + título ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                IndicadorPrioridad(recordatorio.prioridad)
                Text(
                    text = recordatorio.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorTexto
                )
            }

            Spacer(Modifier.height(SEPARACION_TITULO_DESCRIPCION))

            // ---- Descripción ----
            // Plegada: 2 líneas con "...". Desplegada: completa.
            // `maxLines` + `Ellipsis` es lo que evita que el texto se salga.
            Text(
                text = recordatorio.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = colorSecundario,
                maxLines = if (expandida) Int.MAX_VALUE else 2,
                overflow = if (expandida) TextOverflow.Clip else TextOverflow.Ellipsis
            )

            if (expandida) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Recordar el ${formatoLargo(recordatorio.fechaRecordatorio)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorTexto
                )
            }

            // ---- Fila de abajo: botón editar (opcional) + flecha ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (expandida && onEditar != null) {
                    TextButton(onClick = onEditar) { Text("Editar", color = colorTexto) }
                }

                if (sePliega) {
                    IconButton(onClick = { expandida = !expandida }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_expand),
                            // Describe lo que VA A PASAR, y cambia con el estado.
                            contentDescription = if (expandida) "Contraer recordatorio"
                            else "Expandir recordatorio",
                            tint = colorTexto,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(rotacionFlecha)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
//  Colores por estado
//  Cada variante mapeada a un rol del tema.
// ============================================================

@Composable
internal fun colorFondoDe(estado: EstadoCard): Color = when (estado) {
    EstadoCard.NORMAL -> MaterialTheme.colorScheme.surface                 // crema #FFF8F3
    EstadoCard.SELECCIONADO -> MaterialTheme.colorScheme.tertiaryContainer // verde claro
    EstadoCard.EN_PAPELERA -> MaterialTheme.colorScheme.surfaceDim         // beige apagado
}

@Composable
internal fun colorTextoDe(estado: EstadoCard): Color = when (estado) {
    EstadoCard.SELECCIONADO -> MaterialTheme.colorScheme.onTertiaryContainer
    else -> MaterialTheme.colorScheme.onSurface
}

@Composable
internal fun colorSecundarioDe(estado: EstadoCard): Color = when (estado) {
    EstadoCard.SELECCIONADO -> MaterialTheme.colorScheme.onTertiaryContainer
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

// ============================================================
//  Fondos de swipe
//  Lo que se ve DETRÁS de la tarjeta cuando la deslizas.
//
//  La tarjeta no se encoge, se DESLIZA. Este bloque
//  está debajo todo el tiempo y se va descubriendo conforme la tarjeta se corre.
// ============================================================

enum class AccionSwipe { BORRAR, RECUPERAR }

/**
 * El bloque de color que aparece al deslizar una tarjeta.
 *
 * Cómo se usa con el componente de Material, cuando llegues al punto 4:
 *
 *     SwipeToDismissBox(
 *         state = estadoSwipe,
 *         backgroundContent = { FondoSwipe(AccionSwipe.BORRAR) }
 *     ) {
 *         CardRecordatorio(recordatorio)
 *     }
 */
@Composable
fun FondoSwipe(
    accion: AccionSwipe,
    modifier: Modifier = Modifier
) {
    val fondo: Color
    val contenido: Color
    @DrawableRes val icono: Int
    val descripcion: String

    when (accion) {
        AccionSwipe.BORRAR -> {
            fondo = MaterialTheme.colorScheme.errorContainer
            contenido = MaterialTheme.colorScheme.error
            icono = R.drawable.ic_trash
            descripcion = "Mover a la papelera"
        }
        AccionSwipe.RECUPERAR -> {
            fondo = MaterialTheme.colorScheme.tertiaryContainer
            contenido = MaterialTheme.colorScheme.onTertiaryContainer
            icono = R.drawable.ic_movetoinbox
            descripcion = "Recuperar recordatorio"
        }
    }

    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(RADIO_TARJETA))
            .background(fondo)
            .padding(horizontal = 28.dp)
    ) {
        Icon(
            painter = painterResource(icono),
            contentDescription = descripcion,
            tint = contenido,
            modifier = Modifier.size(32.dp)
        )
    }
}

// ============================================================
//  Indicador de prioridad
// ============================================================

/**
 * El puntito de prioridad que va antes del título.
 *
 * 1. En NULA no dibuja NADA, ni siquiera un hueco: un recordatorio sin prioridad
 *    no debe pagar espacio por una propiedad que no tiene.
 *
 * 2. El `contentDescription` dice "Prioridad alta" en palabras, y es OBLIGATORIO:
 *    si la única señal fuera el color, alguien con daltonismo o usando TalkBack
 *    no se enteraría.
 */

@Composable
fun IndicadorPrioridad(
    prioridad: nivelPrioridad,
    modifier: Modifier = Modifier
) {
    if (prioridad == nivelPrioridad.NULA) return

    Icon(
        painter = painterResource(R.drawable.ic_prioridad),
        contentDescription = descripcionPrioridad(prioridad),
        tint = colorPrioridad(prioridad),
        modifier = modifier
            .padding(end = 6.dp)
            .size(18.dp)
    )
}

@Composable
private fun colorPrioridad(prioridad: nivelPrioridad): Color = when (prioridad) {
    nivelPrioridad.ALTA -> MaterialTheme.colorScheme.error                 // rojo
    nivelPrioridad.MEDIA -> MaterialTheme.colorScheme.primaryContainer     // amarillito
    nivelPrioridad.BAJA -> MaterialTheme.colorScheme.tertiary              // verde
    nivelPrioridad.NULA -> Color.Transparent
}

private fun descripcionPrioridad(prioridad: nivelPrioridad): String = when (prioridad) {
    nivelPrioridad.ALTA -> "Prioridad alta"
    nivelPrioridad.MEDIA -> "Prioridad media"
    nivelPrioridad.BAJA -> "Prioridad baja"
    nivelPrioridad.NULA -> "Sin prioridad"
}

// ============================================================
//  Ayudantes de fecha
//  `private` porque solo los usa esta tarjeta. Si otra pantalla los necesita,
//  se pueden mover a un archivo en ui y quítarles el private.
// ============================================================

/** Texto relativo: "hace 2 h", "en 3 d". */
internal fun hace(momento: LocalDateTime): String {
    val minutos = ChronoUnit.MINUTES.between(momento, LocalDateTime.now())
    val futuro = minutos < 0
    val abs = kotlin.math.abs(minutos)

    val cantidad = when {
        abs < 1 -> return "ahora"
        abs < 60 -> "$abs min"
        abs < 1440 -> "${abs / 60} h"
        else -> "${abs / 1440} d"
    }
    return if (futuro) "en $cantidad" else "hace $cantidad"
}

private val FORMATO_LARGO = DateTimeFormatter.ofPattern("d 'de' MMMM, HH:mm", Locale("es", "MX"))
private val FORMATO_CORTO = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "MX"))

internal fun formatoLargo(momento: LocalDateTime): String = momento.format(FORMATO_LARGO)

/**
 * "se elimina el 12/09/2026", que es lo que dice en la papelera.
 *
 * El `?:` (Elvis) cubre el caso raro de un recordatorio en la papelera SIN fecha
 * de eliminación. No debería pasar, pero si pasa, mejor un texto honesto que
 * una app cerrada.
 */
internal fun textoEliminacion(fechaEliminado: LocalDateTime?): String =
    fechaEliminado
        ?.plusDays(DIAS_EN_PAPELERA)
        ?.format(FORMATO_CORTO)
        ?.let { "se elimina el $it" }
        ?: "sin fecha de eliminación"

// ============================================================
//  Previews — uno por cada variante de tu Figma
// ============================================================

private const val FONDO_APP = 0xFFEAE2D4

private fun ejemplo(prioridad: nivelPrioridad = nivelPrioridad.NULA) = Recordatorio(
    titulo = "Tema del Recordatorio",
    descripcion = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Pellentesque a nisi ultricies, faucibus risus feugiat, maximus augue. " +
            "Etiam felis libero, viverra sed enim quis.",
    fechaRecordatorio = LocalDateTime.now().plusDays(2),
    fechaEliminado = LocalDateTime.now(),
    prioridad = prioridad
)

@Preview(name = "1 · Normal", showBackground = true, backgroundColor = FONDO_APP)
@Composable
private fun CardNormalPreview() {
    GestorRecordatoriosTheme {
        CardRecordatorio(ejemplo(), Modifier.padding(16.dp))
    }
}

@Preview(name = "2 · Prioridad alta", showBackground = true, backgroundColor = FONDO_APP)
@Composable
private fun CardPrioridadPreview() {
    GestorRecordatoriosTheme {
        CardRecordatorio(ejemplo(nivelPrioridad.ALTA), Modifier.padding(16.dp), onEditar = {})
    }
}

@Preview(name = "3 · Seleccionada", showBackground = true, backgroundColor = FONDO_APP)
@Composable
private fun CardSeleccionadaPreview() {
    GestorRecordatoriosTheme {
        CardRecordatorio(ejemplo(), Modifier.padding(16.dp), estado = EstadoCard.SELECCIONADO)
    }
}

@Preview(name = "4 · En la papelera", showBackground = true, backgroundColor = FONDO_APP)
@Composable
private fun CardPapeleraPreview() {
    GestorRecordatoriosTheme {
        CardRecordatorio(ejemplo(), Modifier.padding(16.dp), estado = EstadoCard.EN_PAPELERA)
    }
}

@Preview(name = "5 · Swipe · borrar", showBackground = true, backgroundColor = FONDO_APP, heightDp = 140)
@Composable
private fun FondoBorrarPreview() {
    GestorRecordatoriosTheme {
        Box(Modifier.padding(16.dp)) { FondoSwipe(AccionSwipe.BORRAR) }
    }
}

@Preview(name = "6 · Swipe · recuperar", showBackground = true, backgroundColor = FONDO_APP, heightDp = 140)
@Composable
private fun FondoRecuperarPreview() {
    GestorRecordatoriosTheme {
        Box(Modifier.padding(16.dp)) { FondoSwipe(AccionSwipe.RECUPERAR) }
    }
}
