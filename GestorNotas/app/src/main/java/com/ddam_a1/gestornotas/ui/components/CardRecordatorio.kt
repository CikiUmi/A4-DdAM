package com.ddam_a1.gestornotas.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestornotas.R
import com.ddam_a1.gestornotas.modelClasses.DIAS_EN_PAPELERA
import com.ddam_a1.gestornotas.modelClasses.Nota
import com.ddam_a1.gestornotas.ui.theme.GestorNotasTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// ============================================================
//  MEDIDAS — tomadas de Figma (componente "Card Notas")
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
 * Tarjeta de una nota.
 *
 * Ya NO se despliega. Tocarla avisa hacia afuera con `onClick` y la pantalla
 * decide que significa: en telefono abrir la vista de la nota, en tablet
 * seleccionarla para el panel de al lado.
 *
 * Por eso tampoco dibuja la flecha: el control de desplegar no tendria nada que
 * controlar, y un boton que no hace nada confunde mas que ayudar.
 *
 * @param nota los datos que se van a mostrar
 * @param estado cual variante se dibuja (normal, seleccionada, en papelera)
 * @param onClick que hacer al tocarla. Si va null, la tarjeta no es tocable.
 */
@Composable
fun CardNota(
    nota: Nota,
    modifier: Modifier = Modifier,
    estado: EstadoCard = EstadoCard.NORMAL,
    onClick: (() -> Unit)? = null
) {
    val colorTexto = colorTextoDe(estado)
    val colorSecundario = colorSecundarioDe(estado)

    Card(
        shape = RoundedCornerShape(RADIO_TARJETA),
        colors = CardDefaults.cardColors(containerColor = colorFondoDe(estado)),
        elevation = CardDefaults.cardElevation(defaultElevation = ELEVACION_TARJETA),
        modifier = modifier
            .fillMaxWidth()
            // `then` con un Modifier vacio: si nadie da onClick, no se agrega
            // nada. Asi la tarjeta no finge ser tocable cuando no lo es.
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(PADDING_TARJETA)) {

            // ---- La linea de fecha, arriba a la derecha ----
            // En la bandeja dice "hace 2 h" en gris.
            // En la papelera dice "se elimina el 12/09/2026" en ROJO.
            // Es el mismo hueco contando dos cosas distintas segun donde estes.
            Text(
                text = if (estado == EstadoCard.EN_PAPELERA)
                    textoEliminacion(nota.fechaEliminado)
                else
                    hace(nota.fechaNota),
                style = MaterialTheme.typography.labelSmall,
                color = if (estado == EstadoCard.EN_PAPELERA)
                    MaterialTheme.colorScheme.error
                else colorSecundario,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            // Sin separador aqui: la fecha y el titulo van pegados,
            // porque son un mismo bloque de informacion.

            Text(
                text = nota.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = colorTexto
            )

            Spacer(Modifier.height(SEPARACION_TITULO_DESCRIPCION))

            // ---- Adelanto del contenido ----
            // Siempre dos lineas con "...". El texto completo se ve al abrir la
            // nota, no aqui: la tarjeta es un indice, no el documento.
            Text(
                text = nota.contenido,
                style = MaterialTheme.typography.bodyMedium,
                color = colorSecundario,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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
 *         CardNota(nota)
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
            descripcion = "Recuperar nota"
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
 * El `?:` (Elvis) cubre el caso raro de una nota en la papelera SIN fecha
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

private fun ejemplo() = Nota(
    titulo = "Tema del Nota",
    contenido = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Pellentesque a nisi ultricies, faucibus risus feugiat, maximus augue. " +
            "Etiam felis libero, viverra sed enim quis.",
    fechaNota = LocalDateTime.now().plusDays(2),
    fechaEliminado = LocalDateTime.now()
)

@Preview(name = "1 · Normal", showBackground = true, backgroundColor = FONDO_APP)
@Composable
private fun CardNormalPreview() {
    GestorNotasTheme {
        CardNota(ejemplo(), Modifier.padding(16.dp))
    }
}

@Preview(name = "3 · Seleccionada", showBackground = true, backgroundColor = FONDO_APP)
@Composable
private fun CardSeleccionadaPreview() {
    GestorNotasTheme {
        CardNota(ejemplo(), Modifier.padding(16.dp), estado = EstadoCard.SELECCIONADO)
    }
}

@Preview(name = "4 · En la papelera", showBackground = true, backgroundColor = FONDO_APP)
@Composable
private fun CardPapeleraPreview() {
    GestorNotasTheme {
        CardNota(ejemplo(), Modifier.padding(16.dp), estado = EstadoCard.EN_PAPELERA)
    }
}

@Preview(name = "5 · Swipe · borrar", showBackground = true, backgroundColor = FONDO_APP, heightDp = 140)
@Composable
private fun FondoBorrarPreview() {
    GestorNotasTheme {
        Box(Modifier.padding(16.dp)) { FondoSwipe(AccionSwipe.BORRAR) }
    }
}

@Preview(name = "6 · Swipe · recuperar", showBackground = true, backgroundColor = FONDO_APP, heightDp = 140)
@Composable
private fun FondoRecuperarPreview() {
    GestorNotasTheme {
        Box(Modifier.padding(16.dp)) { FondoSwipe(AccionSwipe.RECUPERAR) }
    }
}
