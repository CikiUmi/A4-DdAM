package com.ddam_a1.gestornotas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestornotas.modelClasses.Nota
import com.ddam_a1.gestornotas.ui.components.ELEVACION_TARJETA
import com.ddam_a1.gestornotas.ui.components.EstadoCard
import com.ddam_a1.gestornotas.ui.components.PADDING_TARJETA
import com.ddam_a1.gestornotas.ui.components.RADIO_TARJETA
import com.ddam_a1.gestornotas.ui.components.SEPARACION_TITULO_DESCRIPCION
import com.ddam_a1.gestornotas.ui.components.colorFondoDe
import com.ddam_a1.gestornotas.ui.components.colorSecundarioDe
import com.ddam_a1.gestornotas.ui.components.colorTextoDe
import com.ddam_a1.gestornotas.ui.components.formatoLargo
import com.ddam_a1.gestornotas.ui.components.hace
import com.ddam_a1.gestornotas.ui.theme.GestorNotasTheme

// ============================================================
//  EL PANEL DE DETALLE  (solo tablet)
//
//  Es la tarjeta de la derecha de tu Figma: la misma forma, el mismo radio, la
//  misma sombra y el mismo padding que las de la lista, pero SIEMPRE desplegada
//  y sin flecha. Porque aqui el desplegado no es un estado que se alterna: este
//  panel existe justamente para mostrarlo todo.
//
//  Reutiliza las constantes y los ayudantes de fecha de CardNota.kt en
//  vez de copiarlos. Si manana cambias el radio de las tarjetas, este panel
//  cambia con ellas y no se queda desalineado.
// ============================================================

/**
 * @param nota el que esta seleccionado, o null si no hay ninguno
 */
@Composable
fun PanelDetalle(
    nota: Nota?,
    onEditar: (String) -> Unit,
    onMoverAPapelera: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (nota == null) {
        // Mismo criterio que la lista vacia: un panel en blanco no dice nada.
        Box(
            modifier = modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Selecciona una nota\npara ver el detalle",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val colorTexto = colorTextoDe(EstadoCard.NORMAL)
    val colorSecundario = colorSecundarioDe(EstadoCard.NORMAL)

    Card(
        shape = RoundedCornerShape(RADIO_TARJETA),
        colors = CardDefaults.cardColors(containerColor = colorFondoDe(EstadoCard.NORMAL)),
        elevation = CardDefaults.cardElevation(defaultElevation = ELEVACION_TARJETA),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(PADDING_TARJETA)) {

            Text(
                text = hace(nota.fechaNota),
                style = MaterialTheme.typography.labelSmall,
                color = colorSecundario,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Text(
                text = nota.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = colorTexto
            )

            Spacer(Modifier.height(SEPARACION_TITULO_DESCRIPCION))

            // La descripcion COMPLETA, sin maxLines ni ellipsis. Esa es toda la
            // razon de ser de este panel. Si es larguisima, quien hace scroll es
            // el panel entero (lo envuelve Main), no este texto: asi los botones
            // se van con el contenido en vez de quedarse flotando.
            Text(
                text = nota.contenido,
                style = MaterialTheme.typography.bodyMedium,
                color = colorSecundario
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Creada el ${formatoLargo(nota.fechaNota)}",
                style = MaterialTheme.typography.labelMedium,
                color = colorTexto
            )

            Spacer(Modifier.height(8.dp))

            // Las acciones viven AQUI y no en las tarjetas de la lista: en la
            // vista doble la lista sirve para escoger, y el panel para actuar.
            // Separar escoger de actuar es lo que hace que no borres cosas sin
            // querer mientras navegas.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onMoverAPapelera(nota.id) }) {
                    Text("Mover a la papelera", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = { onEditar(nota.id) }) {
                    Text("Editar", color = colorTexto)
                }
            }
        }
    }
}

private const val FONDO = 0xFFEAE2D4

@Preview(name = "Detalle con nota", showBackground = true, backgroundColor = FONDO, widthDp = 456, heightDp = 400)
@Composable
private fun PanelDetallePreview() {
    GestorNotasTheme {
        PanelDetalle(notasDeEjemplo().first(), onEditar = {}, onMoverAPapelera = {})
    }
}

@Preview(name = "Detalle sin seleccion", showBackground = true, backgroundColor = FONDO, widthDp = 456, heightDp = 300)
@Composable
private fun PanelDetalleVacioPreview() {
    GestorNotasTheme {
        PanelDetalle(null, onEditar = {}, onMoverAPapelera = {})
    }
}
