package com.ddam_a1.gestornotas.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestornotas.R
import com.ddam_a1.gestornotas.ui.components.DestinoNav
import com.ddam_a1.gestornotas.ui.components.NotasBottomBar
import com.ddam_a1.gestornotas.ui.components.altoBarraInferior
import com.ddam_a1.gestornotas.ui.components.NotasNavRail
import com.ddam_a1.gestornotas.ui.components.NotasNavRailAbierto
import com.ddam_a1.gestornotas.ui.theme.GestorNotasTheme

// ============================================================
//  EL ANDAMIO
//
//  Es el marco que comparten TODAS las pantallas: el fondo crema y la barra de
//  navegacion. Lo que cambia adentro es el contenido.
//
//  Existe para que Main, Papelera y Editar no repitan el mismo armazon tres
//  veces. Si manana mueves la barra o cambias el fondo, lo cambias aqui una vez.
// ============================================================

/**
 * El punto de quiebre entre "telefono" y "tablet-ish".
 *
 * 600dp es el valor que define Material (WindowSizeClass):
 *   - compact   < 600dp   -> telefono vertical      -> barra abajo
 *   - medium    600-839dp -> telefono horizontal / tablet chica -> rail
 *   - expanded  >= 840dp  -> tablet                 -> rail
 *
 * Se mide en dp, NO en pixeles, y se mide la VENTANA, no la pantalla: si el
 * usuario abre la app en media pantalla, la ventana es angosta aunque la tablet
 * sea grande, y debe verse la barra de abajo. Por eso se lee con
 * `BoxWithConstraints` y no con `LocalConfiguration`.
 */
private val BREAKPOINT_COMPACTO = 600.dp

/**
 * Marco de la app.
 *
 * @param destinoActual cual pestania se pinta como activa. Es NULO-ABLE a
 *        proposito: en la pantalla de editar no estas en ningun destino, asi
 *        que no debe haber ninguna marcada. Marcar "Bandeja" mientras editas
 *        seria mentirle al usuario sobre donde esta.
 * @param contenido lo que va dentro del marco
 */
