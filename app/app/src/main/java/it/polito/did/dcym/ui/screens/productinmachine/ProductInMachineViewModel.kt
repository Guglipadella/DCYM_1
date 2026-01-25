package it.polito.did.dcym.ui.screens.productinmachine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductInMachineUiState(
    val product: Product? = null,
    val machineId: String = "",
    val userBalance: Double = 20.00, // Saldo mock
    val isLoading: Boolean = true
)

class ProductInMachineViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val _uiState = MutableStateFlow(ProductInMachineUiState())
    val uiState = _uiState.asStateFlow()

    fun loadData(productId: Int, machineId: String) {
        viewModelScope.launch {
            // Carica tutti i prodotti e trova quello giusto
            val products = repository.getProducts().first()
            val foundProduct = products.find { it.id == productId }

            _uiState.update {
                it.copy(
                    product = foundProduct,
                    machineId = machineId,
                    isLoading = false
                )
            }
        }
    }
}