package it.polito.did.dcym.ui.screens.confirmation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.data.model.OrderStatus
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.data.repository.FirebaseRepository
import kotlinx.coroutines.delay
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

    // --- NUOVA FUNZIONE: Crea l'ordine e restituisce l'ID ---
    fun confirmPurchase(onOrderCreated: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Generiamo i dati
            val newOrderId = "ORD-${System.currentTimeMillis()}"
            val uniqueCode = (100000..999999).random().toString() // Genera 6 cifre per il suono

            val order = Order(
                id = newOrderId,
                productId = _uiState.value.product?.id.toString(),
                machineId = _uiState.value.machine?.id ?: "unknown",
                pickupCode = uniqueCode,
                status = OrderStatus.PENDING
            )

            // SALVATAGGIO VERO SU FIREBASE
            val success = repository.saveOrder(order)

            _uiState.update { it.copy(isLoading = false) }

            if (success) {
                onOrderCreated(newOrderId)
            } else {
                // Gestisci errore se vuoi (es. Toast)
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