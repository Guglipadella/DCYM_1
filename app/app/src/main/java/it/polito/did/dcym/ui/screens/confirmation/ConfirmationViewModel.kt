package it.polito.did.dcym.ui.screens.confirmation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ConfirmationUiState(
    val product: Product? = null,
    val machine: Machine? = null,
    val isLoading: Boolean = true,
    val userBalance: Double = 20.00,
    val maxReturnDate: String = ""
)

class ConfirmationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FirebaseRepository()
    private val _uiState = MutableStateFlow(ConfirmationUiState())
    val uiState = _uiState.asStateFlow()

    fun loadData(productId: Int, machineId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val products = repository.getProducts().first()
            val product = products.find { it.id == productId }

            val machines = repository.getMachines().first()
            val machine = machines.find { it.id == machineId }

            // Calcolo data restituzione (Mock: +1 giorno)
            val tomorrow = LocalDate.now().plusDays(1)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dateStr = tomorrow.format(formatter)

            _uiState.update {
                it.copy(
                    product = product,
                    machine = machine,
                    maxReturnDate = dateStr,
                    isLoading = false
                )
            }
        }
    }

    fun confirmPurchase(isRent: Boolean, onOrderCreated: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentState = _uiState.value
            val product = currentState.product
            val machine = currentState.machine

            if (product == null || machine == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val newOrderId = "ORD-${System.currentTimeMillis()}"
            val uniqueCode = (100000..999999).random().toString()

            // Calcolo costo
            val cost = if (isRent) (product.priceRent ?: 0.0) else product.pricePurchase

            // --- CREAZIONE ORDINE CORRETTA ---
            val order = Order(
                id = newOrderId,
                userId = "user_demo", // ID Utente mock
                productId = product.id, // Ora è un Int
                machineId = machine.id,
                pickupCode = uniqueCode,

                // Nuovi campi richiesti
                productName = product.name,
                isRent = isRent,

                purchaseTimestamp = System.currentTimeMillis(),
                totalCost = cost,

                // Stato come stringa
                status = "PENDING"

                // RIMOSSI: expirationTimestamp (calcolato al volo), type (usiamo isRent)
            )

            val success = repository.saveOrder(order)

            _uiState.update { it.copy(isLoading = false) }

            if (success) {
                onOrderCreated(newOrderId)
            } else {
                Log.e("ConfirmationVM", "Errore salvataggio ordine")
            }
        }
    }

    fun getProductImageRes(imageName: String): Int {
        val context = getApplication<Application>().applicationContext
        val id = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        return if (id != 0) id else it.polito.did.dcym.R.drawable.ic_logo_png_dcym
    }
}