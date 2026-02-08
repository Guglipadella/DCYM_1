package it.polito.did.dcym.ui.screens.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HelpUiState(
    val hasActiveRentals: Boolean = false,
    val faqs: List<FAQItem> = emptyList()
)

data class FAQItem(
    val question: String,
    val answer: String,
    val isExpanded: Boolean = false
)

class HelpViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val _uiState = MutableStateFlow(HelpUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFaqs()
        observeActiveRentals()
    }

    private fun loadFaqs() {
        val faqList = listOf(
            FAQItem(
                "Come funziona Don’t Call Your Mom?",
                "Don’t Call Your Mom ti permette di acquistare o noleggiare oggetti di uso quotidiano direttamente dai distributori automatici presenti nelle aule. Partendo dalla ricerca dei prodotti o delle macchinette."
            ),
            FAQItem(
                "Posso noleggiare tutti i prodotti?",
                "No. Solo i prodotti contrassegnati come “Noleggiabile” possono essere presi a noleggio. Gli altri sono acquistabili una sola volta."
            ),
            FAQItem(
                "Come pago un prodotto?",
                "Il pagamento avviene direttamente dall’app utilizzando il tuo saldo. Puoi ricaricare il saldo in qualsiasi momento dal profilo."
            ),
            FAQItem(
                "Dove trovo il codice per il ritiro?",
                "Dopo l’acquisto o il noleggio, il codice viene mostrato subito a schermo. Puoi trovarlo di nuovo anche nella sezione Acquisti."
            )
        )
        _uiState.update { it.copy(faqs = faqList) }
    }

    private fun observeActiveRentals() {
        viewModelScope.launch {
            repository.getOrders().collect { allOrders ->
                val hasActive = allOrders.any { it.status == "ONGOING" && it.isRent }
                _uiState.update { it.copy(hasActiveRentals = hasActive) }
            }
        }
    }

    fun toggleFaq(index: Int) {
        _uiState.update { state ->
            val updatedFaqs = state.faqs.mapIndexed { i, faq ->
                if (i == index) faq.copy(isExpanded = !faq.isExpanded) else faq
            }
            state.copy(faqs = updatedFaqs)
        }
    }
}