@Composable
fun Andamio(
    destinoActual: DestinoNav?,
    onDestino: (DestinoNav) -> Unit,
    onNuevo: () -> Unit,
    railAbierto: Boolean,
    onAlternarRail: () -> Unit,
    modifier: Modifier = Modifier,
    // El boton de accion es el MISMO dibujo en todas las pantallas, pero no hace
    // lo mismo en todas: en la bandeja crea, en la vista edita, editando guarda.
    // Eso lo decide quien usa el Andamio (el NavHost), no el Andamio.
    @DrawableRes iconoAccion: Int = R.drawable.ic_add,
    descripcionAccion: String = "Nueva nota",
    contenido: @Composable (Margenes) -> Unit
) {
    // El fondo de la app. Tiene que ser el MISMO color que usa la muesca de la
    // barra inferior, porque esa muesca es un circulo de este color encima de la
    // barra. Si no coinciden, se ve el truco.
    val fondo = MaterialTheme.colorScheme.surfaceContainerHighest

    // Si el rail esta abierto NO se decide aqui: entra por parametro.
    // Tiene que vivir mas arriba (en el NavHost) porque el Andamio se vuelve a
    // crear cada vez que cambias de destino; si el estado viviera aqui, abrir el
    // menu en la bandeja e irte a la papelera lo cerraria solo. Es el mismo
    // izado de estado de siempre, dos pisos mas arriba.

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(fondo)
            // Empuja TODO por debajo de la barra de estado. La app va
            // edge-to-edge (lo activa enableEdgeToEdge en MainActivity), asi que
            // sin esto el contenido quedaria debajo del reloj y la bateria.
            // El hueco de ABAJO no se toca aqui: la barra inferior lo maneja ella
            // sola, porque necesita pintar su verde hasta el borde fisico.
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        val compacto = maxWidth < BREAKPOINT_COMPACTO

        // Los espaciados se deciden UNA vez, aqui, a partir del ancho real de la
        // ventana, y se le entregan al contenido. Asi ninguna pantalla tiene que
        // volver a medir ni puede equivocarse de breakpoint.
        val base = margenesPara(maxWidth)

        // En compact la barra va ENCIMA del contenido (asi esta en tu Figma: la
        // ultima tarjeta se ve cortada por el verde). Entonces la lista tiene que
        // reservar abajo el alto de la barra, o su ultima tarjeta quedaria
        // tapada para siempre. En rail no hay barra abajo, no hay nada que sumar.
        val margenes =
            if (compacto) base.copy(inferior = base.inferior + altoBarraInferior())
            else base

        if (compacto) {
            // ---- TELEFONO: barra FLOTANDO encima del contenido ----
            // Un Box, no un Column: en un Column la barra seria hermana del
            // contenido y lo empujaria hacia arriba. Aqui van apiladas, y el
            // contenido se dibuja de pared a pared por debajo de la barra.
            // Eso es lo que hace que las tarjetas se "metan" bajo el verde al
            // hacer scroll, como en tu diseno.
            Box(Modifier.fillMaxSize()) {
                contenido(margenes)

                NotasBottomBar(
                    destinoActual = destinoActual,
                    onDestino = onDestino,
                    onNuevo = onNuevo,
                    iconoAccion = iconoAccion,
                    descripcionAccion = descripcionAccion,
                    colorFondoPantalla = fondo,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        } else {
            // ---- MEDIUM Y TABLET: rail lateral ----
            Row(Modifier.fillMaxSize()) {
                if (railAbierto) {
                    NotasNavRailAbierto(
                        destinoActual = destinoActual,
                        onDestino = onDestino,
                        onMenu = onAlternarRail,
                        onNuevo = onNuevo,
                        iconoAccion = iconoAccion,
                        descripcionAccion = descripcionAccion
                    )
                } else {
                    NotasNavRail(
                        destinoActual = destinoActual,
                        onDestino = onDestino,
                        onMenu = onAlternarRail,
                        onNuevo = onNuevo,
                        iconoAccion = iconoAccion,
                        descripcionAccion = descripcionAccion
                    )
                }

                Box(Modifier.weight(1f)) { contenido(margenes) }
            }
        }
    }
}

// ============================================================
//  Previews de la app COMPLETA
//
//  Estos son los que mas te van a servir: no revisan un componente, revisan si
//  la pantalla entera se arma bien en cada tamanio. Cambia widthDp y ves el
//  cambio de barra abajo a rail sin tocar el emulador.
// ============================================================

@Preview(name = "App - compact (telefono)", widthDp = 412, heightDp = 892)
@Composable
private fun AndamioCompactPreview() {
    GestorNotasTheme {
        Andamio(
            destinoActual = DestinoNav.BANDEJA,
            onDestino = {}, onNuevo = {},
            railAbierto = false, onAlternarRail = {}
        ) { m ->
            Main(notasDeEjemplo(), m, onMoverAPapelera = {}, onEditar = {})
        }
    }
}

@Preview(name = "App - medium (rail cerrado)", widthDp = 840, heightDp = 720)
@Composable
private fun AndamioMediumPreview() {
    GestorNotasTheme {
        Andamio(
            destinoActual = DestinoNav.BANDEJA,
            onDestino = {}, onNuevo = {},
            railAbierto = false, onAlternarRail = {}
        ) { m ->
            Main(notasDeEjemplo(), m, onMoverAPapelera = {}, onEditar = {})
        }
    }
}

@Preview(name = "App - medium (rail abierto)", widthDp = 840, heightDp = 720)
@Composable
private fun AndamioMediumAbiertoPreview() {
    GestorNotasTheme {
        Andamio(
            destinoActual = DestinoNav.PAPELERA,
            onDestino = {}, onNuevo = {},
            railAbierto = true, onAlternarRail = {}
        ) { m ->
            Papelera(emptyList(), m, onRecuperar = {})
        }
    }
}

@Preview(name = "Editar - compact", widthDp = 412, heightDp = 892)
@Composable
private fun AndamioEditarCompactPreview() {
    GestorNotasTheme {
        Andamio(
            destinoActual = null,
            onDestino = {}, onNuevo = {},
            railAbierto = false, onAlternarRail = {}
        ) { m ->
            Editar(original = null, margenes = m, onGuardar = {}, onCancelar = {})
        }
    }
}

@Preview(name = "Editar - medium", widthDp = 840, heightDp = 720)
@Composable
private fun AndamioEditarMediumPreview() {
    GestorNotasTheme {
        Andamio(
            destinoActual = null,
            onDestino = {}, onNuevo = {},
            railAbierto = false, onAlternarRail = {}
        ) { m ->
            Editar(original = null, margenes = m, onGuardar = {}, onCancelar = {})
        }
    }
}

@Preview(name = "App - expanded (vista doble)", widthDp = 1280, heightDp = 800)
@Composable
private fun AndamioExpandedPreview() {
    GestorNotasTheme {
        Andamio(
            destinoActual = DestinoNav.BANDEJA,
            onDestino = {}, onNuevo = {},
            railAbierto = false, onAlternarRail = {}
        ) { m ->
            Main(notasDeEjemplo(), m, onMoverAPapelera = {}, onEditar = {})
        }
    }
}
