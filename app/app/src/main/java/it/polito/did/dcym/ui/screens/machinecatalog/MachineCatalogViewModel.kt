package it.polito.did.dcym.ui.screens.machinecatalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.data.repository.FirebaseRepository
import it.polito.did.dcym.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class MachineCatalogFilter {
    object All : MachineCatalogFilter()
    data class ByCategory(val category: String) : MachineCatalogFilter()
    object Favorites : MachineCatalogFilter()
}

data class MachineCatalogUiState(
    val machineName: String = "",
    val allProductsInMachine: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val selectedFilter: MachineCatalogFilter = MachineCatalogFilter.All,
    val searchQuery: String = "",
    val userBalance: Double = 20.00,
    val isLoading: Boolean = true,
    val hasActiveRentals: Boolean = false
)

class MachineCatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FirebaseRepository()
    private val favoritesRepo = FavoritesRepository.getInstance(application)

    init {
        viewModelScope.launch {
            repository.getOrders().collect { allOrders ->
                val hasActive = allOrders.any { it.status == "ONGOING" && it.isRent }
                _uiState.update { it.copy(hasActiveRentals = hasActive) }
            }
        }
    }
    private val _uiState = MutableStateFlow(MachineCatalogUiState())
    val uiState = _uiState.asStateFlow()

    private var _favoriteIds: Set<Int> = emptySet()

    init {
        // Ascoltiamo i preferiti
        viewModelScope.launch {
            favoritesRepo.favoriteIds.collect { ids ->
                _favoriteIds = ids
                updateFiltered()
            }
        }
    }

    fun loadData(machineId: String) {
        viewModelScope.launch {
            val machines = repository.getMachines().first()
            val machine = machines.find { it.id == machineId }

            if (machine == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val allProducts = repository.getProducts().first()

            val productsInThisMachine = allProducts.filter { product ->
                machine.getStockForProduct(product.id) > 0
            }.map { product ->
                product.copy(isFavorite = _favoriteIds.contains(product.id))
            }

            _uiState.update {
                it.copy(
                    machineName = machine.name,
                    allProductsInMachine = productsInThisMachine,
                    filteredProducts = productsInThisMachine,
                    isLoading = false
                )
            }
        }
    }

    fun selectFilter(filter: MachineCatalogFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        updateFiltered()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFiltered()
    }

    fun toggleFavorite(productId: Int) {
        favoritesRepo.toggleFavorite(productId)
    }

    private fun updateFiltered() {
        _uiState.update { state ->
            // Aggiorniamo lo stato dei preferiti
            val updatedAll = state.allProductsInMachine.map { product ->
                product.copy(isFavorite = _favoriteIds.contains(product.id))
            }

            val filtered = filterProducts(updatedAll, state.selectedFilter, state.searchQuery)

            state.copy(
                allProductsInMachine = updatedAll,
                filteredProducts = filtered
            )
        }
    }

    private fun filterProducts(
        products: List<Product>,
        filter: MachineCatalogFilter,
        query: String
    ): List<Product> {
        var result = products

        result = when (filter) {
            is MachineCatalogFilter.All -> result
            is MachineCatalogFilter.Favorites -> result.filter { it.isFavorite }
            is MachineCatalogFilter.ByCategory -> result.filter { product ->
                product.categories.any { it.equals(filter.category, ignoreCase = true) }
            }
        }

        if (query.isNotEmpty()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }
        }

        return result
    }
}