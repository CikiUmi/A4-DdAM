package com.ddam_a1.gestorrecordatorios.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestorrecordatorios.R
import com.ddam_a1.gestorrecordatorios.ui.theme.GestorRecordatoriosTheme

// ============================================================
//  MEDIDAS del rail abierto
//
//  En Figma la variante "Barra Lateral AbIerta / deskOpen" mide 444 de ancho.
//  Aquí son 280, que es la proporción equivalente en un dispositivo real y deja
//  sitio para el contenido.
//
//  El alto y el radio de cada fila son los MISMOS del rail cerrado (72 y 18),
//  importados de NavBar.kt: al abrir y cerrar el menú las piezas deben sentirse
//  las mismas, no dos componentes parecidos.
// ============================================================

private val RAIL_ABIERTO_ANCHO = 280.dp
private val PADDING_ARRIBA = 32.dp
private val PADDING_ABAJO = 32.dp
private val PADDING_LATERAL = 12.dp

/**
 * Una fila del menú lateral abierto: icono + texto.
 * Corresponde a tu componente "Etiqueta" del Figma.
 *
 * DECISIONES IMPORTANTES
 *
 * 1. El fondo solo aparece cuando `seleccionado` es true, y usa el mismo beige
 *    transparente del rail cerrado: `primaryContainer` al 70% sobre el verde,
 *    que da exactamente el #C2B283 de tu Figma.
 *
 * 2. El `contentDescription` del icono va en null A PROPÓSITO: el texto de al lado
 *    ya dice lo mismo, y si los dos hablaran, TalkBack leería la etiqueta dos veces.
 *    En el rail cerrado, donde NO hay texto, el icono sí lleva descripción.
 *
 * 3. Alto de 72 igual que `ItemRail`, así los iconos quedan a la misma altura
 *    cuando el menú se abre. Si cambiaran de posición, la persona tendría que
 *    volver a buscarlos.
 */
@Composable
fun EtiquetaNav(
    @DrawableRes icono: Int,
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(ITEM_TAM)
            .clip(RoundedCornerShape(ITEM_RADIO))
            .background(
                // Mismo indicador que el rail cerrado: primaryContainer al 70%,
                // que es el beige #C2B283 de tu Figma.
                if (seleccionado)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)
                else Color.Transparent
            )
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 20.dp)
    ) {
        val colorContenido =
            if (seleccionado) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onTertiary

        Icon(
            painter = painterResource(icono),
            contentDescription = null,   // lo dice el texto de al lado
            tint = colorContenido,
            modifier = Modifier.size(ICONO_TAM)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = colorContenido
        )
    }
}

/**
 * El rail lateral ABIERTO. Corresponde a la variante "Barra Lateral AbIerta".
 *
 * Mismo orden que el rail cerrado: control arriba, destinos en medio, y la acción
 * de "nuevo recordatorio" hasta abajo.
 */
@Composable
fun RecordatoriosNavRailAbierto(
    destinoActual: DestinoNav?,
    onDestino: (DestinoNav) -> Unit,
    onMenu: () -> Unit,
    onNuevo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(RAIL_ABIERTO_ANCHO)
            .shadow(8.dp)
            .background(MaterialTheme.colorScheme.onTertiaryContainer)
            .padding(horizontal = PADDING_LATERAL)
            .padding(top = PADDING_ARRIBA, bottom = PADDING_ABAJO)
    ) {
        // ---- El control ----
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(ITEM_TAM)
                .clip(RoundedCornerShape(ITEM_RADIO))
                .clickable(onClick = onMenu)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = "Cerrar menú",
                tint = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.size(ICONO_TAM)
            )
        }

        Spacer(Modifier.height(24.dp))

        // ---- Los destinos ----
        // Recorremos el enum en vez de escribirlos a mano: una sola lista
        // de destinos para las tres barras de la app.
        DestinoNav.entries.forEachIndexed { indice, destino ->
            if (indice > 0) Spacer(Modifier.height(24.dp))
            EtiquetaNav(
                icono = iconoDe(destino),
                texto = etiquetaDe(destino),
                seleccionado = destino == destinoActual,
                onClick = { onDestino(destino) }
            )
        }

        Spacer(Modifier.weight(1f))

        // ---- La acción, hasta abajo (igual que en el rail cerrado) ----
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(ITEM_TAM)
                .clip(RoundedCornerShape(ITEM_RADIO))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onNuevo)
                .padding(horizontal = 20.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(ICONO_TAM)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = "Nuevo recordatorio",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

// ============================================================
//  Previews
// ============================================================

@Preview(name = "Etiquetas · los dos estados", showBackground = true, backgroundColor = 0xFF354D2E, widthDp = 280)
@Composable
private fun EtiquetasPreview() {
    GestorRecordatoriosTheme {
        Column(Modifier.padding(12.dp)) {
            EtiquetaNav(R.drawable.ic_inbox, "Bandeja", seleccionado = true, onClick = {})
            Spacer(Modifier.height(24.dp))
            EtiquetaNav(R.drawable.ic_trash, "Papelera", seleccionado = false, onClick = {})
        }
    }
}

@Preview(name = "Rail abierto · bandeja", showBackground = true, backgroundColor = 0xFFEAE2D4, heightDp = 640)
@Composable
private fun RailAbiertoPreview() {
    GestorRecordatoriosTheme {
        RecordatoriosNavRailAbierto(DestinoNav.BANDEJA, {}, {}, {})
    }
}
