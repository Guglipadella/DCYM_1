package it.polito.did.dcym.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.data.model.OrderStatus
import it.polito.did.dcym.data.model.OrderType
import it.polito.did.dcym.data.repository.FirebaseRepository
import it.polito.did.dcym.ui.utils.DtmfPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryItem(
    val order: Order,
    val productName: String,
    val machineName: String,
    val productImageRes: String,
    val price: Double
)

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val filteredItems: List<HistoryItem> = emptyList(),
    val filter: HistoryFilter = HistoryFilter.IN_CORSO,
    val searchQuery: String = "", // <--- NUOVO CAMPO
    val isLoading: Boolean = true,
    val isPlayingSound: Boolean = false
)
// Definiamo i nuovi filtri richiesti
enum class HistoryFilter { IN_CORSO, TUTTI, NOLEGGI, ACQUISTI }


class HistoryViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()
    private val dtmfPlayer = DtmfPlayer()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            combine(
                repository.getOrders(),
                repository.getProducts(),
                repository.getMachines()
            ) { orders, products, machines ->
                orders.map { order ->
                    val prod = products.find { it.id.toString() == order.productId }
                    val mach = machines.find { it.id == order.machineId }

                    HistoryItem(
                        order = order,
                        productName = prod?.name ?: "Prodotto sconosciuto",
                        productImageRes = prod?.imageResName ?: "",
                        machineName = mach?.name ?: "Macchinetta sconosciuta",
                        price = prod?.pricePurchase ?: 0.0
                    )
                }
            }.collect { fullList ->
                // Ordiniamo per data decrescente (i più recenti in alto)
                val sortedList = fullList.sortedByDescending { it.order.purchaseTimestamp }
                _uiState.update {
                    it.copy(
                        items = sortedList,
                        isLoading = false
                    )
                }
                applyFilter() // Riapplica il filtro appena arrivano nuovi dati
            }
        }
    }

    fun setFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(filter = filter) }
        applyFilter()
    }

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        applyFilter()
    }

    // 3. Aggiorna applyFilter per includere la logica di ricerca
    private fun applyFilter() {
        val current = _uiState.value

        // Prima filtriamo per Tab (logica che abbiamo fatto prima)
        val tabFiltered = when (current.filter) {
            HistoryFilter.IN_CORSO -> current.items.filter {
                it.order.status == OrderStatus.PENDING || it.order.status == OrderStatus.ONGOING
            }
            HistoryFilter.TUTTI -> current.items
            HistoryFilter.NOLEGGI -> current.items.filter { it.order.type == OrderType.RENTAL }
            HistoryFilter.ACQUISTI -> current.items.filter { it.order.type == OrderType.PURCHASE }
        }

        // Poi filtriamo per la barra di ricerca (se c'è testo)
        val query = current.searchQuery.trim().lowercase()
        val finalFiltered = if (query.isEmpty()) {
            tabFiltered
        } else {
            tabFiltered.filter {
                // Cerchiamo nel nome del prodotto O nel nome della macchinetta
                it.productName.lowercase().contains(query) ||
                        it.machineName.lowercase().contains(query)
            }
        }

        _uiState.update { it.copy(filteredItems = finalFiltered) }
    }
    fun playSound(code: String) {
        if (_uiState.value.isPlayingSound) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPlayingSound = true) }
            dtmfPlayer.playSequence(code) { }
            _uiState.update { it.copy(isPlayingSound = false) }
        }
    }
}