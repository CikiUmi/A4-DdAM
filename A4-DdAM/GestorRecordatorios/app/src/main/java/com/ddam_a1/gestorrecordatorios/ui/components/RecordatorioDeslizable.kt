package com.ddam_a1.gestorrecordatorios.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestorrecordatorios.modelClasses.Recordatorio
import com.ddam_a1.gestorrecordatorios.modelClasses.nivelPrioridad
import com.ddam_a1.gestorrecordatorios.ui.theme.GestorRecordatoriosTheme
import java.time.LocalDateTime

/**
 * Umbral: qué porcentaje del ancho hay que recorrer para que el gesto CUENTE.
 * Si sueltas antes, la tarjeta regresa sola a su lugar.
 */
private const val UMBRAL_SWIPE = 0.5f

/**
 * Una tarjeta que se puede deslizar de DERECHA a IZQ.*
 *
 * La acción entra por parámetro (`onAccion`), no está escrita aquí.
 *    El mismo componente sirve para la bandeja (mandar a papelera) y para la
 *    papelera (recuperar). El componente sabe DESLIZAR; la pantalla sabe QUÉ
 *    significa deslizar ahí.
 *
 * @param accion cuál fondo se descubre (BORRAR o RECUPERAR)
 * @param onAccion qué hacer al completar el gesto; recibe el id
 */
@Composable
fun RecordatorioDeslizable(
    recordatorio: Recordatorio,
    accion: AccionSwipe,
    onAccion: (String) -> Unit,
    modifier: Modifier = Modifier,
    estado: EstadoCard = EstadoCard.NORMAL,
    onEditar: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    // El estado del gesto: cuánto se ha corrido, hacia dónde, si ya pasó el umbral.
    // Va en un `remember` (eso hace el `rememberSwipeToDismissBoxState`) porque
    // tiene que sobrevivir a las recomposiciones que ocurren mientras arrastras.
    val estadoSwipe = rememberSwipeToDismissBoxState(
        positionalThreshold = { anchoTotal -> anchoTotal * UMBRAL_SWIPE }
    )

    SwipeToDismissBox(
        state = estadoSwipe,
        modifier = modifier,
        // para que vaya sólo de derecha a izq
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,

        // este está definido en el archivo de las cards
        backgroundContent = { FondoSwipe(accion) },

        // `onDismiss` se dispara cuando el gesto se completa.
        // una función que agarra e parámetro, recibe un string
        // con el id se hace eso de borrar o bandeja de entrada


        onDismiss = { direccion ->
            if (direccion == SwipeToDismissBoxValue.EndToStart) {
                onAccion(recordatorio.id)
            }
        }
    ) {
        CardRecordatorio(
            recordatorio = recordatorio,
            estado = estado,
            onEditar = onEditar,
            onClick = onClick
        )
    }
}

/** De dónde entra la tarjeta. Positivo = desde la derecha. */
private val DESPLAZAMIENTO_ENTRADA = 100.dp

/**
 * Envuelve cualquier cosa y la hace ENTRAR deslizándose desde la derecha.
 */

@Composable
fun AnimacionEntrada(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    //rememberSaveable porque van a estar en el LazyColumn
    // por la lógica del viewModel los recordatorios se destruyen y vuelven a crear
    // la animación se activa al añadir nuevo a la lista, entonces serían mil veces con remember
    // rememberSaveable ya sabe que ya estaba guardado :DD
    val enPreview = LocalInspectionMode.current

    // es false porque está a la derecha primero, no se ve aún...
    var visible by rememberSaveable { mutableStateOf(enPreview) }

    LaunchedEffect(Unit) { visible = true }

    // se asigna la posición y anima el cambio de 100 a 0.dp solito
    // se hace con spring, pero eso es ya de la app, se escoge y ya

    val desplazamiento by animateDpAsState(
        targetValue = if (visible) 0.dp else DESPLAZAMIENTO_ENTRADA,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "entradaDesplazamiento"
    )

    val opacidad by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "entradaOpacidad"
    )

    // se va recorriendo con la animación :D
    Box(
        modifier = modifier
            .offset(x = desplazamiento)
            .alpha(opacidad)
    ) {
        content()
    }
}

// ============================================================
//  DEMO PARA PROBAR EL GESTO
//
//  `mutableStateListOf` en vez de `listOf`. Con una lista fija, la tarjeta se
//  desliza... y se queda colgada fuera de pantalla, porque nadie la quito de
//  los datos. Asi como esta, deslizar borra el dato y la fila desaparece sola,
//  que es justo lo que va a pasar en la app al llamar `meterPapelera(id)`.
// ============================================================

private const val FONDO = 0xFFEAE2D4

private fun demo(titulo: String, prioridad: nivelPrioridad = nivelPrioridad.NULA) = Recordatorio(
    titulo = titulo,
    descripcion = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Pellentesque a nisi ultricies, faucibus risus feugiat.",
    fechaRecordatorio = LocalDateTime.now().plusDays(2),
    prioridad = prioridad
)

private fun datosDemo() = listOf(
    demo("Entregar la practica de DdAM", nivelPrioridad.ALTA),
    demo("Comprar cafe"),
    demo("Llamar a mama", nivelPrioridad.BAJA),
    demo("Revisar el Figma del equipo", nivelPrioridad.MEDIA)
)

/**
 * Maqueta de la bandeja, solo para probar el gesto.
 * Hace de mentiritas lo que el ViewModel hara de verdad:
 * guardar la lista y quitarle cosas.
 */
@Composable
fun DemoDeslizable(modifier: Modifier = Modifier) {
    val datos = remember { mutableStateListOf(*datosDemo().toTypedArray()) }

    if (datos.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TextButton(onClick = { datos.addAll(datosDemo()) }) {
                Text("Ya no queda nada. Reiniciar demo")
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // El `key` es obligatorio en el Lazy: porque si no Compose identifica las
        // filas por su posicion, y al borrar la primera mueve a todas en vez de a
        // una. Resultado tipico: se anima la tarjeta equivocada.
        items(datos, key = { it.id }) { recordatorio ->
            AnimacionEntrada {
                RecordatorioDeslizable(
                    recordatorio = recordatorio,
                    accion = AccionSwipe.BORRAR,
                    onAccion = { id -> datos.removeAll { it.id == id } }
                )
            }
        }
    }
}

@Preview(name = "Deslizar para eliminar", showBackground = true, backgroundColor = FONDO, heightDp = 720)
@Composable
private fun DemoDeslizablePreview() {
    GestorRecordatoriosTheme { DemoDeslizable() }
}

@Preview(name = "Deslizar para recuperar", showBackground = true, backgroundColor = FONDO, heightDp = 300)
@Composable
private fun DemoRecuperarPreview() {
    GestorRecordatoriosTheme {
        val datos = remember { mutableStateListOf(demo("Recordatorio tirado")) }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(datos, key = { it.id }) { r ->
                RecordatorioDeslizable(
                    recordatorio = r,
                    accion = AccionSwipe.RECUPERAR,
                    estado = EstadoCard.EN_PAPELERA,
                    onAccion = { id -> datos.removeAll { it.id == id } }
                )
            }
        }
    }
}
