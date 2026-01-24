package it.polito.did.dcym.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val machines: List<Machine> = emptyList(),
    val filteredMachines: List<Machine> = emptyList(), // Lista filtrata per la UI
    val searchQuery: String = "", // Testo barra di ricerca
    val isLoading: Boolean = true
)

class MapViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMachines()
    }

    private fun loadMachines() {
        viewModelScope.launch {
            repository.getMachines().collect { list ->
                _uiState.update {
                    it.copy(
                        machines = list,
                        filteredMachines = list, // All'inizio sono uguali
                        isLoading = false
                    )
                }
            }
        }
    }

    // Funzione per filtrare in tempo reale
    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { currentState ->
            val filtered = if (newQuery.isBlank()) {
                currentState.machines
            } else {
                currentState.machines.filter { machine ->
                    machine.name.contains(newQuery, ignoreCase = true)
                    // Se avessimo l'indirizzo nel modello, cercheremmo anche lì
                }
            }
            currentState.copy(searchQuery = newQuery, filteredMachines = filtered)
        }
    }
}