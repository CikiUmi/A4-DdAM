package com.ddam_a1.gestornotas.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import java.io.File
import kotlin.math.abs

// ============================================================
//  EL VISOR DE FOTOS  (punto del pellizco)
//
//  Pantalla completa, negra, con UNA foto. Dos gestos:
//
//    PELLIZCO   dos dedos que se abren o se cierran -> zoom de 1x a 5x.
//               Con zoom, un dedo arrastra la foto para ver los rincones.
//    ARRASTRE   sin zoom, jalar hacia abajo la despide. Es el gesto que ya usan
//               todas las galerías, así que nadie tiene que aprenderlo.
//
//  POR QUE NO SE USA detectTransformGestures
//
//  `detectTransformGestures` es el atajo normal de Compose para pellizco, y
//  funciona bien... pero sólo avisa MIENTRAS te mueves. Nunca avisa cuándo
//  SOLTASTE. Y para "arrastra hacia abajo para cerrar" eso es justo lo que hace
//  falta: si jalas 40 píxeles y te arrepientes, al soltar la foto debe regresar
//  a su lugar. Sin el aviso de soltar, se quedaría chueca para siempre.
//
//  Así que aquí está escrito a mano con `awaitEachGesture`, que es exactamente
//  lo que `detectTransformGestures` hace por dentro, más la línea de después del
//  ciclo — que corre cuando ya no queda ningún dedo en la pantalla.
// ============================================================

/** Hasta dónde deja acercarse el pellizco. */
private const val ZOOM_MINIMO = 1f
private const val ZOOM_MAXIMO = 5f

/**
 * Cuánto hay que jalar hacia abajo para que se cierre, en píxeles.
 *
 * Es un número en crudo y no `.dp` porque los gestos hablan en píxeles: lo que
 * `pan.y` entrega ya viene en las unidades de la pantalla real.
 */
private const val UMBRAL_CIERRE = 300f

/**
 * La foto a pantalla completa.
 *
 * @param ruta dónde está el archivo copiado
 * @param onCerrar se llama al arrastrar hacia abajo, o con el botón de atrás
 *        (de eso se encarga el `onDismissRequest` del Dialog). Tocar NO cierra:
 *        el Box tapa toda la pantalla, así que no hay "fuera" que tocar, y un
 *        toque suelto sería fácil de disparar sin querer mientras haces zoom.
 */
@Composable
fun VisorImagen(
    ruta: String,
    onCerrar: () -> Unit
) {
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(
            // Sin esto el diálogo se queda del ancho de una alerta. Con esto
            // ocupa la pantalla completa, que es lo que pide un visor de fotos.
            usePlatformDefaultWidth = false
        )
    ) {
        // ---- El estado del gesto ----
        // `mutableFloatStateOf` y no `mutableStateOf` para los números: guarda un
        // float pelón en vez de meterlo en una caja de objeto. Con un valor que
        // cambia en cada fotograma del pellizco, esa caja se nota.
        var escala by remember { mutableFloatStateOf(1f) }
        var desplazamiento by remember { mutableStateOf(Offset.Zero) }
        var arrastreCierre by remember { mutableFloatStateOf(0f) }

        // El regreso a su sitio cuando sueltas sin llegar al umbral.
        //
        // OJO CON EL RESORTE. Este mismo valor animado es el que mueve la foto
        // MIENTRAS jalas, asi que con el resorte suave de fabrica la imagen iba
        // siempre unos pixeles atras del dedo y se sentia blandita. Con
        // `StiffnessHigh` alcanza al dedo (se ve 1 a 1) y aun asi el regreso al
        // soltar es un movimiento y no un brinco seco.
        //
        // `DampingRatioNoBouncy` porque una foto que rebota al volver a su sitio
        // parece de juguete.
        val arrastreAnimado by animateFloatAsState(
            targetValue = arrastreCierre,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            ),
            label = "arrastre"
        )

        // El fondo se va aclarando conforme jalas: es la pista de que soltar va a
        // cerrar. 1f = negro completo, 0f = transparente.
        val opacidadFondo = (1f - abs(arrastreAnimado) / (UMBRAL_CIERRE * 2f)).coerceIn(0f, 1f)

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = opacidadFondo))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        // 1. Espera el primer dedo.
                        awaitFirstDown(requireUnconsumed = false)

                        // 2. Mientras haya dedos, lee cada evento.
                        var cambios: List<PointerInputChange>
                        do {
                            val evento = awaitPointerEvent()
                            cambios = evento.changes

                            val zoom = evento.calculateZoom()   // 1f = no cambió
                            val pan = evento.calculatePan()     // cuánto se movió

                            if (escala > ZOOM_MINIMO || zoom != 1f) {
                                // Con la foto acercada, el dedo la PASEA.
                                escala = (escala * zoom).coerceIn(ZOOM_MINIMO, ZOOM_MAXIMO)

                                // PERO NO HASTA PERDERLA. Sin este limite, a 5x
                                // podias arrastrar la foto entera fuera de la
                                // pantalla y quedarte viendo negro, sin forma de
                                // traerla de vuelta mas que cerrando.
                                //
                                // Cuanto sobra para pasear = lo que crecio la
                                // foto, repartido entre los dos lados. A 1x sobra
                                // cero, y el limite se vuelve "no te muevas".
                                // `size` es el tamaño real de la caja en pixeles,
                                // y lo da el propio PointerInputScope.
                                val sobraX = size.width * (escala - 1f) / 2f
                                val sobraY = size.height * (escala - 1f) / 2f
                                desplazamiento = Offset(
                                    (desplazamiento.x + pan.x).coerceIn(-sobraX, sobraX),
                                    (desplazamiento.y + pan.y).coerceIn(-sobraY, sobraY)
                                )

                                // Al volver a 1x se recentra: si no, la foto se
                                // quedaría fuera de cuadro y no habría forma de
                                // traerla de vuelta.
                                if (escala == ZOOM_MINIMO) desplazamiento = Offset.Zero
                            } else {
                                // Sin zoom, el dedo sirve para despedir la foto.
                                // `coerceAtLeast(0f)` la deja jalar sólo hacia
                                // abajo; hacia arriba no hace nada.
                                arrastreCierre = (arrastreCierre + pan.y).coerceAtLeast(0f)
                            }

                            // Marcar los eventos como consumidos evita que algo
                            // de más atrás (el scroll de la pantalla) también los
                            // agarre y las dos cosas se peleen.
                            cambios.forEach { if (it.positionChanged()) it.consume() }
                        } while (cambios.any { it.pressed })

                        // 3. Ya no hay dedos: aquí se decide.
                        if (arrastreCierre > UMBRAL_CIERRE) onCerrar() else arrastreCierre = 0f
                    }
                }
        ) {
            AsyncImage(
                model = File(ruta),
                contentDescription = "Imagen de la nota, en pantalla completa",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    // `graphicsLayer` mueve y escala la foto SIN volver a medir
                    // ni acomodar nada: sólo cambia cómo se pinta. Por eso el
                    // pellizco se siente fluido aunque cambie 60 veces por
                    // segundo. Hacerlo con `padding` o `size` recalcularía el
                    // layout completo en cada fotograma.
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        translationX = desplazamiento.x
                        translationY = desplazamiento.y + arrastreAnimado
                    }
            )
        }
    }
}
