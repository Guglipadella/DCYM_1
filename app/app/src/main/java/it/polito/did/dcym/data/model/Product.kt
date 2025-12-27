package it.polito.did.dcym.data.model

enum class ProductType {
    PURCHASE_ONLY,
    RENTABLE
}

data class Product(
    val id: String,
    val name: String,
    val imageUrl: String, // URL o risorsa locale per ora
    val pricePurchase: Double,
    val priceRentDaily: Double? = null, // Null se non noleggiabile
    val maxRentDurationDays: Int? = null,
    val type: ProductType,
    val availableAtMachines: List<String> // ID delle macchinette dove si trova
)