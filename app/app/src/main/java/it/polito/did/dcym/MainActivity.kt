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
import it.polito.did.dcym.ui.screens.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DontCallYourMomTheme {
                // 1. Inizializziamo il controller della navigazione
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 2. Definiamo il grafo di navigazione
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // SCHERMATA HOME
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onFindProductClick = { navController.navigate(Screen.Catalog.route) },
                                onFindMachineClick = { navController.navigate(Screen.Map.route) }
                            )
                        }

                        // SCHERMATA MAPPA
                        composable(Screen.Map.route) {
                            MapScreen(
                                onMachineSelected = {
                                    // Esempio: vai al catalogo
                                    navController.navigate(Screen.Catalog.route)
                                }
                            )
                        }

                        // SCHERMATA CATALOGO
                        composable(Screen.Catalog.route) {
                            CatalogScreen(
                                onProductClick = { productId ->
                                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                                }
                            )
                        }

                        // SCHERMATA DETTAGLIO PRODOTTO
                        composable(
                            route = Screen.ProductDetail.route,
                            arguments = listOf(navArgument("productId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId")
                            ProductDetailScreen(productId = productId)
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// PLACEHOLDER SCREENS (Schermate provvisorie per non avere errori)
// Successivamente sposteremo ognuna di queste funzioni nel proprio file in 'ui/screens'
// --------------------------------------------------------------------------



@Composable
fun MapScreen(onMachineSelected: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { onMachineSelected("macchinetta_1") }) {
            Text("Simula selezione Macchinetta")
        }
    }
}

@Composable
fun CatalogScreen(onProductClick: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { onProductClick("calcolatrice_scientifica") }) {
            Text("Vedi Calcolatrice")
        }
    }
}

@Composable
fun ProductDetailScreen(productId: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Dettaglio prodotto: $productId")
    }
}