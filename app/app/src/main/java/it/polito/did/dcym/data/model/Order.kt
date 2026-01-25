package it.polito.did.dcym.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val machineId: String = "",
    val pickupCode: String = "",
    val productName: String = "",
    val status: String = "PENDING",  // String puro
    val type: String = "PURCHASE",   // String puro
    val purchaseTimestamp: Long = System.currentTimeMillis(),
    val expirationTimestamp: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
) {
    // Helper properties per facilitare i controlli
    val isRent: Boolean
        get() = type.equals("RENTAL", ignoreCase = true)

    val isOngoing: Boolean
        get() = status.equals("ONGOING", ignoreCase = true)

    val isPendingRefund: Boolean
        get() = status.equals("PENDING_REFUND", ignoreCase = true)

    val isPending: Boolean
        get() = status.equals("PENDING", ignoreCase = true)

    val isCompleted: Boolean
        get() = status.equals("COMPLETED", ignoreCase = true)

    val isExpired: Boolean
        get() = status.equals("EXPIRED", ignoreCase = true)
}