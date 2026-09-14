package com.ddam_a1.gestornotas.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestornotas.modelClasses.Nota

// ============================================================
//  LA VISTA DE UNA NOTA
//
//  Aqui vive el contenido de la nota: el texto, los stickers, el pellizco.
//
//  El pellizco (pinch-to-zoom) se hace con `detectTransformGestures`, el
//  equivalente en Compose al `ScaleGestureDetector` de las Views clasicas.
//  Detecta a la vez: escala (pellizcar), desplazamiento (arrastrar con dos
//  dedos) y rotacion (que aqui se ignora, no la necesitamos).
//
//  El estado de zoom (escala + desplazamiento) vive en este composable con
//  `remember`, no en el ViewModel: es puramente visual, no es un dato de la
//  nota. Si el usuario sale y vuelve a entrar, es razonable que el zoom se
//  reinicie, igual que pasaria con cualquier vista de imagenes.
//
//  Recibe la nota YA RESUELTA, no un id. El NavHost es quien hace `vm.leer(id)`
//  y decide: si existe te la pasa, si no existe llega null y esto es una nota
//  nueva. La misma logica de Editar, y por la misma razon: la pantalla no
//  conoce al ViewModel, solo recibe datos.
// ============================================================

private const val ESCALA_MINIMA = 1f
private const val ESCALA_MAXIMA = 4f

/**
 * @param nota la nota a mostrar, o null si es una nota nueva
 * @param onCerrar volver atrassssss
 */
@Composable
fun Vista(
    nota: Nota?,
    margenes: Margenes,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ---- Estado del zoom ----
    // `escala`: cuanto se agranda el contenido (1f = tamano normal).
    // `desplazamiento`: cuanto se movio el contenido al arrastrar con dos
    // dedos, para poder recorrer la nota una vez que esta agrandada.
    var escala by remember { mutableFloatStateOf(1f) }
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = margenes.lateral)
            // El detector de gestos va en el contenedor completo: asi el
            // usuario puede pellizcar en cualquier parte de la pantalla, no
            // solo encima del texto.
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Se multiplica, no se reemplaza: el gesto entrega cuanto
                    // CAMBIO la escala desde el ultimo evento, no la escala
                    // final. coerceIn evita que el usuario achique el texto
                    // hasta desaparecer o lo agrande hasta lo ilegible.
                    val nuevaEscala = (escala * zoom).coerceIn(ESCALA_MINIMA, ESCALA_MAXIMA)

                    // Si ya estamos en el minimo, no tiene sentido dejar
                    // desplazamiento acumulado: se veria descentrado sin haber
                    // hecho zoom. Se resetea junto con la escala.
                    desplazamiento = if (nuevaEscala == ESCALA_MINIMA) {
                        Offset.Zero
                    } else {
                        desplazamiento + pan
                    }
                    escala = nuevaEscala
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (nota == null) "Nota nueva\n(pantalla en construccion)"
            else "${nota.titulo}\n\n${nota.contenido}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(24.dp)
                // `graphicsLayer` transforma solo el DIBUJO, no el layout: es
                // barato (no recalcula medidas en cada frame) y es donde debe
                // vivir un zoom que cambia constantemente mientras el usuario
                // mueve los dedos.
                .graphicsLayer(
                    scaleX = escala,
                    scaleY = escala,
                    translationX = desplazamiento.x,
                    translationY = desplazamiento.y
                )
        )
    }
}