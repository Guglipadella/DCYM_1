package it.polito.did.dcym.ui.screens.purchase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class PurchaseOptionsUiState(
    val product: Product? = null,
    val machine: Machine? = null,
    val isLoading: Boolean = true,
    val userBalance: Double = 20.00, // Saldo finto per il prototipo
    val maxReturnDate: String = ""   // Data formattata (es. "15 Ott")
)

class PurchaseOptionsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FirebaseRepository()
    private val _uiState = MutableStateFlow(PurchaseOptionsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadData(productId: Int, machineId: String) {
        viewModelScope.launch {
            val products = repository.getProducts().first()
            val machines = repository.getMachines().first()

            val product = products.find { it.id == productId }
            val machine = machines.find { it.id == machineId }

            // Calcolo data scadenza (Oggi + 6 giorni)
            val limitDate = LocalDate.now().plusDays(6)
            val formatter = DateTimeFormatter.ofPattern("dd MMM") // Es. "12 Gen"

            _uiState.value = _uiState.value.copy(
                product = product,
                machine = machine,
                isLoading = false,
                maxReturnDate = limitDate.format(formatter)
            )
        }
    }

    fun getProductImageRes(imageName: String): Int {
        val context = getApplication<Application>().applicationContext
        val id = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        return if (id != 0) id else it.polito.did.dcym.R.drawable.ic_logo_png_dcym
    }
}