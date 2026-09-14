package com.ddam_a1.gestornotas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ddam_a1.gestornotas.R
import com.ddam_a1.gestornotas.modelClasses.Imagen
import java.io.File

// ============================================================
//  LA TIRA DE FOTOS DE UNA NOTA
//
//  Una fila horizontal de miniaturas cuadradas dentro de la tarjeta.
//
//  Es `LazyRow` y no `Row` con scroll: si una nota tiene veinte fotos, `Row`
//  cargaría las veinte aunque se vean tres. `LazyRow` sólo arma las que caben en
//  pantalla, igual que la `LazyColumn` de la bandeja.
//
//  LAS MINIATURAS NO SON LA FOTO COMPLETA
//
//  Coil lee el tamaño del hueco donde va a dibujar y decodifica la imagen a esa
//  medida, no a los 12 megapíxeles del archivo. Sin eso, ocho miniaturas de 96dp
//  se comerían la memoria de la app. Por eso no se decodifica a mano con
//  BitmapFactory: Coil ya trae esa cuenta hecha.
// ============================================================

private val MINIATURA = 96.dp
private val MINIATURA_RADIO = 12.dp
private val SEPARACION = 8.dp
private val QUITAR_TAM = 24.dp

/**
 * Las fotos de una nota.
 *
 * @param editando en edición cada foto trae su tache para quitarla; leyendo,
 *        tocarla la abre a pantalla completa. Son dos intenciones distintas y no
 *        pueden convivir en el mismo toque: si al editar tocar abriera el visor,
 *        quitar una foto sería imposible sin salirse de edición.
 * @param onAbrir tocar una foto en modo lectura
 * @param onQuitar el tache, sólo en edición
 */
@Composable
fun TiraImagenes(
    imagenes: List<Imagen>,
    editando: Boolean,
    onAbrir: (Imagen) -> Unit,
    onQuitar: (Imagen) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sin fotos no se dibuja NADA, ni un hueco vacío. Una nota sin imágenes no
    // debe pagar espacio por algo que no tiene.
    if (imagenes.isEmpty()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(SEPARACION),
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = modifier
    ) {
        // `key = { it.id }` para que al quitar una foto Compose sepa CUAL se fue
        // y no vuelva a dibujar toda la fila desde cero.
        items(imagenes, key = { it.id }) { imagen ->
            Box {
                AsyncImage(
                    model = File(imagen.ruta),
                    contentDescription = if (editando) "Imagen adjunta"
                    else "Imagen adjunta. Tócala para verla completa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(MINIATURA)
                        .clip(RoundedCornerShape(MINIATURA_RADIO))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .then(
                            if (!editando) Modifier.clickable(
                                role = Role.Button,
                                onClick = { onAbrir(imagen) }
                            ) else Modifier
                        )
                )

                if (editando) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(QUITAR_TAM)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .clickable(
                                role = Role.Button,
                                onClick = { onQuitar(imagen) }
                            )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_trash),
                            contentDescription = "Quitar imagen",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
