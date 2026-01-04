package it.polito.did.dcym.ui.screens.catalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Importiamo la Category dal file model
import it.polito.did.dcym.data.model.Category

// (Le classi CatalogUiState e CatalogFilter rimangono uguali a prima)
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

    // Istanza del Repository
    private val repository = FirebaseRepository()

    // Lista completa scaricata da Firebase
    private var _allProductsFromDb: List<Product> = emptyList()

    init {
        // Avviamo l'ascolto dei dati da Firebase
        viewModelScope.launch {
            repository.getProducts().collect { firebaseProducts ->
                _allProductsFromDb = firebaseProducts
                updateFilteredList() // Aggiorna la UI
            }
        }
    }

    // ... IL RESTO DELLE FUNZIONI (selectFilter, onSearchQueryChange, updateFilteredList) ...
    // ... RIMANGONO QUASI UGUALI, MA USANO _allProductsFromDb INVECE DI MockDatabase ...

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFilteredList()
    }

    fun selectFilter(filter: CatalogFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        updateFilteredList()
    }

    fun toggleFavorite(productId: Int) {
        // Nota: Qui stiamo aggiornando solo in locale per la UI.
        // Se volessi salvare i preferiti su Firebase o SharedPreferences, lo faresti qui.
        _allProductsFromDb = _allProductsFromDb.map {
            if (it.id == productId) it.copy(isFavorite = !it.isFavorite) else it
        }
        updateFilteredList()
    }

    private fun updateFilteredList() {
        val currentState = _uiState.value

        var filtered = when (val filter = currentState.selectedFilter) {
            is CatalogFilter.All -> _allProductsFromDb
            is CatalogFilter.Favorites -> _allProductsFromDb.filter { it.isFavorite }
            is CatalogFilter.ByCategory -> _allProductsFromDb.filter { product ->
                // Usiamo il metodo helper per controllare le categorie
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

    // Helper per la UI per ottenere l'immagine corretta
    fun getProductImage(product: Product): Int {
        // getApplication<Application>() ci dÃ  il contesto sicuro
        val context = getApplication<Application>().applicationContext
        val resId = product.getImageResourceId(context)
        // Se non trova l'immagine (ritorna 0), usa un placeholder o l'icona del logo
        return if (resId != 0) resId else it.polito.did.dcym.R.drawable.ic_logo_png_dcym // O un'immagine di default
    }
}