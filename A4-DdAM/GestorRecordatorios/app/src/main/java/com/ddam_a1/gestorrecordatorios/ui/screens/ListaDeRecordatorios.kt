package com.ddam_a1.gestorrecordatorios.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestorrecordatorios.modelClasses.Recordatorio
import com.ddam_a1.gestorrecordatorios.modelClasses.nivelPrioridad
import com.ddam_a1.gestorrecordatorios.ui.components.AccionSwipe
import com.ddam_a1.gestorrecordatorios.ui.components.AnimacionEntrada
import com.ddam_a1.gestorrecordatorios.ui.components.EstadoCard
import com.ddam_a1.gestorrecordatorios.ui.components.RecordatorioDeslizable
import com.ddam_a1.gestorrecordatorios.ui.theme.GestorRecordatoriosTheme
import java.time.LocalDateTime

// ============================================================
//  LA LISTA
//
//  Bandeja y Papelera son LA MISMA pantalla con distintos parametros:
//  otra lista, otro fondo de swipe, otro estado de tarjeta, otra accion.
//  Por eso hay un solo composable y dos pantallas que lo configuran.
//
//  Los espaciados NO se deciden aqui: llegan en `margenes`, que el Andamio
//  calculo a partir del ancho de la ventana. Asi la lista no tiene que saber
//  en que dispositivo esta.
// ============================================================

@Composable
fun ListaDeRecordatorios(
    titulo: String,
    recordatorios: List<Recordatorio>,
    margenes: Margenes,
    accion: AccionSwipe,
    estadoCard: EstadoCard,
    onAccion: (String) -> Unit,
    mensajeVacio: String,
    modifier: Modifier = Modifier,
    onEditar: ((String) -> Unit)? = null,
    // --- solo se usan en la vista doble de tablet ---
    seleccionadoId: String? = null,
    onSeleccionar: ((String) -> Unit)? = null,
    // En la vista doble el titulo lo dibuja la pantalla, arriba de LOS DOS
    // paneles, asi que la lista no debe dibujarlo otra vez.
    mostrarTitulo: Boolean = true
) {
    Column(modifier.fillMaxSize()) {

        if (mostrarTitulo) {
            TituloPantalla(titulo, margenes)
        }

        // ---- Encabezado ----
        // En tu Figma el titulo dice "Mis Recordatorios" en LAS DOS variantes,
        // bandeja y papelera: es el nombre de la app, no el de la seccion. Quien
        // dice donde estas es el indicador de la barra, que ya tiene dos senales
        // (forma + color). Por eso el texto entra por parametro y las dos
        // pantallas le pasan lo mismo.
        //
        // Lleva el MISMO margen lateral que las tarjetas para que quede alineado
        // con ellas; si tuviera el suyo se veria desfasado.
        if (recordatorios.isEmpty()) {
            // ---- Estado vacio ----
            // Una lista vacia NUNCA debe ser una pantalla en blanco: el usuario no
            // sabe si no hay nada o si la app se rompio. El texto tiene que decir
            // las dos cosas: que no hay nada, y que hacer al respecto.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = margenes.lateral * 2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mensajeVacio,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = margenes.anchoMaximoLista)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            // contentPadding y no padding: el padding recorta la lista y las
            // tarjetas se cortarian al hacer scroll. contentPadding deja que el
            // contenido pase por debajo, solo lo separa en reposo.
            contentPadding = PaddingValues(
                start = margenes.lateral,
                end = margenes.lateral,
                top = 0.dp,
                bottom = margenes.inferior
            ),
            verticalArrangement = Arrangement.spacedBy(margenes.entreTarjetas)
        ) {
            items(recordatorios, key = { it.id }) { recordatorio ->
                // La tarjeta abierta en el panel de al lado se pinta con tu
                // variante SELECCIONADO (el verde de tu Figma). Ese estado
                // llevaba semanas programado sin usarse: era justo para esto.
                val estaSeleccionada =
                    seleccionadoId != null && recordatorio.id == seleccionadoId

                AnimacionEntrada {
                    RecordatorioDeslizable(
                        recordatorio = recordatorio,
                        accion = accion,
                        estado = if (estaSeleccionada) EstadoCard.SELECCIONADO else estadoCard,
                        onAccion = onAccion,
                        // Si la pantalla no da onEditar (la papelera no lo da),
                        // la tarjeta no muestra el boton. Un boton que no lleva
                        // a ningun lado es peor que no tener boton.
                        onEditar = onEditar?.let { editar -> { editar(recordatorio.id) } },
                        // Si hay panel de detalle, tocar SELECCIONA en vez de
                        // desplegar. Si no lo hay (telefono), va null y la
                        // tarjeta se sigue plegando ella sola como siempre.
                        onClick = onSeleccionar?.let { sel -> { sel(recordatorio.id) } }
                    )
                }
            }
        }
    }
}

