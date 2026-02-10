package it.polito.did.dcym.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Order
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
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isPlayingSound: Boolean = false
)

// Filtri disponibili nella UI
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
                    val prod = products.find { it.id.toString() == order.productId.toString() }
                    val mach = machines.find { it.id == order.machineId }

                    HistoryItem(
                        order = order,
                        productName = prod?.name ?: "Prodotto sconosciuto",
                        productImageRes = prod?.imageResName ?: "",
                        machineName = mach?.name ?: "Macchinetta sconosciuta",
                        price = order.totalCost
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
                applyFilter()
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

    private fun applyFilter() {
        val current = _uiState.value

        // Prima filtriamo per Tab
        val tabFiltered = when (current.filter) {
            HistoryFilter.IN_CORSO -> {
                val now = System.currentTimeMillis()
                val oneDayMs = 24L * 60 * 60 * 1000

                current.items.filter { item ->
                    val status = item.order.status
                    val isPending = status == "PENDING"
                    val isOngoing = status == "ONGOING"
                    val isReturned = status == "RETURNED" // Nuovo stato Arduino

                    // Includiamo PENDING, ONGOING e RETURNED negli ordini "In Corso"
                    if (!isPending && !isOngoing && !isReturned) return@filter false

                    // Calcolo scadenza: 24h per ritiro (PENDING), 6gg per restituzione
                    val expirationTime = if (isPending) {
                        item.order.purchaseTimestamp + oneDayMs
                    } else {
                        item.order.purchaseTimestamp + (6 * oneDayMs)
                    }

                    now < expirationTime
                }.sortedWith(
                    // Ordiniamo: prima i noleggi attivi o pronti per il termine (ONGOING/RETURNED)
                    compareByDescending<HistoryItem> {
                        (it.order.status == "ONGOING" || it.order.status == "RETURNED") && it.order.isRent
                    }.thenByDescending { it.order.purchaseTimestamp }
                )
            }
            HistoryFilter.TUTTI -> current.items
            HistoryFilter.NOLEGGI -> current.items.filter {
                it.order.isRent  // Solo i noleggi
            }
            HistoryFilter.ACQUISTI -> current.items.filter {
                !it.order.isRent  // Solo gli acquisti
            }
        }

        // Poi filtriamo per la barra di ricerca
        val query = current.searchQuery.trim().lowercase()
        val finalFiltered = if (query.isEmpty()) {
            tabFiltered
        } else {
            tabFiltered.filter {
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
    fun completeReturn(orderId: String) {
        repository.updateOrderStatus(orderId, "PENDING_REFUND")
    }
}