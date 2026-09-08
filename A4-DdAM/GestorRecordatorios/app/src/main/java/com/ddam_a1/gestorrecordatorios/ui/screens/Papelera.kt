package com.ddam_a1.gestorrecordatorios.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestorrecordatorios.modelClasses.Recordatorio
import com.ddam_a1.gestorrecordatorios.ui.components.AccionSwipe
import com.ddam_a1.gestorrecordatorios.ui.components.EstadoCard
import com.ddam_a1.gestorrecordatorios.ui.theme.GestorRecordatoriosTheme
import java.time.LocalDateTime

/**
 * La papelera.
 *
 * Es la MISMA lista de la bandeja con tres cambios:
 *   - los datos vienen de ListaPapelera
 *   - el fondo del swipe es verde y dice "recuperar"
 *   - las tarjetas van en estado EN_PAPELERA (beige apagado + la fecha en rojo)
 *
 * Y NO pasa `onEditar`: no tiene sentido editar algo que ya tiraste. Primero lo
 * recuperas. Como el parametro es opcional, la tarjeta simplemente no dibuja el
 * boton.
 */
@Composable
fun Papelera(
    recordatorios: List<Recordatorio>,
    margenes: Margenes,
    onRecuperar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ListaDeRecordatorios(
        titulo = "Mis Recordatorios",
        recordatorios = recordatorios,
        margenes = margenes,
        accion = AccionSwipe.RECUPERAR,
        estadoCard = EstadoCard.EN_PAPELERA,
        onAccion = onRecuperar,
        mensajeVacio = "La papelera esta vacia.\nLo que elimines vive aqui 5 dias.",
        modifier = modifier
    )
}

private const val FONDO = 0xFFEAE2D4

@Preview(name = "Papelera", showBackground = true, backgroundColor = FONDO, widthDp = 412, heightDp = 720)
@Composable
private fun PapeleraPreview() {
    GestorRecordatoriosTheme {
        Papelera(
            recordatoriosDeEjemplo().map {
                it.copy(enPapelera = true, fechaEliminado = LocalDateTime.now())
            },
            margenesPara(412.dp),
            onRecuperar = {}
        )
    }
}