/**
 * El titulo de la pantalla.
 *
 * Vive aparte porque lo usan dos layouts distintos: la lista sola (telefono) y
 * la vista doble (tablet), donde va arriba de los dos paneles. Copiarlo en los
 * dos habria significado que un cambio de estilo se hiciera en dos lados.
 */
@Composable
fun TituloPantalla(
    texto: String,
    margenes: Margenes,
    modifier: Modifier = Modifier
) {
    Text(
        text = texto,
        // displaySmall = la serif (Averia) grande. En el Figma mide 48 de alto y
        // va en el cafe de `primary`, no en el color de texto normal.
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .widthIn(max = margenes.anchoMaximoLista)
            .fillMaxWidth()
            .padding(horizontal = margenes.lateral)
            .padding(top = margenes.superior, bottom = margenes.tituloALista)
    )
}

// ============================================================
//  Previews
// ============================================================

private const val FONDO = 0xFFEAE2D4

internal fun recordatoriosDeEjemplo() = listOf(
    Recordatorio(
        titulo = "Entregar la practica de DdAM",
        descripcion = "Subir el APK y la documentacion al aula virtual antes de las 11:59.",
        fechaRecordatorio = LocalDateTime.now().plusDays(1),
        prioridad = nivelPrioridad.ALTA
    ),
    Recordatorio(
        titulo = "Comprar cafe",
        descripcion = "Del que muele la tienda de la esquina.",
        fechaRecordatorio = LocalDateTime.now().plusDays(3)
    ),
    Recordatorio(
        titulo = "Revisar el Figma del equipo",
        descripcion = "Ver los comentarios que dejaron en la pantalla de editar.",
        fechaRecordatorio = LocalDateTime.now().plusDays(5),
        prioridad = nivelPrioridad.MEDIA
    )
)

@Preview(name = "Lista - compact (margen 16)", showBackground = true, backgroundColor = FONDO, widthDp = 412, heightDp = 720)
@Composable
private fun ListaCompactPreview() {
    GestorRecordatoriosTheme {
        ListaDeRecordatorios(
            titulo = "Mis Recordatorios",
            recordatorios = recordatoriosDeEjemplo(),
            margenes = margenesPara(412.dp),
            accion = AccionSwipe.BORRAR,
            estadoCard = EstadoCard.NORMAL,
            onAccion = {},
            onEditar = {},
            mensajeVacio = ""
        )
    }
}

@Preview(name = "Lista - medium (margen 24)", showBackground = true, backgroundColor = FONDO, widthDp = 700, heightDp = 720)
@Composable
private fun ListaMediumPreview() {
    GestorRecordatoriosTheme {
        ListaDeRecordatorios(
            titulo = "Mis Recordatorios",
            recordatorios = recordatoriosDeEjemplo(),
            margenes = margenesPara(700.dp),
            accion = AccionSwipe.BORRAR,
            estadoCard = EstadoCard.NORMAL,
            onAccion = {},
            onEditar = {},
            mensajeVacio = ""
        )
    }
}

@Preview(name = "Lista vacia", showBackground = true, backgroundColor = FONDO, widthDp = 412, heightDp = 400)
@Composable
private fun ListaVaciaPreview() {
    GestorRecordatoriosTheme {
        ListaDeRecordatorios(
            titulo = "Mis Recordatorios",
            recordatorios = emptyList(),
            margenes = margenesPara(412.dp),
            accion = AccionSwipe.BORRAR,
            estadoCard = EstadoCard.NORMAL,
            onAccion = {},
            mensajeVacio = "No tienes recordatorios.\nToca + para crear el primero."
        )
    }
}
