package it.polito.did.dcym.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.data.repository.FirebaseRepository
import it.polito.did.dcym.ui.utils.DtmfPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val pendingPickupOrders: List<Order> = emptyList(),
    val activeRentals: List<Order> = emptyList(),
    val pendingRefundOrders: List<Order> = emptyList(),
    val machineNames: Map<String, String> = emptyMap(),
    val userName: String = "Guglielmo Padella",
    val userBalance: Double = 20.0,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isPlayingSound: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val dtmfPlayer = DtmfPlayer() // DtmfPlayer non richiede context
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            // Recuperiamo i nomi delle macchinette dal repository
            val machines = repository.getMachines().first()
            val machineMap = machines.associate { it.id to it.name }

            repository.getOrders().collect { allOrders ->
                _uiState.update { state ->
                    state.copy(
                        pendingPickupOrders = allOrders.filter { it.status == "PENDING" },
                        activeRentals = allOrders.filter { it.status == "ONGOING" && it.isRent }, // rent = true
                        pendingRefundOrders = allOrders.filter { it.status == "PENDING_REFUND" },
                        machineNames = machineMap,
                        isLoading = false
                    )
                }
            }
        }
    }

    // Copiato esattamente da HistoryViewModel.kt
    fun playSound(code: String) {
        if (_uiState.value.isPlayingSound) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPlayingSound = true) }
            dtmfPlayer.playSequence(code) { /* no-op */ }
            _uiState.update { it.copy(isPlayingSound = false) }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    data class RefundRow(val label: String, val amount: Double)

    fun getFutureRefundOptions(order: Order): List<RefundRow> {
        val percentages = listOf(0.80, 0.72, 0.64, 0.56, 0.48, 0.40)
        val msPerDay = 24L * 60 * 60 * 1000
        val now = System.currentTimeMillis()
        val daysElapsed = ((now - order.purchaseTimestamp) / msPerDay).toInt().coerceAtLeast(0)

        val rows = mutableListOf<RefundRow>()
        percentages.forEachIndexed { index, percent ->
            if (index >= daysElapsed) {
                val amount = order.totalCost * percent
                val dayNum = index + 1
                val label = if (index == daysElapsed) "Oggi (Entro 24h)" else "Entro il $dayNum° giorno"
                rows.add(RefundRow(label, amount))
            }
        }
        return rows
    }
}