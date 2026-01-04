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
                                onFindMachineClick = { navController.navigate(Screen.Map.route) }
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
                                    // Naviga al dettaglio passando l'ID (convertito in String)
                                    navController.navigate(Screen.ProductDetail.createRoute(productId.toString()))
                                }
                            )
                        }

                        // --- DETTAGLIO PRODOTTO (MODIFICATO) ---
                        composable(
                            route = Screen.ProductDetail.route,
                            arguments = listOf(navArgument("productId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId")

                            ProductDetailScreen(
                                productId = productId,
                                onBackClick = {
                                    // Torna indietro
                                    navController.popBackStack()
                                },
                                onMachineClick = { machineId ->
                                    // Qui in futuro andremo alla pagina di acquisto specifica
                                    // Per ora torniamo alla mappa o facciamo un print
                                    println("Hai cliccato la macchinetta: $machineId")
                                    // Esempio: navController.navigate(Screen.Map.route)
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