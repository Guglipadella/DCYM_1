package it.polito.did.dcym.data.model

import com.google.firebase.database.PropertyName

data class Order(
    val id: String = "",
    val userId: String = "",
    val productId: Int = 0,
    val machineId: String = "",
    val pickupCode: String = "",

    // INFO PRODOTTO (Denormalizzate per facilità)
    val productName: String = "",

    // ⚠️ IMPORTANTE: Firebase salva come "rent" ma in Kotlin usiamo "isRent"
    @get:PropertyName("rent")
    @set:PropertyName("rent")
    var isRent: Boolean = false,   // TRUE = è un noleggio, FALSE = acquisto

    // INFO TEMPORALI E COSTI
    val purchaseTimestamp: Long = 0,
    val totalCost: Double = 0.0,

    // STATO: "PENDING", "ONGOING", "RETURNED", "PENDING_REFUND", "COMPLETED"
    val status: String = "PENDING"
)