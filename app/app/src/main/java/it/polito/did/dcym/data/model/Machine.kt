package it.polito.did.dcym.data.model

data class Machine(
    val id: String = "",
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val inventory: Map<String, Int> = emptyMap(),

    // --- NUOVI CAMPI ---
    val sede: String = "",
    val edificio: String = "",
    val piano: String = "",
    val indirizzo: String = ""
) {
    fun getStockForProduct(productId: Int): Int {
        return inventory[productId.toString()] ?: 0
    }
}