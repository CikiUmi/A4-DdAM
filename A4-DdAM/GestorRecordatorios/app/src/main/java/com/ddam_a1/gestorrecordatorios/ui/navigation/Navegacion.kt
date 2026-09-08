package com.ddam_a1.gestorrecordatorios.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ddam_a1.gestorrecordatorios.ui.components.DestinoNav
import com.ddam_a1.gestorrecordatorios.ui.screens.Andamio
import com.ddam_a1.gestorrecordatorios.ui.screens.Editar
import com.ddam_a1.gestorrecordatorios.ui.screens.Main
import com.ddam_a1.gestorrecordatorios.ui.screens.Papelera
import com.ddam_a1.gestorrecordatorios.viewmodel.RecordatoriosViewModel

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
private const val RUTA_CREAR = "editar"

@Composable
fun RecordatoriosNavHost(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    // UN solo ViewModel para toda la app: se pide aqui, arriba del NavHost, no
    // dentro de cada destino. Si lo pidieras adentro, Compose te daria uno
    // distinto por pantalla (uno por NavBackStackEntry) y la papelera nunca se
    // enteraria de lo que tiras desde la bandeja.
    val vm: RecordatoriosViewModel = hiltViewModel()

    // El menu lateral abierto/cerrado vive aqui, FUERA del NavHost, para que
    // sobreviva al cambio de pantalla. Si viviera dentro del Andamio se
    // reiniciaria en cada navegacion.
    var railAbierto by rememberSaveable { mutableStateOf(false) }
    val alternarRail = { railAbierto = !railAbierto }

    // Cual recordatorio esta abierto en el panel de detalle de la tablet.
    //
    // Vive AQUI, igual que el rail, por la misma razon: la pantalla se vuelve a
    // crear al navegar y perderia la seleccion. Y es `String?` (un id) y no un
    // Recordatorio: guardar el objeto seria guardar una foto vieja, y ademas
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
            Andamio(
                destinoActual = DestinoNav.BANDEJA,
                onDestino = { destino -> irA(destino) },
                onNuevo = { navController.navigate(RUTA_CREAR) { launchSingleTop = true } },
                railAbierto = railAbierto,
                onAlternarRail = alternarRail
            ) { margenes ->
                Main(
                    recordatorios = vm.ListaRecordatorios,
                    margenes = margenes,
                    onMoverAPapelera = { id -> vm.meterPapelera(id) },
                    onEditar = { id -> navController.navigate(rutaEditar(id)) },
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

            Andamio(
                destinoActual = DestinoNav.PAPELERA,
                onDestino = { destino -> irA(destino) },
                onNuevo = { navController.navigate(RUTA_CREAR) { launchSingleTop = true } },
                railAbierto = railAbierto,
                onAlternarRail = alternarRail
            ) { margenes ->
                Papelera(
                    recordatorios = vm.ListaPapelera,
                    margenes = margenes,
                    onRecuperar = { id -> vm.sacarPapelera(id) }
                )
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
            val original = vm.leer(id)

            Andamio(
                // null = ningun destino marcado. Editar no es una pestania de la
                // barra, es un lugar al que entras y del que sales.
                destinoActual = null,
                onDestino = { destino -> irA(destino) },
                onNuevo = { navController.navigate(RUTA_CREAR) { launchSingleTop = true } },
                railAbierto = railAbierto,
                onAlternarRail = alternarRail
            ) { margenes ->
                Editar(
                    original = original,
                    margenes = margenes,
                    onGuardar = { recordatorio ->
                        // Una sola pantalla, dos operaciones. Quien decide cual es
                        // esta linea: si venia un original, se actualiza; si no,
                        // se agrega.
                        val ok = if (original == null) vm.agregar(recordatorio)
                        else vm.actualizar(recordatorio)

                        // Solo se cierra si el ViewModel acepto. Si rechazo (el
                        // titulo vacio, la fecha en el pasado), la pantalla se
                        // queda y el usuario no pierde lo que escribio.
                        if (ok) navController.popBackStack()
                    },
                    onCancelar = { navController.popBackStack() }
                )
            }
        }
    }
}
