package it.polito.did.dcym.ui.screens.machinedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MachineDetailUiState(
    val machine: Machine? = null,
    val isLoading: Boolean = true
)

class MachineDetailViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val _uiState = MutableStateFlow(MachineDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadMachine(machineId: String) {
        viewModelScope.launch {
            val machines = repository.getMachines().first()
            val found = machines.find { it.id == machineId }
            _uiState.update {
                it.copy(machine = found, isLoading = false)
            }
        }
    }
}