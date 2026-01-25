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
    val activeRentals: List<Order> = emptyList(), // La lista filtrata
    val filteredRentals: List<Order> = emptyList(), // Lista filtrata anche dalla ricerca
    val userName: String = "Guglielmo Padella", // O nome utente reale
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
            // Recupera gli ordini
            val allOrders = repository.getOrders().first()

            // Filtra SOLO i noleggi attivi o in attesa di rimborso
            val activeList = allOrders.filter { order ->
                order.isRent && (order.isOngoing || order.isPendingRefund)
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