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
import it.polito.did.dcym.ui.screens.profile.ProfileScreen

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

                        // --- MAPPA (placeholder) ---
                        composable(Screen.Map.route) {
                            MapScreen(
                                onMachineSelected = {
                                    // per ora: rimanda al catalogo
                                    navController.navigate(Screen.Catalog.route) { launchSingleTop = true }
                                }
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
                                    navController.navigate("history") { launchSingleTop = true } // oppure Screen.History.route se lo aggiungi
                                },
                                onGoToHelp = {
                                    navController.navigate("help") { launchSingleTop = true } // idem
                                }
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
                            route = Screen.Confirmation.route,
                            arguments = listOf(
                                navArgument("pId") { type = NavType.StringType },
                                navArgument("mId") { type = NavType.StringType },
                                navArgument("isRent") { type = NavType.BoolType }
                            )
                        ) { backStackEntry ->
                            val pId = backStackEntry.arguments?.getString("pId")
                            val mId = backStackEntry.arguments?.getString("mId")
                            val isRent = backStackEntry.arguments?.getBoolean("isRent") ?: false

                            ConfirmationScreen(
                                productId = pId,
                                machineId = mId,
                                isRent = isRent,
                                onBackClick = { navController.popBackStack() },
                                onFinalConfirmClick = {
                                    println("CLICK FINALE! Rent: $isRent")
                                }
                            )
                        }

                        // --- (OPZIONALE) HISTORY / HELP placeholder se li metti nella navbar ---
                        // Se nel tuo Screen.kt NON esistono, commenta questi due blocchi.
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
