package it.polito.did.dcym.data.model

data class Machine(
    val id: String = "",
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    // Mappa l'inventario: "ID Prodotto" -> Quantità (es. "1" -> 5)
    val inventory: Map<String, Int> = emptyMap()
) {
    // Funzione helper per sapere quanti pezzi ci sono di un certo prodotto
    fun getStockForProduct(productId: Int): Int {
        return inventory[productId.toString()] ?: 0
    }
}