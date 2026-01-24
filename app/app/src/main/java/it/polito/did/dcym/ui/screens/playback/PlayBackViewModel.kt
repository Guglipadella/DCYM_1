package it.polito.did.dcym.ui.screens.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.data.model.OrderStatus
import it.polito.did.dcym.data.repository.FirebaseRepository
import it.polito.did.dcym.ui.utils.DtmfPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val order: Order? = null,
    val timeLeftString: String = "24:00:00",
    val isPlaying: Boolean = false,
    val isCompleted: Boolean = false,
    val currentDigitIndex: Int = -1
)

class PlaybackViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState = _uiState.asStateFlow()
    private val dtmfPlayer = DtmfPlayer()
    private val repository = FirebaseRepository()

    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            // ASCOLTA FIREBASE IN TEMPO REALE
            repository.getOrderFlow(orderId).collect { updatedOrder ->
                if (updatedOrder != null) {
                    _uiState.update {
                        it.copy(
                            order = updatedOrder,
                            // Se lo stato è completed, la UI mostrerà la spunta verde
                            isCompleted = updatedOrder.status == OrderStatus.COMPLETED
                        )
                    }

                    // Aggiorniamo anche il timer se l'ordine è valido
                    if (_uiState.value.timeLeftString == "24:00:00") {
                        startCountdown() // Parte solo la prima volta
                    }
                }
            }
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            while (true) {
                val order = _uiState.value.order ?: break
                val now = System.currentTimeMillis()
                val diff = order.expirationTimestamp - now

                if (diff <= 0) {
                    _uiState.update { it.copy(timeLeftString = "SCADUTO") }
                    break
                }
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff / (1000 * 60)) % 60
                val seconds = (diff / 1000) % 60
                _uiState.update {
                    it.copy(timeLeftString = "%02d:%02d:%02d".format(hours, minutes, seconds))
                }
                delay(1000)
            }
        }
    }


    fun playCodeSound() {
        val code = _uiState.value.order?.pickupCode ?: return
        if (_uiState.value.isPlaying) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPlaying = true) }
            dtmfPlayer.playSequence(code) { idx ->
                _uiState.update { it.copy(currentDigitIndex = idx) }
            }
            _uiState.update { it.copy(isPlaying = false, currentDigitIndex = -1) }
        }
    }
}