package it.polito.did.dcym.ui.screens.confirmation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.data.model.OrderStatus
import it.polito.did.dcym.data.model.OrderType // Assicurati di aver importato l'enum
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
            val machines = repository.getMachines().first()
            val product = products.find { it.id == productId }
            val machine = machines.find { it.id == machineId }

            val limitDate = LocalDate.now().plusDays(6)
            val formatter = DateTimeFormatter.ofPattern("dd MMMM")

            _uiState.update {
                it.copy(
                    product = product,
                    machine = machine,
                    isLoading = false,
                    maxReturnDate = limitDate.format(formatter)
                )
            }
        }
    }

    // --- AGGIORNATO: Ora accetta isRent ---
    fun confirmPurchase(isRent: Boolean, onOrderCreated: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val newOrderId = "ORD-${System.currentTimeMillis()}"
            val uniqueCode = (100000..999999).random().toString()

            // Determiniamo il tipo corretto
            val type = if (isRent) OrderType.RENTAL else OrderType.PURCHASE

            val order = Order(
                id = newOrderId,
                userId = "user_demo", // O l'ID utente reale se presente
                productId = _uiState.value.product?.id.toString(),
                machineId = _uiState.value.machine?.id ?: "unknown",
                pickupCode = uniqueCode,
                type = type, // <--- FONDAMENTALE: Salviamo il tipo corretto
                status = OrderStatus.PENDING,
                purchaseTimestamp = System.currentTimeMillis()
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