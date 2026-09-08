package com.ddam_a1.gestorrecordatorios.ui.screens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================
//  LOS ESPACIADOS, MEDIDOS SOBRE EL FIGMA
//
//  Todo lo de abajo salio de la pagina "cels", midiendo las posiciones reales:
//
//  COMPACT  (Android Compact, 412 x 869)
//    titulo TITLEAPP ........ x=20  y=44   alto 48
//    lista  Frame 3 ......... x=20  y=108  ancho 372   -> 412-20-372 = 20 derecha
//    tarjetas ............... 0, 153.9, 316.9, 470.9, 624.8  -> hueco 12
//
//  MEDIUM  (Android Medium, 700 x 840)
//    rail ................... ancho 134 (el componente mide 124, ver nota)
//    titulo ................. x=32  y=40
//    lista .................. x=32  y=112 ancho 502   -> 566-32-502 = 32 derecha
//    tarjetas ............... 0, 167, 334, 501, 668    -> hueco 16
//
//  EXPANDED  (Tablet, 1280 x 800)
//    rail ................... ancho 124
//    titulo ................. x=60  y=40
//    lista .................. x=60  y=120 ancho 547.6
//    tarjetas ............... 0, 171, 342, 513, 684    -> hueco 20
//
//  Y ahi esta la regla, confirmada por tu propio diseno: mientras MAS grande la
//  pantalla, MAS margen. 20 -> 32 -> 60. El espacio de sobra se le da al aire,
//  no al contenido.
// ============================================================

/**
 * En cual de los tres tamanios estamos.
 *
 * Hasta ahora los margenes bastaban, pero la vista doble no es un cambio de
 * espaciado: es OTRO layout. Y esa decision no la puede tomar la pantalla
 * midiendo por su cuenta, porque entonces cada una podria contestar distinto.
 * Viaja aqui, junto a los margenes, como parte de la misma respuesta.
 *
 * Esta es la diferencia entre "responsive" (lo mismo, estirado) y "adaptive"
 * (una interfaz distinta que aprovecha el espacio). La vista doble es adaptive.
 */
enum class TamanoVentana { COMPACT, MEDIUM, EXPANDED }

/**
 * El paquete de medidas de una pantalla.
 *
 * Es una `data class` y no seis parametros sueltos porque siempre viajan
 * juntos: si una pantalla usa el margen de compact tiene que usar TODO lo de
 * compact. Al ir en un solo objeto, no se pueden mezclar por accidente.
 */
data class Margenes(
    /** En cual de los tres tamanios estamos. */
    val tamano: TamanoVentana,
    /** Margen izquierdo y derecho del contenido. */
    val lateral: Dp,
    /** Aire arriba del titulo (ya por debajo de la barra de estado). */
    val superior: Dp,
    /** Hueco entre el titulo y la primera tarjeta. */
    val tituloALista: Dp,
    /** Aire abajo. En compact incluye el alto de la barra, ver Andamio. */
    val inferior: Dp,
    /** Separacion entre tarjetas de la lista. */
    val entreTarjetas: Dp,
    /** Hasta donde puede crecer la lista antes de quedarse centrada. */
    val anchoMaximoLista: Dp,
    /** Hasta donde puede crecer el formulario. Mas angosto: son campos, no texto. */
    val anchoMaximoFormulario: Dp
)

private val COMPACT = Margenes(
    tamano = TamanoVentana.COMPACT,
    lateral = 20.dp,
    superior = 20.dp,        // y=44 en el Figma, menos los 24 de la barra de estado
    tituloALista = 16.dp,    // titulo termina en 92, lista empieza en 108
    inferior = 24.dp,        // el Andamio le suma el alto de la barra
    entreTarjetas = 12.dp,
    // Dp.Unspecified = "sin tope". Es lo que widthIn entiende como "no me
    // limites"; Dp.Infinity NO sirve, revienta al convertirse a pixeles.
    anchoMaximoLista = Dp.Unspecified,
    anchoMaximoFormulario = Dp.Unspecified
)

private val MEDIUM = Margenes(
    tamano = TamanoVentana.MEDIUM,
    lateral = 32.dp,
    superior = 40.dp,
    tituloALista = 24.dp,    // titulo termina en 88, lista empieza en 112
    inferior = 24.dp,
    entreTarjetas = 16.dp,
    anchoMaximoLista = Dp.Unspecified,
    anchoMaximoFormulario = 560.dp
)

private val EXPANDED = Margenes(
    tamano = TamanoVentana.EXPANDED,
    lateral = 60.dp,
    superior = 40.dp,
    tituloALista = 32.dp,    // titulo termina en 88, lista empieza en 120
    inferior = 32.dp,
    entreTarjetas = 20.dp,
    // En expanded la lista NO se topa: comparte el ancho con el panel de
    // detalle, y el reparto lo hacen los pesos de abajo.
    anchoMaximoLista = Dp.Unspecified,
    anchoMaximoFormulario = 560.dp
)

/**
 * Elige el paquete segun el ancho de la VENTANA.
 *
 * Una sola funcion decide los espaciados de toda la app. Si el Figma cambia,
 * cambias aqui y cambia en las tres pantallas. Es lo mismo que hace tu tema con
 * los colores: una fuente de verdad.
 */
fun margenesPara(ancho: Dp): Margenes = when {
    ancho < 600.dp -> COMPACT
    ancho < 840.dp -> MEDIUM
    else -> EXPANDED
}

// ============================================================
//  LA VISTA DOBLE (solo expanded)
//
//  En tu Figma de tablet (1280 x 800) el area de contenido mide 1156 y adentro:
//     lista ....... 547.6   desde x=60
//     hueco .......  32
//     detalle ..... 456.4   termina en 1096, o sea 60 del borde
//
//  Estan como PESOS y no como anchos fijos a proposito: en una tablet mas
//  grande, o con la app en media pantalla, 548 fijos dejarian un hueco feo a la
//  derecha. Con pesos el reparto se mantiene en la misma proporcion.
// ============================================================

const val PESO_LISTA = 547.6f
const val PESO_DETALLE = 456.4f
val HUECO_PANELES = 32.dp
