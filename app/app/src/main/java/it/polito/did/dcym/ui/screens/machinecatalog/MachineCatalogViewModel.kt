package it.polito.did.dcym.ui.screens.machinecatalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MachineCatalogUiState(
    val machineName: String = "",
    val allProductsInMachine: List<Product> = emptyList(), // Tutti i prodotti disponibili qui
    val filteredProducts: List<Product> = emptyList(),     // Quelli filtrati per categoria
    val selectedCategory: String = "Tutti",
    val isLoading: Boolean = true
)

class MachineCatalogViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val _uiState = MutableStateFlow(MachineCatalogUiState())
    val uiState = _uiState.asStateFlow()

    fun loadData(machineId: String) {
        viewModelScope.launch {
            // 1. Scarichiamo tutte le macchinette e troviamo quella giusta
            val machines = repository.getMachines().first()
            val machine = machines.find { it.id == machineId }

            if (machine == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            // 2. Scarichiamo tutti i prodotti
            val allProducts = repository.getProducts().first()

            // 3. FILTRO: Teniamo solo i prodotti che hanno stock > 0 in QUESTA macchinetta
            val productsInThisMachine = allProducts.filter { product ->
                machine.getStockForProduct(product.id) > 0
            }

            _uiState.update {
                it.copy(
                    machineName = machine.name,
                    allProductsInMachine = productsInThisMachine,
                    filteredProducts = productsInThisMachine, // All'inizio mostriamo tutto
                    isLoading = false
                )
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            val filtered = if (category == "Tutti") {
                state.allProductsInMachine
            } else {
                state.allProductsInMachine.filter { it.categories.contains(category) }
            }
            state.copy(selectedCategory = category, filteredProducts = filtered)
        }
    }
}

