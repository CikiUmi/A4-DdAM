package com.ddam_a1.gestornotas.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ddam_a1.gestornotas.R
import com.ddam_a1.gestornotas.modelClasses.Imagen
import com.ddam_a1.gestornotas.modelClasses.Nota
import com.ddam_a1.gestornotas.ui.components.BarraUtils
import com.ddam_a1.gestornotas.ui.components.TiraImagenes
import com.ddam_a1.gestornotas.ui.components.Utilidad
import com.ddam_a1.gestornotas.ui.components.VisorImagen
import com.ddam_a1.gestornotas.ui.components.BotonAccionTarjeta
import com.ddam_a1.gestornotas.ui.components.ELEVACION_TARJETA
import com.ddam_a1.gestornotas.ui.components.PADDING_TARJETA
import com.ddam_a1.gestornotas.ui.components.RADIO_TARJETA
import com.ddam_a1.gestornotas.ui.theme.GestorNotasTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ============================================================
//  LA VISTA DE UNA NOTA
//
//  Es UNA pantalla con DOS modos, no dos pantallas:
//
//    LECTURA   texto plano, sin barra de utilidades. El boton de accion es un
//              lapiz y te pasa a edicion.
//    EDICION   los mismos textos, ahora escribibles, mas la barra para
//              adjuntar fotos. El boton es una palomita y guarda.
//
//  Que sea una sola pantalla no es ahorro de codigo: es lo que hace que el texto
//  NO SE MUEVA al empezar a escribir. Si fueran dos pantallas, al tocar el lapiz
//  todo saltaria un par de pixeles y se sentiria como otro lugar. Asi se siente
//  como la misma hoja. Es el truco de las notas de iOS.
//
//  QUIEN SE ACUERDA DE QUE
//
//  El modo (`editando`) NO vive aqui: entra por parametro. Tiene que vivir en el
//  NavHost porque el boton que lo cambia, en telefono, esta en la barra de
//  navegacion, que dibuja el Andamio — y el Andamio no esta dentro de esta
//  pantalla, es su hermano. El unico lugar desde donde se ven los dos es arriba
//  de ambos. Es el mismo izado de estado de `railAbierto`.
//
//  El TEXTO a medio escribir si vive aqui, y se manda hacia arriba con
//  `onBorrador` en cada tecla, para que quien guarde tenga la version de hoy.
//
//  MEDIDAS DEL FIGMA
//    compact   margen 20, fecha 14 alineada a la derecha, hueco 16, titulo 36
//    medium    margen 32, titulo 40, la tarjeta mide 512 de ancho
//    tarjeta   padding 24, texto 20 (compact) / 16 (medium)
// ============================================================

/** Hueco entre la linea de fecha y el titulo. En tu Figma son 16 en compact. */
private val FECHA_A_TITULO = 16.dp

/** Alto minimo de la tarjeta, para que una nota de una linea no quede ridicula. */
private val TARJETA_ALTO_MINIMO = 180.dp

private val FORMATO_FECHA =
    DateTimeFormatter.ofPattern("dd/MM/yy", Locale("es", "MX"))

/**
 * Vista / edicion de una nota.
 *
 * Sigue la regla de toda pantalla de este proyecto: NO conoce el ViewModel.
 * Recibe la nota ya resuelta y devuelve intenciones por callbacks.
 *
 * @param nota la nota a mostrar, o null si es una nota nueva
 * @param editando en cual de los dos modos estamos. Lo decide el NavHost.
 * @param onBorrador se llama en cada cambio de texto con la nota YA ARMADA.
 *        Quien guarda (el NavHost) se queda con la ultima que recibio.
 * @param onAlternarModo el boton de accion. En lectura pasa a edicion; en
 *        edicion guarda y vuelve a lectura. Es UNA sola lambda a proposito:
 *        el boton del Figma es uno solo, y dos lambdas permitirian que el icono
 *        dijera una cosa y el codigo hiciera otra.
 */
