package it.polito.did.dcym.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val machineId: String = "",
    val pickupCode: String = "",
    val type: OrderType = OrderType.PURCHASE, // <--- NUOVO CAMPO: Distingue acquisto/noleggio
    val status: OrderStatus = OrderStatus.PENDING,
    val purchaseTimestamp: Long = System.currentTimeMillis(),
    val expirationTimestamp: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
)

enum class OrderType {
    PURCHASE, // Acquisto standard
    RENTAL    // Noleggio
}

enum class OrderStatus {
    PENDING,   // Pagato, in attesa di ritiro allo sportello
    ONGOING,   // (Solo Noleggi) Ritirato, in uso dall'utente, non ancora restituito
    COMPLETED, // Transazione finita (Acquisto ritirato o Noleggio restituito)
    EXPIRED    // Scaduto (non ritirato in tempo)
}