package it.polito.did.dcym.ui.utils

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.delay

class DtmfPlayer {
    // Volume al 100% per essere sicuri che Arduino senta
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    /**
     * Riproduce la sequenza DTMF.
     * @param code La stringa numerica (es. "123456")
     * @param onNotePlayed Callback per aggiornare la UI (opzionale, per effetti grafici)
     */
    suspend fun playSequence(code: String, onNotePlayed: (Int) -> Unit) {
        for ((index, char) in code.withIndex()) {
            val toneType = getToneForChar(char)
            if (toneType != -1) {
                onNotePlayed(index) // Notifica quale numero sta suonando
                toneGenerator.startTone(toneType, 150) // Suona per 150ms
                delay(150) // Aspetta che finisca il suono
                toneGenerator.stopTone()
                delay(100) // Pausa di 100ms tra un tono e l'altro
            }
        }
        onNotePlayed(-1) // Reset alla fine
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

