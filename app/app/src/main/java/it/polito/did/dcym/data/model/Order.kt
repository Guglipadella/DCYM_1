package it.polito.did.dcym.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val productId: String = "", // Cosa hai comprato
    val machineId: String = "", // Dove lo ritiri
    val pickupCode: String = "", // Il codice numerico (es. "582910") per il suono DTMF
    val status: OrderStatus = OrderStatus.PENDING, // PENDING, COMPLETED, EXPIRED
    val purchaseTimestamp: Long = System.currentTimeMillis(),
    val expirationTimestamp: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 ore da ora
)

enum class OrderStatus {
    PENDING,   // Pagato, in attesa di ritiro (puoi suonare il codice)
    COMPLETED, // Arduino ha aperto, prodotto ritirato
    EXPIRED    // Passate le 24h
}