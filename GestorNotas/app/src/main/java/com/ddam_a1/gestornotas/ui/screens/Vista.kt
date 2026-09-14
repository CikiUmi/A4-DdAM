package com.ddam_a1.gestornotas.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestornotas.modelClasses.Nota

// ============================================================
//  LA VISTA DE UNA NOTA  (todavia vacia)
//
//  Aqui va a vivir el contenido de la nota: el texto, los stickers, el pellizco.
//  Por ahora solo confirma que la navegacion llego bien.
//
//  Recibe la nota YA RESUELTA, no un id. El NavHost es quien hace `vm.leer(id)`
//  y decide: si existe te la pasa, si no existe llega null y esto es una nota
//  nueva. La misma logica de Editar, y por la misma razon: la pantalla no
//  conoce al ViewModel, solo recibe datos.
// ============================================================

/**
 * @param nota la nota a mostrar, o null si es una nota nueva
 * @param onCerrar volver atras
 */
@Composable
fun Vista(
    nota: Nota?,
    margenes: Margenes,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = margenes.lateral),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (nota == null) "Nota nueva\n(pantalla en construccion)"
            else "${nota.titulo}\n(pantalla en construccion)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}
