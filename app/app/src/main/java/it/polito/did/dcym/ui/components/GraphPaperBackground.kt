package it.polito.did.dcym.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun GraphPaperBackground(content: @Composable BoxScope.() -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    val gridMinor = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
    val gridMajor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(bg)

            val step = 18.dp.toPx()
            val majorStep = step * 5

            var x = 0f
            while (x <= size.width) {
                val isMajor = (x % majorStep) < 1f
                drawLine(
                    color = if (isMajor) gridMajor else gridMinor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = if (isMajor) 1.5f else 1f
                )
                x += step
            }

            var y = 0f
            while (y <= size.height) {
                val isMajor = (y % majorStep) < 1f
                drawLine(
                    color = if (isMajor) gridMajor else gridMinor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (isMajor) 1.5f else 1f
                )
                y += step
            }
        }

        content()
    }
}
