package com.ddam_a1.gestornotas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ddam_a1.gestornotas.R
import com.ddam_a1.gestornotas.data.AlmacenImagenes
import com.ddam_a1.gestornotas.modelClasses.Nota
import com.ddam_a1.gestornotas.ui.components.DestinoNav
import com.ddam_a1.gestornotas.ui.screens.Andamio
import com.ddam_a1.gestornotas.ui.screens.Editar
import com.ddam_a1.gestornotas.ui.screens.Main
import com.ddam_a1.gestornotas.ui.screens.Papelera
import com.ddam_a1.gestornotas.ui.screens.Vista
import com.ddam_a1.gestornotas.viewmodel.NotasViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

// ============================================================
//  LAS RUTAS
//
//  Constantes y no strings sueltos: si escribes "papelera" en un lado y
//  "Papelera" en otro, el compilador no te dice nada y la app truena en tiempo
//  de ejecucion. Con constantes, un typo no compila.
// ============================================================

private const val RUTA_BANDEJA = "bandeja"
private const val RUTA_PAPELERA = "papelera"

/**
 * La ruta de editar lleva el id COMO ARGUMENTO OPCIONAL:
 *
 *     editar            -> crear uno nuevo   (id = null)
 *     editar?id=abc-123 -> editar ese
 *
 * Es la misma idea de tu `leer(id: String?)`: un id nulo significa "vengo a
 * crear". La ruta y el ViewModel hablan el mismo idioma.
 */
private const val ARG_ID = "id"
private const val RUTA_EDITAR = "editar?$ARG_ID={$ARG_ID}"

private fun rutaEditar(id: String) = "editar?$ARG_ID=$id"

/**
 * A donde lleva el boton "+" de la barra.
 *
 * ANTES ERA "editar", el formulario con Titulo / Descripcion / Fecha / Hora.
 * Ese formulario es lo ultimo que queda de cuando la app era de recordatorios:
 * ahi tenia sentido preguntar CUANDO te aviso, y por eso pedia fecha y hora.
 *
 * En un diario no se escoge la fecha: es cuando lo escribiste, y `Nota()` ya la
 * pone sola. Y `Vista` hace todo lo que hacia ese formulario (crear y modificar)
 * sin salir de la hoja. Tener los dos caminos significaba que el "+" abria una
 * pantalla y el lapiz de una nota abria otra, para lo mismo.
 *
 * Es "vista" a secas, sin `?id=`: el argumento es opcional y su valor por
 * defecto es null, y un id nulo ya significaba "vengo a crear". La pantalla
 * arranca en modo edicion sola.
 *
 * `Editar.kt` NO se borro: sigue en el proyecto y su destino sigue registrado
 * aqui abajo, nada mas que ya ningun boton lleva a el.
 */
private const val RUTA_CREAR = "vista"

/**
 * La vista de una nota. Misma forma que editar, mismo argumento opcional:
 *
 *     vista            -> nota nueva   (id = null)
 *     vista?id=abc-123 -> esa nota
 *
 * Todavia esta vacia; existe para que tocar una tarjeta ya lleve a algun lado.
 */
private const val RUTA_VISTA = "vista?$ARG_ID={$ARG_ID}"

private fun rutaVista(id: String) = "vista?$ARG_ID=$id"

