package it.polito.did.dcym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import it.polito.did.dcym.ui.navigation.Screen
import it.polito.did.dcym.ui.theme.DontCallYourMomTheme
// IMPORTA LE SCHERMATE VERE
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

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // --- HOME ---
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onFindProductClick = { navController.navigate(Screen.Catalog.route) },
                                onFindMachineClick = { navController.navigate(Screen.Map.route) },
                                onEditProfileClick = { navController.navigate(Screen.Profile.route) } // <-- nuovo
                            )

                        }

                        // --- MAPPA (Placeholder per ora) ---
                        composable(Screen.Map.route) {
                            MapScreen(
                                onMachineSelected = {
                                    // Simuliamo che dalla mappa si vada al catalogo di quella macchinetta
                                    navController.navigate(Screen.Catalog.route)
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
                                    navController.navigate(Screen.Home.route) {
                                        launchSingleTop = true
                                    }
                                },
                                onGoToCatalog = {
                                    navController.navigate(Screen.Catalog.route) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        // --- PROFILO ---
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                onGoToHomeChoice = { navController.navigate(Screen.Home.route) },
                                onGoToCatalog = { navController.navigate(Screen.Catalog.route) }
                            )
                        }


                        // --- DETTAGLIO PRODOTTO (MODIFICATO) ---
                        // 1. AGGIORNA IL BLOCCO PRODUCT DETAIL
                        composable(
                            route = Screen.ProductDetail.route,
                            arguments = listOf(navArgument("productId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId")

                            ProductDetailScreen(
                                productId = productId,
                                onBackClick = { navController.popBackStack() },

                                // QUI LA MODIFICA: Quando clicchi la freccia della macchinetta
                                onMachineSelect = { machineId ->
                                    if (productId != null) {
                                        navController.navigate(Screen.PurchaseOptions.createRoute(productId, machineId))
                                    }
                                },

                                onMachineInfoClick = { machineId ->
                                    // Per ora non fa nulla o stampa un log
                                    println("Info macchinetta: $machineId")
                                }
                            )
                        }

// 1. AGGIORNA PURCHASE OPTIONS (Per navigare alla conferma)
                        composable(
                            route = Screen.PurchaseOptions.route,
                            // ... arguments uguali a prima ...
                        ) { backStackEntry ->
                            val pId = backStackEntry.arguments?.getString("productId")
                            val mId = backStackEntry.arguments?.getString("machineId")

                            PurchaseOptionsScreen(
                                productId = pId, machineId = mId,
                                onBackClick = { navController.popBackStack() },
                                // QUI I COLLEGAMENTI NUOVI:
                                onConfirmPurchase = {
                                    if (pId != null && mId != null) {
                                        navController.navigate(Screen.Confirmation.createRoute(pId, mId, isRent = false))
                                    }
                                },
                                onConfirmRent = {
                                    if (pId != null && mId != null) {
                                        navController.navigate(Screen.Confirmation.createRoute(pId, mId, isRent = true))
                                    }
                                }
                            )
                        }

// 2. AGGIUNGI LA NUOVA ROTTA DI CONFERMA
                        composable(
                            route = Screen.Confirmation.route,
                            arguments = listOf(
                                navArgument("pId") { type = NavType.StringType },
                                navArgument("mId") { type = NavType.StringType },
                                navArgument("isRent") { type = NavType.BoolType } // Parametro booleano
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
                                    // PROSSIMO STEP: Qui chiameremo la funzione che genera il codice e paga!
                                    println("CLICK FINALE! Rent: $isRent")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------
// RIMANI SOLO CON IL PLACEHOLDER DELLA MAPPA
// (CatalogScreen e ProductDetailScreen li hai cancellati da qui perché ora sono file veri)
// -------------------------------------------------------------------

@Composable
fun MapScreen(onMachineSelected: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { onMachineSelected("macchinetta_aule_i") }) {
            Text("Simula selezione Macchinetta (Mappa in costruzione)")
        }
    }
}