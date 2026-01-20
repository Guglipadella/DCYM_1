package it.polito.did.dcym.ui.navigation

sealed class Screen(val route: String) {

    // ✅ Root tabs / sezioni principali
    data object Home : Screen("home_choice") // schermata "Ciao! Cosa vuoi fare?"
    data object Catalog : Screen("catalog")
    data object Map : Screen("map")
    data object Profile : Screen("profile")
    data object History : Screen("history")
    data object Help : Screen("help")

    // ✅ Flow prodotto
    data object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }

    data object PurchaseOptions : Screen("purchase_options/{productId}/{machineId}") {
        fun createRoute(productId: String, machineId: String) =
            "purchase_options/$productId/$machineId"
    }

    data object Confirmation : Screen("confirm/{pId}/{mId}/{isRent}") {
        fun createRoute(pId: String, mId: String, isRent: Boolean) =
            "confirm/$pId/$mId/$isRent"
    }

    // ✅ (Se li userai davvero)
    data object Checkout : Screen("checkout")
    data object ReturnProcess : Screen("return")
}
