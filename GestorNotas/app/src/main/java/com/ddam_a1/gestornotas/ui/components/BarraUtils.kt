package com.ddam_a1.gestornotas.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestornotas.R
import com.ddam_a1.gestornotas.ui.theme.GestorNotasTheme
import com.ddam_a1.gestornotas.ui.theme.coloresExtra

// ============================================================
//  LA BARRA DE UTILIDADES  (componente "Barra Utils" del Figma)
//
//  La pildora que aparece abajo del texto CUANDO estas editando. Vive dentro de
//  la tarjeta, no en la barra de navegacion, porque son acciones sobre EL TEXTO,
//  no sobre la app.
//
//  Hoy trae UNA sola cosa: adjuntar una foto, y esa si funciona. Los stickers
//  salieron de aqui hasta que existan de verdad.
//
//  MEDIDAS DEL FIGMA
//    pildora ....... radio 32, padding 10 (el ancho ya no es fijo, ver abajo)
//    iconos ........ 30
//    sombra ........ 0 0 6 rgba(84,69,42,0.4)  -> en Compose, elevacion
// ============================================================

/**
 * Separacion entre iconos. La pildora ya NO mide 158 fijos: con un solo icono
 * quedaria un ovalo medio vacio. Ahora se ajusta a lo que tenga adentro, asi que
 * crece sola el dia que vuelvan los stickers.
 */
private val PILDORA_SEPARACION = 18.dp
private val PILDORA_RADIO = 32.dp
private val PILDORA_PADDING = 10.dp
private val ICONO_UTIL = 30.dp

/** Opacidad del fondo de la pildora. En el Figma es `-60a`. */
private const val ALPHA_PILDORA = 0.60f

/** Diametro del boton redondo de accion (guardar / editar) que va a su derecha. */
internal val BOTON_ACCION_TAM = 50.dp

/**
 * Una utilidad de la barra.
 *
 * POR AHORA SOLO HAY UNA. Los stickers estan fuera hasta que existan de verdad:
 * un boton que no hace nada no informa, estorba. Cuando toque implementarlos,
 * volver a ponerlos es agregar UNA linea a este enum — la barra se re-dibuja
 * sola porque recorre `Utilidad.entries`, no una lista escrita a mano.
 *
 * Y el emoji no lleva boton a proposito: el teclado del sistema YA trae emojis,
 * asi que escribirlos en la nota funciona hoy sin una sola linea de codigo. Un
 * boton propio seria un segundo camino para lo mismo, mas confuso y peor (el
 * teclado tiene buscador, recientes y tono de piel).
 *
 * Los drawables ic_sticker e ic_reaction se quedan en res/drawable esperando.
 */
enum class Utilidad(@DrawableRes val icono: Int, val descripcion: String) {
    FOTO(R.drawable.ic_add_photo, "Agregar imagen")
}

/**
 * La pildora de utilidades.
 *
 * @param onUtilidad que hacer cuando tocan una. Recibe CUAL fue, para que la
 *        pantalla decida; la barra no sabe de fotos ni de stickers.
 */
@Composable
fun BarraUtils(
    onUtilidad: (Utilidad) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PILDORA_SEPARACION),
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(PILDORA_RADIO))
            .clip(RoundedCornerShape(PILDORA_RADIO))
            // #D8C2BE al 60%. En el Figma la variable se llama
            // `onSurfaceVariantDark`, pero ese hex en modo CLARO es
            // `outlineVariant`. Se usa el rol, no el nombre de alla.
            .background(
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = ALPHA_PILDORA)
            )
            .padding(PILDORA_PADDING)
    ) {
        Utilidad.entries.forEach { util ->
            IconButton(
                onClick = { onUtilidad(util) },
                modifier = Modifier.size(ICONO_UTIL)
            ) {
                Icon(
                    painter = painterResource(util.icono),
                    contentDescription = util.descripcion,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(ICONO_UTIL)
                )
            }
        }
    }
}

/**
 * El boton redondo de guardar / editar que va DENTRO de la tarjeta.
 *
 * Solo aparece en medium y expanded. En telefono el mismo boton vive en la
 * muesca de la barra de abajo, y asi esta en tu Figma: en compact el hueco de la
 * derecha de la barra de utils esta vacio.
 *
 * O sea: el boton no se duplica, se MUEVE. Si estuviera en los dos lados a la
 * vez, en tablet habria dos palomitas haciendo lo mismo.
 */
@Composable
fun BotonAccionTarjeta(
    @DrawableRes icono: Int,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(BOTON_ACCION_TAM)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            // #FFDAD5. En el Figma se llama `tertiaryContainerLight`; en tu tema
            // ese hex es `primaryContainer`. Otro cruzado.
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(icono),
                contentDescription = descripcion,
                tint = MaterialTheme.coloresExtra.correct.color,
                modifier = Modifier.size(ICONO_UTIL)
            )
        }
    }
}

// ============================================================
//  Previews
// ============================================================

private const val FONDO_TARJETA = 0xFFFFF8F7

@Preview(name = "Barra de utilidades", showBackground = true, backgroundColor = FONDO_TARJETA)
@Composable
private fun BarraUtilsPreview() {
    GestorNotasTheme {
        Box(Modifier.padding(16.dp)) { BarraUtils(onUtilidad = {}) }
    }
}

@Preview(name = "Boton guardar", showBackground = true, backgroundColor = FONDO_TARJETA)
@Composable
private fun BotonGuardarPreview() {
    GestorNotasTheme {
        Box(Modifier.padding(16.dp)) {
            BotonAccionTarjeta(R.drawable.ic_check, "Guardar nota", onClick = {})
        }
    }
}

@Preview(name = "Fila completa (medium)", showBackground = true, backgroundColor = FONDO_TARJETA, widthDp = 420)
@Composable
private fun FilaUtilsPreview() {
    GestorNotasTheme {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(10.dp)
        ) {
            BarraUtils(onUtilidad = {})
            BotonAccionTarjeta(R.drawable.ic_check, "Guardar nota", onClick = {})
        }
    }
}
