package it.polito.did.dcym.ui.screens.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: Product? = null,
    val availableMachines: List<Machine> = emptyList(),
    val isLoading: Boolean = true,
    val hasActiveRentals: Boolean = false
)

class ProductDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseRepository()
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState = _uiState.asStateFlow()
    init {
        viewModelScope.launch {
            repository.getOrders().collect { allOrders ->
                val hasActive = allOrders.any { it.status == "ONGOING" && it.isRent }
                _uiState.update { it.copy(hasActiveRentals = hasActive) }
            }
        }
    }
    fun loadProductData(productId: Int) {
        println("DEBUG: Caricamento dati per prodotto ID: $productId")
        viewModelScope.launch {
            combine(
                repository.getProducts(),
                repository.getMachines()
            ) { products, machines ->
                println("DEBUG: Dati ricevuti da Firebase. Prodotti: ${products.size}, Macchinette: ${machines.size}")

                // 1. Trova il prodotto giusto
                val foundProduct = products.find { it.id == productId }
                if (foundProduct == null) {
                    println("DEBUG: ERRORE - Prodotto con ID $productId non trovato nella lista!")
                } else {
                    println("DEBUG: Prodotto trovato: ${foundProduct.name}")
                }

                // 2. Filtra le macchinette
                val validMachines = machines.filter { machine ->
                    val stock = machine.getStockForProduct(productId)
                    println("DEBUG: Controllo macchinetta '${machine.name}' (ID: ${machine.id}). Stock per prodotto $productId: $stock")
                    stock > 0
                }

                println("DEBUG: Macchinette valide trovate: ${validMachines.size}")
                Pair(foundProduct, validMachines)

            }.collect { (product, machines) ->
                _uiState.update { current ->
                    current.copy(
                        product = product,
                        availableMachines = machines,
                        isLoading = false
                        // hasActiveRentals rimane quello calcolato dall'init
                    )
                }
            }
        }
    }

    fun getProductImageRes(imageName: String): Int {
        val context = getApplication<Application>().applicationContext
        val id = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        return if (id != 0) id else it.polito.did.dcym.R.drawable.ic_logo_png_dcym
    }
}