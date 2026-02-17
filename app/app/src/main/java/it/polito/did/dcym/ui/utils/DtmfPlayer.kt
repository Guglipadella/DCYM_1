package it.polito.did.dcym.ui.utils

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.delay

class DtmfPlayer {
    // Volume al 100% per essere sicuri che Arduino senta
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 90)

    /**
     * Riproduce la sequenza DTMF.
     * @param code La stringa numerica (es. "123456")
     * @param onNotePlayed Callback per aggiornare la UI (opzionale, per effetti grafici)
     */
    suspend fun playSequence(code: String, onNotePlayed: (Int) -> Unit) {
        for ((index, char) in code.withIndex()) {
            val toneType = getToneForChar(char)
            if (toneType != -1) {
                onNotePlayed(index)

                // Verifichiamo se è l'ultimo carattere della sequenza
                val isLast = index == code.length - 1

                // Durata standard 500ms, ma l'ultimo dura 1000ms (1 secondo)
                val duration = if (isLast) 1050 else 600

                // Un micro-delay di sicurezza per stabilizzare l'audio prima di partire
                delay(60)

                // Avviamo il tono
                toneGenerator.startTone(toneType, duration)

                // Aspettiamo che il tono finisca (duration + piccolo margine di 50ms)
                delay(duration.toLong() + 60)

                // Fermiamo il tono esplicitamente
                toneGenerator.stopTone()

                // Pausa tra i toni: se non è l'ultimo, aspettiamo 400ms prima del prossimo
                // Se è l'ultimo, abbiamo finito.
                if (!isLast) {
                    delay(400)
                }
            }
        }
        // Segnala alla UI che la sequenza è finita
        onNotePlayed(-1)
    }

    private fun getToneForChar(c: Char): Int {
        return when (c) {
            '0' -> ToneGenerator.TONE_DTMF_0
            '1' -> ToneGenerator.TONE_DTMF_1
            '2' -> ToneGenerator.TONE_DTMF_2
            '3' -> ToneGenerator.TONE_DTMF_3
            '4' -> ToneGenerator.TONE_DTMF_4
            '5' -> ToneGenerator.TONE_DTMF_5
            '6' -> ToneGenerator.TONE_DTMF_6
            '7' -> ToneGenerator.TONE_DTMF_7
            '8' -> ToneGenerator.TONE_DTMF_8
            '9' -> ToneGenerator.TONE_DTMF_9
            '*' -> ToneGenerator.TONE_DTMF_S
            '#' -> ToneGenerator.TONE_DTMF_P
            else -> -1
        }
    }

    fun release() {
        toneGenerator.release()
    }
}