@Composable
fun Vista(
    nota: Nota?,
    editando: Boolean,
    margenes: Margenes,
    onBorrador: (Nota) -> Unit,
    onAlternarModo: () -> Unit,
    modifier: Modifier = Modifier,
    imagenes: List<Imagen> = emptyList(),
    onImagenElegida: (Uri) -> Unit = {},
    onQuitarImagen: (Imagen) -> Unit = {}
) {
    // La base sobre la que se copian los cambios. Si es nota nueva, una vacia:
    // el constructor de Nota ya pone id y fecha, no hay que inventarlos aqui.
    // `remember` y no `Nota()` pelon: sin el, cada recomposicion crearia una
    // nota NUEVA con id NUEVO, la llave de los rememberSaveable de abajo
    // cambiaria en cada tecla y el texto se borraria solo mientras escribes.
    val vacia = remember { Nota() }
    val base = nota ?: vacia

    // `rememberSaveable` y no `remember`: si giras el telefono a media frase, no
    // se puede perder lo escrito. La llave es el id para que al abrir OTRA nota
    // el estado se reinicie en vez de arrastrar el texto de la anterior.
    var titulo by rememberSaveable(base.id) { mutableStateOf(base.titulo) }
    var contenido by rememberSaveable(base.id) { mutableStateOf(base.contenido) }

    // Cada tecla vuelve a armar la nota y la manda para arriba. No guarda nada:
    // solo deja lista la version actual para cuando toquen la palomita.
    LaunchedEffect(titulo, contenido, base.id) {
        onBorrador(base.copy(titulo = titulo, contenido = contenido))
    }

    // ---- El selector de fotos del sistema ----
    //
    // `PickVisualMedia` es el selector NUEVO de Android: abre una pantalla con
    // pura galeria y NO PIDE NINGUN PERMISO. La app nunca ve tus demas fotos,
    // solo recibe la que escogiste. El permiso READ_MEDIA_IMAGES de toda la vida
    // no hace falta, y por eso no hay nada que agregar al Manifest.
    //
    // `rememberLauncherForActivityResult` es el puente: registra la pantalla que
    // se va a abrir y te devuelve un objeto para lanzarla. La lambda de abajo se
    // ejecuta cuando esa pantalla regresa, con la foto elegida o con null si la
    // persona se arrepintio.
    val selectorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onImagenElegida(uri)
    }

    // Cual foto esta abierta a pantalla completa. null = ninguna.
    //
    // Vive AQUI y no en el NavHost (a diferencia de `editando`) porque nadie mas
    // la necesita: el visor se dibuja dentro de esta pantalla y se cierra solo.
    // La regla es siempre la misma — el estado sube unicamente hasta donde haga
    // falta, ni un piso mas.
    var fotoAbierta by remember { mutableStateOf<Imagen?>(null) }

    fotoAbierta?.let { abierta ->
        VisorImagen(ruta = abierta.ruta, onCerrar = { fotoAbierta = null })
    }

    // En telefono el boton de accion esta en la muesca de la barra de abajo, y
    // lo dibuja el Andamio. De medium para arriba no hay barra abajo, hay rail,
    // asi que el boton se muda ADENTRO de la tarjeta. No se duplica: se mueve.
    val botonEnTarjeta = margenes.tamano != TamanoVentana.COMPACT

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            // Sube el contenido cuando aparece el teclado, para que no tape lo
            // que estas escribiendo.
            .imePadding()
            .padding(horizontal = margenes.lateral)
            .padding(top = margenes.superior, bottom = margenes.inferior)
    ) {
        // ---- La fecha, arriba a la derecha ----
        Text(
            text = textoFecha(base.fechaNota),
            style = MaterialTheme.typography.labelLarge,
            // #574419 al 70%. En el Figma la variable dice
            // `onSecondaryContainerLight`, pero ese hex en tu tema es
            // `onTertiaryContainer`. El nombre de alla esta cruzado.
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.70f),
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(FECHA_A_TITULO))

        // ---- El titulo ----
        //
        // Cambia de COLOR segun el modo, y eso no es decoracion: es la unica
        // pista de que el titulo se puede tocar. En lectura va en el cafe del
        // texto; al editar se pone rosa (`primary`), el color de "esto es tuyo,
        // escribele". Sale asi de tu Figma: la pantalla de editar tiene el
        // titulo en #904A42 y la de leer en #574419.
        val colorTitulo =
            if (editando) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onTertiaryContainer

        val estiloTitulo = MaterialTheme.typography.displaySmall.copy(color = colorTitulo)

        if (editando) {
            CampoLibre(
                valor = titulo,
                onValor = { titulo = it },
                marcador = "Titulo de la nota",
                estilo = estiloTitulo,
                unaLinea = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = titulo.ifBlank { "Nota sin titulo" },
                style = estiloTitulo,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(margenes.tituloALista))

        // ---- La tarjeta con el contenido ----
        Card(
            shape = RoundedCornerShape(RADIO_TARJETA),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = ELEVACION_TARJETA),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = margenes.anchoMaximoLista)
                .heightIn(min = TARJETA_ALTO_MINIMO)
        ) {
            Column(Modifier.padding(PADDING_TARJETA)) {

                val estiloCuerpo = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                if (editando) {
                    CampoLibre(
                        valor = contenido,
                        onValor = { contenido = it },
                        marcador = "Escribe aqui...",
                        estilo = estiloCuerpo,
                        unaLinea = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = contenido.ifBlank { "Esta nota todavia esta vacia." },
                        style = estiloCuerpo,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ---- Las fotos ----
                //
                // Van DESPUES del texto y ANTES de las utilidades, como en un
                // diario de papel: primero lo que escribiste, luego lo que
                // pegaste. Si no hay fotos, TiraImagenes no dibuja nada, ni
                // siquiera este Spacer se nota.
                if (imagenes.isNotEmpty()) {
                    Spacer(Modifier.height(PADDING_TARJETA))
                    TiraImagenes(
                        imagenes = imagenes,
                        editando = editando,
                        // Tocar una foto la abre completa SOLO leyendo, tal cual
                        // lo pediste: editando el toque es para quitarla.
                        onAbrir = { fotoAbierta = it },
                        onQuitar = onQuitarImagen
                    )
                }

                // ---- La fila de utilidades, SOLO al editar ----
                //
                // En lectura no se dibuja nada, ni un hueco. Una barra de
                // herramientas que no puedes usar no informa: estorba.
                if (editando) {
                    Spacer(Modifier.height(PADDING_TARJETA))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BarraUtils(
                            onUtilidad = { util ->
                                when (util) {
                                    // El `ImageOnly` es lo que hace que el
                                    // selector no ofrezca videos.
                                    Utilidad.FOTO -> selectorFoto.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            }
                        )

                        if (botonEnTarjeta) {
                            BotonAccionTarjeta(
                                icono = R.drawable.ic_check,
                                descripcion = "Guardar nota",
                                onClick = onAlternarModo
                            )
                        }
                    }
                } else if (botonEnTarjeta) {
                    // En lectura, de medium para arriba, el lapiz se queda solo
                    // a la derecha: no hay utilidades que acompañarlo.
                    Spacer(Modifier.height(PADDING_TARJETA))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        BotonAccionTarjeta(
                            icono = R.drawable.ic_edit,
                            descripcion = "Editar nota",
                            onClick = onAlternarModo
                        )
                    }
                }
            }
        }
    }
}

