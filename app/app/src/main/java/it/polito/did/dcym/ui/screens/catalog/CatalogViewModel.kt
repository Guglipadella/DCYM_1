package it.polito.did.dcym.ui.screens.catalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.data.repository.FirebaseRepository
import it.polito.did.dcym.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import it.polito.did.dcym.data.model.Category

data class CatalogUiState(
    val products: List<Product> = emptyList(),
    val selectedFilter: CatalogFilter = CatalogFilter.All,
    val searchQuery: String = ""
)

sealed class CatalogFilter(val label: String) {
    data object All : CatalogFilter("Tutte")
    data class ByCategory(val category: Category) : CatalogFilter(category.label)
    data object Favorites : CatalogFilter("Preferiti")
}

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val repository = FirebaseRepository()
    private val favoritesRepo = FavoritesRepository.getInstance(application)

    private var _allProductsFromDb: List<Product> = emptyList()
    private var _favoriteIds: Set<Int> = emptySet()

    init {
        // Ascoltiamo i preferiti
        viewModelScope.launch {
            favoritesRepo.favoriteIds.collect { ids ->
                _favoriteIds = ids
                updateFilteredList()
            }
        }

        // Ascoltiamo i prodotti da Firebase
        viewModelScope.launch {
            repository.getProducts().collect { firebaseProducts ->
                _allProductsFromDb = firebaseProducts
                updateFilteredList()
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFilteredList()
    }

    fun selectFilter(filter: CatalogFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        updateFilteredList()
    }

    fun toggleFavorite(productId: Int) {
        favoritesRepo.toggleFavorite(productId)
    }

    private fun updateFilteredList() {
        val currentState = _uiState.value

        // Aggiungiamo il flag isFavorite ai prodotti
        val productsWithFavorites = _allProductsFromDb.map { product ->
            product.copy(isFavorite = _favoriteIds.contains(product.id))
        }

        var filtered = when (val filter = currentState.selectedFilter) {
            is CatalogFilter.All -> productsWithFavorites
            is CatalogFilter.Favorites -> productsWithFavorites.filter { it.isFavorite }
            is CatalogFilter.ByCategory -> productsWithFavorites.filter { product ->
                product.getCategoryEnums().contains(filter.category)
            }
        }

        if (currentState.searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        _uiState.update { it.copy(products = filtered) }
    }

    fun getProductImage(product: Product): Int {
        val context = getApplication<Application>().applicationContext
        val resId = product.getImageResourceId(context)
        return if (resId != 0) resId else it.polito.did.dcym.R.drawable.ic_logo_png_dcym
    }
}