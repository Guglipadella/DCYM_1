package it.polito.did.dcym.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Map : Screen("map")
    object Catalog : Screen("catalog")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object Checkout : Screen("checkout")
    object ReturnProcess : Screen("return")
}