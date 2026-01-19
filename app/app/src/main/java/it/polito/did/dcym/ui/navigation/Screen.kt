package it.polito.did.dcym.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Map : Screen("map")
    object Catalog : Screen("catalog")
    object Profile : Screen("profile")

    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    data object PurchaseOptions : Screen("purchase_options/{productId}/{machineId}") {
        fun createRoute(productId: String, machineId: String) = "purchase_options/$productId/$machineId"
    }
    data object Confirmation : Screen("confirm/{pId}/{mId}/{isRent}") {
        fun createRoute(pId: String, mId: String, isRent: Boolean) = "confirm/$pId/$mId/$isRent"
    }
    object Checkout : Screen("checkout")
    object ReturnProcess : Screen("return")
}