@Composable
fun NotasNavHost(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    // UN solo ViewModel para toda la app: se pide aqui, arriba del NavHost, no
    // dentro de cada destino. Si lo pidieras adentro, Compose te daria uno
    // distinto por pantalla (uno por NavBackStackEntry) y la papelera nunca se
    // enteraria de lo que tiras desde la bandeja.
    val vm: NotasViewModel = hiltViewModel()

    // El menu lateral abierto/cerrado vive aqui, FUERA del NavHost, para que
    // sobreviva al cambio de pantalla. Si viviera dentro del Andamio se
    // reiniciaria en cada navegacion.
    var railAbierto by rememberSaveable { mutableStateOf(false) }
    val alternarRail = { railAbierto = !railAbierto }

    // Cual nota esta abierto en el panel de detalle de la tablet.
    //
    // Vive AQUI, igual que el rail, por la misma razon: la pantalla se vuelve a
    // crear al navegar y perderia la seleccion. Y es `String?` (un id) y no un
    // Nota: guardar el objeto seria guardar una foto vieja, y ademas
    // rememberSaveable solo sabe guardar cosas simples.
    //
    // En telefono nadie lo usa: alli tocar una tarjeta la despliega en su sitio.
    var seleccionadoId by rememberSaveable { mutableStateOf<String?>(null) }

    // Ir a un destino de la barra. `popUpTo` evita que se apilen bandejas y
    // papeleras infinitas al ir y venir; `launchSingleTop` evita dos copias de
    // la misma pantalla si le picas dos veces al mismo icono.
    fun irA(destino: DestinoNav) {
        val ruta = when (destino) {
            DestinoNav.BANDEJA -> RUTA_BANDEJA
            DestinoNav.PAPELERA -> RUTA_PAPELERA
        }
        navController.navigate(ruta) {
            popUpTo(RUTA_BANDEJA) { inclusive = false }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = RUTA_BANDEJA,
        modifier = modifier
    ) {

        // ---------- BANDEJA ----------
        composable(RUTA_BANDEJA) {

            // `listaNotas` ya no es una lista: es un StateFlow, o sea un CANAL.
            // `collectAsState()` se suscribe a el y convierte cada emision en
            // estado de Compose.
            //
            // Esa suscripcion es la que hace que la pantalla se redibuje SOLA
            // cuando Room detecta que la tabla cambio. Ya nadie le avisa a nadie:
            // guardas en la base, la base emite, la pantalla se entera.
            //
            // Y va AQUI DENTRO, no arriba del NavHost: asi la suscripcion nace y
            // muere con la pantalla, que es justo lo que el WhileSubscribed(5000)
            // de tu `stateIn` esta esperando para poder soltar el flow.
            val notas by vm.listaNotas.collectAsState()

            Andamio(
                destinoActual = DestinoNav.BANDEJA,
                onDestino = { destino -> irA(destino) },
                onNuevo = { navController.navigate(RUTA_CREAR) { launchSingleTop = true } },
                railAbierto = railAbierto,
                onAlternarRail = alternarRail
            ) { margenes ->
                Main(
                    notas = notas,
                    margenes = margenes,
                    onMoverAPapelera = { id -> vm.meterPapelera(id) },
                    // El panel de tablet tambien abre la hoja, no el formulario.
                    onEditar = { id -> navController.navigate(rutaVista(id)) },
                    // En telefono y tablet chica, tocar una tarjeta abre su vista.
                    onAbrir = { id -> navController.navigate(rutaVista(id)) },
                    seleccionadoId = seleccionadoId,
                    onSeleccionar = { id -> seleccionadoId = id }
                )
            }
        }

        // ---------- PAPELERA ----------
        composable(RUTA_PAPELERA) {
            // Tu "funcion que se llama al entrar". LaunchedEffect(Unit) corre una
            // sola vez al aparecer la pantalla: al abrir la papelera se tiran los
            // que ya cumplieron sus 5 dias.
            LaunchedEffect(Unit) { vm.limpiarPapelera() }

            // Mismo canal, otra consulta. Fijate que no hay que filtrar nada
            // aqui: el WHERE en_papelera = 1 vive en el DAO, en SQL.
            val notas by vm.listaPapelera.collectAsState()

            Andamio(
                destinoActual = DestinoNav.PAPELERA,
                onDestino = { destino -> irA(destino) },
                onNuevo = { navController.navigate(RUTA_CREAR) { launchSingleTop = true } },
                railAbierto = railAbierto,
                onAlternarRail = alternarRail
            ) { margenes ->
                Papelera(
                    notas = notas,
                    margenes = margenes,
                    onRecuperar = { id -> vm.sacarPapelera(id) }
                )
            }
        }

        // ---------- VISTA DE UNA NOTA ----------
        composable(
            route = RUTA_VISTA,
            arguments = listOf(
                navArgument(ARG_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entrada ->
            // Igual que editar: leer por id, y si no existe es una nota nueva.
            val id = entrada.arguments?.getString(ARG_ID)

            // `vm.leer` es suspend, y un @Composable NO es una corrutina: no
            // sabe esperar. Quien si sabe es LaunchedEffect. Entonces el patron
            // es siempre el mismo: un hueco de estado + un efecto que lo llena.
            var nota by remember(id) { mutableStateOf<Nota?>(null) }
            var cargado by remember(id) { mutableStateOf(false) }

            LaunchedEffect(id) {
                nota = vm.leer(id)
                cargado = true      // ya se la respuesta, sea cual sea
            }

            // ---- El modo: leyendo o editando ----
            //
            // Vive AQUI y no dentro de Vista porque en telefono el boton que lo
            // cambia esta en la muesca de la barra de abajo, y esa barra la
            // dibuja el Andamio, que es HERMANO de la pantalla, no su hijo. El
            // unico lugar desde donde se ven los dos es este. Izado de estado
            // otra vez, igual que `railAbierto`.
            //
            // Arranca en edicion si no hay id: una nota que todavia no existe no
            // tiene nada que leer, seria una hoja en blanco pidiendo un toque de
            // mas.
            var editando by rememberSaveable(id) { mutableStateOf(id == null) }

            // El borrador: la nota como va quedando mientras escribe.
            //
            // Es un `mutableStateOf` SIN `by` a proposito. Solo se lee dentro del
            // onClick de abajo, nunca durante la composicion, y Compose unicamente
            // rastrea las lecturas que ocurren mientras dibuja. Resultado:
            // escribir actualiza el borrador sin recomponer todo el NavHost en
            // cada tecla.
            val borrador = remember(id) { mutableStateOf<Nota?>(null) }

            // ---- Las fotos de esta nota ----
            //
            // `collectAsState` sobre el Flow del ViewModel. Al adjuntar una,
            // Room vuelve a emitir y la tira se actualiza sola: no hay que
            // recargar la nota ni avisarle a nadie.
            //
            // La llave del remember es el id para no armar un Flow nuevo en
            // cada recomposicion ni arrastrar el de la nota anterior.
            //
            // Si la nota todavia no existe en la base (`nota` en null, o sea una
            // nota recien abierta y sin guardar) no hay nada que consultar: no
            // puede tener fotos, porque una foto necesita una nota a la cual
            // apuntar. `flowOf(emptyList())` es un Flow que emite una lista
            // vacia y ya; asi el resto del codigo no tiene que preguntar si hay
            // o no hay nota. En cuanto se adjunte la primera foto, `nota` deja
            // de ser null, la llave cambia y este Flow se vuelve el de verdad.
            val idImagenes = nota?.id
            val flujoImagenes = remember(idImagenes) {
                if (idImagenes == null) flowOf(emptyList())
                else vm.imagenesDe(idImagenes)
            }
            val imagenes by flujoImagenes.collectAsState(initial = emptyList())

            // Copiar el archivo es trabajo suspendido, y esto no es un
            // @Composable donde se pueda usar LaunchedEffect: pasa cuando la
            // persona TOCA algo. `rememberCoroutineScope` da un alcance atado a
            // esta pantalla, que se cancela solo si sales antes de que termine.
            val contexto = LocalContext.current
            val alcance = rememberCoroutineScope()

            // UNA sola accion para el boton, la use quien la use: la muesca en
            // telefono o el boton dentro de la tarjeta en tablet. Si hubiera dos
            // copias de esta logica, tarde o temprano una guardaria y la otra no.
            val alternarModo = {
                if (editando) {
                    borrador.value?.let { editada ->
                        // `guardar` es el Upsert: sirve igual si la nota es
                        // nueva o si ya existia. Antes habia que escoger entre
                        // `agregar` y `actualizar` mirando si `nota` era null, y
                        // eso se rompia en un caso real: si adjuntabas una foto
                        // en una nota nueva, la nota YA quedaba guardada, pero
                        // `nota` aqui seguia en null y el guardado se iba por
                        // `agregar`, que con IGNORE no hacia absolutamente nada.
                        // Con un solo metodo no hay nada que adivinar.
                        vm.guardar(editada)
                        // Se adopta lo recien guardado para que al volver a
                        // lectura se lea lo nuevo y no lo que habia al entrar.
                        nota = editada
                    }
                    editando = false
                } else {
                    editando = true
                }
            }

            Andamio(
                destinoActual = null,
                onDestino = { destino -> irA(destino) },
                onNuevo = alternarModo,
                // El icono ES el contrato con el usuario: lapiz promete editar,
                // palomita promete guardar. Por eso sale del MISMO `editando`
                // que decide que hace `alternarModo`; no se pueden desincronizar.
                iconoAccion = if (editando) R.drawable.ic_check else R.drawable.ic_edit,
                descripcionAccion = if (editando) "Guardar nota" else "Editar nota",
                railAbierto = railAbierto,
                onAlternarRail = alternarRail
            ) { margenes ->
                // El Andamio (barra, rail) se dibuja ya; solo el contenido
                // espera a que la lectura termine. El parpadeo dura un frame.
                if (cargado) {
                    Vista(
                        nota = nota,
                        editando = editando,
                        margenes = margenes,
                        onBorrador = { borrador.value = it },
                        onAlternarModo = alternarModo,
                        imagenes = imagenes,
                        onImagenElegida = { uri ->
                            alcance.launch {
                                // 1. copiar el archivo a la carpeta de la app
                                val ruta = AlmacenImagenes.guardar(contexto, uri)
                                // 2. guardar la nota (por la llave foranea) y el
                                //    renglon de la imagen
                                val actual = borrador.value ?: nota
                                if (ruta != null && actual != null) {
                                    vm.agregarImagen(actual, ruta)
                                    // La nota ya existe en la base: adoptarla
                                    // aqui evita que un guardado posterior la
                                    // trate como nueva.
                                    nota = actual
                                }
                            }
                        },
                        onQuitarImagen = { imagen ->
                            vm.quitarImagen(imagen)
                            // El renglon lo borra Room; el ARCHIVO no. SQLite no
                            // sabe de archivos, asi que hay que decirlo aparte.
                            AlmacenImagenes.borrar(imagen.ruta)
                        }
                    )
                }
            }
        }

        // ---------- CREAR / EDITAR ----------
        composable(
            route = RUTA_EDITAR,
            arguments = listOf(
                navArgument(ARG_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entrada ->
            // El id viaja dentro de la ruta y se saca de los argumentos.
            // Puede ser null, y eso NO es un error: significa "nuevo".
            val id = entrada.arguments?.getString(ARG_ID)

            // Mismo patron que en vista, pero AQUI EL `cargado` NO ES OPCIONAL.
            //
            // Editar arranca sus campos con
            //     rememberSaveable { mutableStateOf(original?.titulo ?: "") }
            // y eso corre UNA SOLA VEZ, en el primer frame. Si en ese frame le
            // pasas null porque la lectura sigue en vuelo, el formulario se abre
            // VACIO y ya nunca se vuelve a llenar: editarias una nota existente
            // encima de una hoja en blanco.
            //
            // Por eso un `Nota?` no alcanza: null significa dos cosas distintas,
            // "es nueva" y "todavia no cargo". Hace falta el tercer estado.
            var original by remember(id) { mutableStateOf<Nota?>(null) }
            var cargado by remember(id) { mutableStateOf(false) }

            LaunchedEffect(id) {
                original = vm.leer(id)
                cargado = true
            }

            Andamio(
                // null = ningun destino marcado. Editar no es una pestania de la
                // barra, es un lugar al que entras y del que sales.
                destinoActual = null,
                onDestino = { destino -> irA(destino) },
                onNuevo = { navController.navigate(RUTA_CREAR) { launchSingleTop = true } },
                railAbierto = railAbierto,
                onAlternarRail = alternarRail
            ) { margenes ->
                if (cargado) {
                    Editar(
                        original = original,
                        margenes = margenes,
                        onGuardar = { nota ->
                            // Una sola pantalla, dos operaciones. Y quien decide
                            // cual es el ID, no el objeto: el id lo tienes desde
                            // el primer frame, sin esperar a nadie. Preguntarle al
                            // objeto algo que la ruta ya sabia era el rodeo.
                            val ok = if (id == null) vm.agregar(nota)
                            else vm.actualizar(nota)

                            // Solo se cierra si el ViewModel acepto. Si rechazo
                            // (el titulo vacio), la pantalla se queda y el usuario
                            // no pierde lo que escribio.
                            if (ok) navController.popBackStack()
                        },
                        onCancelar = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
