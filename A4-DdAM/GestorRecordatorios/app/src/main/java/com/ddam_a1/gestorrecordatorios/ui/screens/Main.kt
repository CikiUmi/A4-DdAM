package com.ddam_a1.gestorrecordatorios.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestorrecordatorios.modelClasses.Recordatorio
import com.ddam_a1.gestorrecordatorios.ui.components.AccionSwipe
import com.ddam_a1.gestorrecordatorios.ui.components.EstadoCard
import com.ddam_a1.gestorrecordatorios.ui.theme.GestorRecordatoriosTheme

/**
 * La bandeja de entrada.
 *
 * Tiene DOS layouts, no uno estirado:
 *
 *   compact / medium  ->  solo la lista. Tocar una tarjeta la despliega ahi mismo.
 *   expanded          ->  lista + panel de detalle. Tocar una tarjeta la SELECCIONA
 *                         y el detalle aparece al lado.
 *
 * Esa es la diferencia entre responsive y adaptive: no es la misma interfaz mas
 * ancha, es otra interfaz que aprovecha el espacio. Y fijate que el DATO es el
 * mismo; lo unico que cambia es que en tablet caben las dos cosas a la vez, asi
 * que no hace falta navegar para ver el detalle.
 *
 * Sigue sin recibir el ViewModel: lista, margenes y funciones. Nada mas.
 */
@Composable
fun Main(
    recordatorios: List<Recordatorio>,
    margenes: Margenes,
    onMoverAPapelera: (String) -> Unit,
    onEditar: (String) -> Unit,
    modifier: Modifier = Modifier,
    seleccionadoId: String? = null,
    onSeleccionar: (String) -> Unit = {}
) {
    val vistaDoble = margenes.tamano == TamanoVentana.EXPANDED

    if (!vistaDoble) {
        ListaDeRecordatorios(
            titulo = "Mis Recordatorios",
            recordatorios = recordatorios,
            margenes = margenes,
            accion = AccionSwipe.BORRAR,
            estadoCard = EstadoCard.NORMAL,
            onAccion = onMoverAPapelera,
            onEditar = onEditar,
            mensajeVacio = "No tienes recordatorios.\nToca + para crear el primero.",
            modifier = modifier
        )
        return
    }

    // ---- TABLET: dos paneles ----
    //
    // Cual se muestra en el detalle: el seleccionado si sigue existiendo, y si
    // no, el primero de la lista.
    //
    // El `?:` cubre dos casos de golpe: que todavia no hayas escogido ninguno
    // (recien abres la app) y que el que tenias escogido ya no este (lo mandaste
    // a la papelera desde el propio panel). En los dos, el panel se queda
    // mostrando algo util en vez de vaciarse. Y no hace falta ningun efecto ni
    // tocar el estado: se CALCULA en cada recomposicion.
    val actual = recordatorios.firstOrNull { it.id == seleccionadoId }
        ?: recordatorios.firstOrNull()

    Column(modifier.fillMaxSize()) {

        TituloPantalla("Mis Recordatorios", margenes)

        Row(Modifier.weight(1f).fillMaxWidth()) {

            ListaDeRecordatorios(
                titulo = "",                 // el titulo ya se dibujo arriba
                mostrarTitulo = false,
                recordatorios = recordatorios,
                margenes = margenes,
                accion = AccionSwipe.BORRAR,
                estadoCard = EstadoCard.NORMAL,
                onAccion = onMoverAPapelera,
                seleccionadoId = actual?.id,
                onSeleccionar = onSeleccionar,
                mensajeVacio = "No tienes recordatorios.\nToca + para crear el primero.",
                // Pesos y no anchos fijos: en una tablet mas grande, o con la app
                // en media pantalla, 548 fijos dejarian un hueco a la derecha.
                modifier = Modifier.weight(PESO_LISTA)
            )

            Spacer(Modifier.width(HUECO_PANELES))

            Column(
                modifier = Modifier
                    .weight(PESO_DETALLE)
                    // Quien hace scroll es el panel entero, para que una
                    // descripcion larguisima no deje los botones inalcanzables.
                    .verticalScroll(rememberScrollState())
                    .padding(end = margenes.lateral, bottom = margenes.inferior)
            ) {
                PanelDetalle(
                    recordatorio = actual,
                    onEditar = onEditar,
                    onMoverAPapelera = onMoverAPapelera,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private const val FONDO = 0xFFEAE2D4

@Preview(name = "Bandeja - telefono", showBackground = true, backgroundColor = FONDO, widthDp = 412, heightDp = 720)
@Composable
private fun MainCompactPreview() {
    GestorRecordatoriosTheme {
        Main(recordatoriosDeEjemplo(), margenesPara(412.dp), onMoverAPapelera = {}, onEditar = {})
    }
}

@Preview(name = "Bandeja - tablet (vista doble)", showBackground = true, backgroundColor = FONDO, widthDp = 1100, heightDp = 700)
@Composable
private fun MainExpandedPreview() {
    GestorRecordatoriosTheme {
        Main(
            recordatoriosDeEjemplo(),
            margenesPara(1100.dp),
            onMoverAPapelera = {},
            onEditar = {}
            // sin seleccionadoId: el panel cae solo en el primero de la lista
        )
    }
}

@Preview(name = "Bandeja - tablet vacia", showBackground = true, backgroundColor = FONDO, widthDp = 1100, heightDp = 500)
@Composable
private fun MainExpandedVaciaPreview() {
    GestorRecordatoriosTheme {
        Main(emptyList(), margenesPara(1100.dp), onMoverAPapelera = {}, onEditar = {})
    }
}
