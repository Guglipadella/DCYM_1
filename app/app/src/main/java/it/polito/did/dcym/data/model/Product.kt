package it.polito.did.dcym.data.model

import android.content.Context
import com.google.firebase.database.PropertyName

// Enum per le categorie (Rimane uguale)
enum class Category(val label: String) {
    ELETTRONICA("Elettronica"),
    CANCELLERIA("Cancelleria"),
    UTILITA("Utilità")
}

// Classe che mappa ESATTAMENTE il JSON di Firebase
data class Product(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val pricePurchase: Double = 0.0,
    val priceRent: Double? = null,
    val categories: List<String> = emptyList(), // Firebase ci dà List<String>
    val imageResName: String = "", // Es. "img_calcolatrice"

    // Campo locale (non salvato su Firebase) per gestire i preferiti
    @get:PropertyName("ignore_isFavorite") // Dice a Firebase di ignorarlo
    var isFavorite: Boolean = false
) {
    // Funzione Helper per ottenere l'ID della risorsa drawable dal nome stringa
    fun getImageResourceId(context: Context): Int {
        return context.resources.getIdentifier(imageResName, "drawable", context.packageName)
    }

    // Helper per convertire le stringhe delle categorie in Enum veri
    fun getCategoryEnums(): List<Category> {
        return categories.mapNotNull { catString ->
            Category.values().find { it.name == catString }
        }
    }
}