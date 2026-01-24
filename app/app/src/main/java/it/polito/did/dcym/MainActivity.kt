package it.polito.did.dcym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import it.polito.did.dcym.ui.components.GraphPaperBackground
import it.polito.did.dcym.ui.navigation.Screen
import it.polito.did.dcym.ui.theme.DontCallYourMomTheme



// screens veri
import it.polito.did.dcym.ui.screens.home.HomeScreen
import it.polito.did.dcym.ui.screens.catalog.CatalogScreen
import it.polito.did.dcym.ui.screens.detail.ProductDetailScreen
import it.polito.did.dcym.ui.screens.purchase.PurchaseOptionsScreen
import it.polito.did.dcym.ui.screens.confirmation.ConfirmationScreen
import it.polito.did.dcym.ui.screens.history.HistoryScreen
import it.polito.did.dcym.ui.screens.profile.ProfileScreen
import it.polito.did.dcym.ui.screens.playback.PlaybackScreen
import it.polito.did.dcym.ui.screens.map.MapScreen
import it.polito.did.dcym.ui.screens.machinecatalog.MachineCatalogScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DontCallYourMomTheme {
                val navController = rememberNavController()

                // ✅ sfondo globale a quadretti per TUTTE le schermate
                GraphPaperBackground {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {

                        // --- HOME (scelta percorso) ---
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onFindProductClick = {
                                    navController.navigate(Screen.Catalog.route) { launchSingleTop = true }
                                },
                                onFindMachineClick = {
                                    navController.navigate(Screen.Map.route) { launchSingleTop = true }
                                },
                                onEditProfileClick = {
                                    navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                                }
                            )
                        }

                        // --- SEZIONE MAPPA / MACCHINETTE ---
                        composable(Screen.Map.route) {
                            MapScreen(
                                onGoToHomeChoice = {
                                    // Torna alla home svuotando lo stack se serve, o navigate semplice
                                    navController.popBackStack(Screen.Home.route, inclusive = false)
                                },
                                onMachineClick = { machineId ->
                                    // Naviga al catalogo filtrato per questa macchinetta
                                    navController.navigate(Screen.MachineCatalog.createRoute(machineId))
                                },
                                onGoToProfile = { navController.navigate(Screen.Profile.route) },
                                onGoToHistory = { navController.navigate(Screen.History.route) },
                                onGoToHelp = { /* todo help */ }
                            )
                        }

                        // --- CATALOGO ---
                        composable(Screen.Catalog.route) {
                            CatalogScreen(
                                onProductClick = { productId ->
                                    navController.navigate(Screen.ProductDetail.createRoute(productId.toString()))
                                },
                                onGoToHomeChoice = {
                                    navController.navigate(Screen.Home.route) { launchSingleTop = true }
                                },
                                onGoToCatalog = {
                                    navController.navigate(Screen.Catalog.route) { launchSingleTop = true }
                                },
                                onGoToProfile = {
                                    navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                                },
                                onGoToHistory = {
                                    navController.navigate(Screen.History.route)
                                },
                                onGoToHelp = {
                                    navController.navigate("help") { launchSingleTop = true } // idem
                                }
                            )
                        }
                        // --- CATALOGO MACCHINETTA (FILTRATO) ---
                        composable(
                            route = Screen.MachineCatalog.route, // "machine_catalog/{machineId}"
                            arguments = listOf(
                                navArgument("machineId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val machineId = backStackEntry.arguments?.getString("machineId") ?: ""

                            MachineCatalogScreen(
                                machineId = machineId,
                                onBackClick = { navController.popBackStack() },
                                onProductClick = { productId ->
                                    // Da qui andiamo al dettaglio prodotto
                                    // (Nota: idealmente dovremmo passare anche machineId al dettaglio per sapere che è già selezionata,
                                    // ma per ora va bene il flusso standard)
                                    navController.navigate(Screen.ProductDetail.createRoute(productId.toString()))
                                },
                                onGoToHomeChoice = { navController.popBackStack(Screen.Home.route, inclusive = false) },
                                onGoToProfile = { navController.navigate(Screen.Profile.route) },
                                onGoToHistory = { navController.navigate(Screen.History.route) },
                                onGoToHelp = { /* todo help */ }
                            )
                        }


                        // --- PROFILO ---
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                onGoToHomeChoice = {
                                    navController.navigate(Screen.Home.route) { launchSingleTop = true }
                                },
                                onGoToCatalog = {
                                    navController.navigate(Screen.Catalog.route) { launchSingleTop = true }
                                },
                                onGoToProfile = {
                                    navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                                },
                                onGoToHistory = {
                                    navController.navigate("history") { launchSingleTop = true } // oppure Screen.History.route se lo aggiungi
                                },
                                onGoToHelp = {
                                    navController.navigate("help") { launchSingleTop = true } // idem
                                }
                            )
                        }

                        // --- DETTAGLIO PRODOTTO ---
                        composable(
                            route = Screen.ProductDetail.route,
                            arguments = listOf(navArgument("productId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId")

                            ProductDetailScreen(
                                productId = productId,
                                onBackClick = { navController.popBackStack() },

                                onMachineSelect = { machineId ->
                                    if (productId != null) {
                                        navController.navigate(
                                            Screen.PurchaseOptions.createRoute(productId, machineId)
                                        )
                                    }
                                },

                                onMachineInfoClick = { machineId ->
                                    println("Info macchinetta: $machineId")
                                } ,
                                onGoToHomeChoice = {
                                    navController.navigate(Screen.Home.route) { launchSingleTop = true }
                                },
                                onGoToCatalog = {
                                    navController.navigate(Screen.Catalog.route) { launchSingleTop = true }
                                },
                                onGoToProfile = {
                                    navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                                },
                                onGoToHistory = {
                                    navController.navigate("history") { launchSingleTop = true } // oppure Screen.History.route se lo aggiungi
                                },
                                onGoToHelp = {
                                    navController.navigate("help") { launchSingleTop = true } // idem
                                }
                            )
                        }
                        composable(Screen.History.route) { PlaceholderScreen("Storico (in costruzione)") }
                        composable(Screen.Help.route) { PlaceholderScreen("Aiuto (in costruzione)") }

                        // --- PURCHASE OPTIONS (✅ arguments corretti) ---
                        composable(
                            route = Screen.PurchaseOptions.route,
                            arguments = listOf(
                                navArgument("productId") { type = NavType.StringType },
                                navArgument("machineId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val pId = backStackEntry.arguments?.getString("productId")
                            val mId = backStackEntry.arguments?.getString("machineId")

                            PurchaseOptionsScreen(
                                productId = pId,
                                machineId = mId,
                                onBackClick = { navController.popBackStack() },
                                onConfirmPurchase = {
                                    if (pId != null && mId != null) {
                                        navController.navigate(
                                            Screen.Confirmation.createRoute(pId, mId, isRent = false)
                                        )
                                    }
                                },
                                onConfirmRent = {
                                    if (pId != null && mId != null) {
                                        navController.navigate(
                                            Screen.Confirmation.createRoute(pId, mId, isRent = true)
                                        )
                                    }
                                }
                            )
                        }

                        // --- CONFIRMATION ---
                        composable(
                            route = Screen.Confirmation.route
                        ) { backStackEntry ->
                            val pId = backStackEntry.arguments?.getString("pId")
                            val mId = backStackEntry.arguments?.getString("mId")
                            val isRent = backStackEntry.arguments?.getString("isRent")?.toBoolean() ?: false

                            ConfirmationScreen(
                                productId = pId,
                                machineId = mId,
                                isRent = isRent,
                                onBackClick = { navController.popBackStack() },
                                onFinalConfirmClick = { /* Gestito internamente */ },
                                onPaymentSuccess = { orderId ->
                                    // QUI NAVIGHIAMO PASSANDO L'ID
                                    navController.navigate(Screen.Playback.createRoute(orderId))
                                }
                            )
                        }


                        // --- AGGIORNA QUESTA PARTE ---
                        composable(
                            route = Screen.Playback.route
                        ) { backStackEntry ->
                            // RECUPERA L'ID
                            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""

                            PlaybackScreen(
                                orderId = orderId,
                                onGoToHome = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                },
                                onGoToCatalog = {
                                    navController.navigate(Screen.Catalog.route) {
                                        popUpTo(Screen.Home.route)
                                    }
                                },
                                onGoToProfile = { navController.navigate(Screen.Profile.route) },
                                onGoToHistory = { navController.navigate(Screen.History.route) }
                            )
                        }
                        // --- (OPZIONALE) HISTORY / HELP placeholder se li metti nella navbar ---
                        composable(Screen.History.route) {
                            // Qui chiamiamo la schermata che abbiamo creato
                            HistoryScreen(
                                onGoToHomeChoice = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                },
                                onGoToCatalog = {
                                    navController.navigate(Screen.Catalog.route) {
                                        popUpTo(Screen.Home.route)
                                    }
                                },
                                onGoToProfile = {
                                    navController.navigate(Screen.Profile.route)
                                },
                                onGoToHelp = {
                                    navController.navigate(Screen.Help.route)
                                }
                            )
                        }
                        /*
                        composable(Screen.History.route) {
                            PlaceholderScreen("Storico (in costruzione)")
                        }
                        composable(Screen.Help.route) {
                            PlaceholderScreen("Aiuto (in costruzione)")
                        }
                        */
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// PLACEHOLDER MAPPA
// ----------------------------------------------------
@Composable
fun MapScreen(onMachineSelected: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { onMachineSelected("macchinetta_aule_i") }) {
            Text("Simula selezione Macchinetta (Mappa in costruzione)")
        }
    }
}

// ----------------------------------------------------
// (OPZIONALE) Placeholder generico
// ----------------------------------------------------
@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title)
    }
}
