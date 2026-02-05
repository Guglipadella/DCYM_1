package it.polito.did.dcym.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val activeRentals: List<Order> = emptyList(), // Lista completa attiva
    val filteredRentals: List<Order> = emptyList(), // Lista filtrata dalla ricerca
    val userName: String = "Guglielmo Padella",
    val userBalance: Double = 20.0,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class ProfileViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            // 1. Scarica tutti gli ordini
            val allOrders = repository.getOrders().first()

            // 2. FILTRO RIGIDO PER IL PROFILO
            // Mostra solo se è un Noleggio (isRent) E se lo stato è ONGOING o PENDING_REFUND
            val activeList = allOrders.filter { order ->
                order.isRent && (
                        order.status.equals("ONGOING", ignoreCase = true) ||
                                order.status.equals("PENDING_REFUND", ignoreCase = true)
                        )
            }

            _uiState.update {
                it.copy(
                    activeRentals = activeList,
                    filteredRentals = activeList,
                    isLoading = false
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.activeRentals
            } else {
                state.activeRentals.filter {
                    it.productName.contains(query, ignoreCase = true)
                }
            }
            state.copy(searchQuery = query, filteredRentals = filtered)
        }
    }
}