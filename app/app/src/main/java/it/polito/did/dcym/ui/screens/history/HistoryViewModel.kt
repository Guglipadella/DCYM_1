package it.polito.did.dcym.ui.screens.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.data.model.OrderStatus
import it.polito.did.dcym.data.repository.FirebaseRepository
import it.polito.did.dcym.ui.utils.DtmfPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Modello UI per visualizzare l'ordine arricchito coi nomi
data class HistoryItem(
    val order: Order,
    val productName: String,
    val machineName: String,
    val productImageRes: String,
    val price: Double
)

enum class HistoryFilter { TUTTI, IN_CORSO, COMPLETATI }

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val filteredItems: List<HistoryItem> = emptyList(),
    val filter: HistoryFilter = HistoryFilter.IN_CORSO, // Default su "In Corso" come da PDF
    val isLoading: Boolean = true,
    val isPlayingSound: Boolean = false
)

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
            // Combiniamo 3 flussi di dati: Ordini, Prodotti, Macchinette
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
                _uiState.update {
                    it.copy(
                        items = fullList,
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

    private fun applyFilter() {
        val current = _uiState.value
        val filtered = when (current.filter) {
            HistoryFilter.TUTTI -> current.items
            HistoryFilter.IN_CORSO -> current.items.filter { it.order.status == OrderStatus.PENDING }
            HistoryFilter.COMPLETATI -> current.items.filter { it.order.status == OrderStatus.COMPLETED }
        }
        _uiState.update { it.copy(filteredItems = filtered) }
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