/**
 * Un campo de texto SIN caja.
 *
 * Se usa `BasicTextField` y no `OutlinedTextField` a proposito: el de Material
 * trae su propio borde, su etiqueta flotante y su fondo, y ninguno de los tres
 * esta en tu diseño. Aqui el texto tiene que verse EXACTAMENTE igual leyendo que
 * escribiendo; lo unico que cambia es que aparece el cursor.
 *
 * El marcador (placeholder) se dibuja a mano, en un `Box` detras: `BasicTextField`
 * no trae uno, y sin el, una nota vacia seria una pantalla en blanco sin pistas.
 */
@Composable
private fun CampoLibre(
    valor: String,
    onValor: (String) -> Unit,
    marcador: String,
    estilo: TextStyle,
    unaLinea: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        if (valor.isEmpty()) {
            Text(
                text = marcador,
                style = estilo.copy(color = estilo.color.copy(alpha = 0.45f))
            )
        }
        BasicTextField(
            value = valor,
            onValueChange = onValor,
            textStyle = estilo,
            singleLine = unaLinea,
            // El cursor toma el color del texto; si no, sale del color por
            // defecto de Material y no combina con la paleta.
            cursorBrush = SolidColor(estilo.color),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** "Creado el 14/09/26". Si la nota no tiene titulo todavia, igual tiene fecha. */
private fun textoFecha(momento: LocalDateTime): String =
    "Creado el ${momento.format(FORMATO_FECHA)}"

// ============================================================
//  Previews — los cuatro estados que importan
// ============================================================

private const val FONDO_APP = 0xFFECE1D4

private fun notaDeEjemplo() = Nota(
    titulo = "Titulo de la Nota",
    contenido = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Pellentesque a nisi ultricies, faucibus risus feugiat, maximus augue. " +
            "Etiam felis libero, viverra sed enim quis, efficitur mollis nunc. " +
            "Maecenas arcu metus, porta ac ultrices et, vehicula et ante."
)

@Preview(name = "1 - Lectura (telefono)", showBackground = true, backgroundColor = FONDO_APP, widthDp = 412, heightDp = 800)
@Composable
private fun VistaLecturaPreview() {
    GestorNotasTheme {
        Vista(notaDeEjemplo(), editando = false, margenes = margenesPara(412.dp), onBorrador = {}, onAlternarModo = {})
    }
}

@Preview(name = "2 - Edicion (telefono)", showBackground = true, backgroundColor = FONDO_APP, widthDp = 412, heightDp = 800)
@Composable
private fun VistaEdicionPreview() {
    GestorNotasTheme {
        Vista(notaDeEjemplo(), editando = true, margenes = margenesPara(412.dp), onBorrador = {}, onAlternarModo = {})
    }
}

@Preview(name = "3 - Nota nueva (vacia)", showBackground = true, backgroundColor = FONDO_APP, widthDp = 412, heightDp = 800)
@Composable
private fun VistaNuevaPreview() {
    GestorNotasTheme {
        Vista(null, editando = true, margenes = margenesPara(412.dp), onBorrador = {}, onAlternarModo = {})
    }
}

@Preview(name = "4 - Edicion (medium, boton dentro)", showBackground = true, backgroundColor = FONDO_APP, widthDp = 700, heightDp = 840)
@Composable
private fun VistaMediumPreview() {
    GestorNotasTheme {
        Vista(notaDeEjemplo(), editando = true, margenes = margenesPara(700.dp), onBorrador = {}, onAlternarModo = {})
    }
}
