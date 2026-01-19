package it.polito.did.dcym.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.polito.did.dcym.R

@Composable
fun HomeScreen(
    onFindProductClick: () -> Unit,
    onFindMachineClick: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    GraphPaperBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(10.dp))

            // LOGO (ora si chiama "logo")
            Icon(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Don't Call Your Mom",
                tint = Color.Unspecified,
                modifier = Modifier.size(200.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Ciao! Cosa vuoi fare?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Scegli il percorso: puoi cambiare quando vuoi.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            // Le tre card (spaziatura uniforme)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                PathCard(
                    title = "Trova un prodotto",
                    subtitle = "Sfoglia il catalogo e salva i preferiti",
                    iconRes = R.drawable.ic_store,
                    onClick = onFindProductClick
                )

                PathCard(
                    title = "Trova una macchinetta",
                    subtitle = "Vedi i distributori e la disponibilità",
                    iconRes = R.drawable.ic_distributori,
                    onClick = onFindMachineClick
                )

                PathCard(
                    title = "Modifica profilo",
                    subtitle = "Nome, avatar e preferenze",
                    iconRes = R.drawable.ic_profile,
                    onClick = onEditProfileClick
                )
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

/* -----------------------------
   CARD “STICKER” PER IL PERCORSO
   ----------------------------- */
@Composable
private fun PathCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val accent = MaterialTheme.colorScheme.secondary // giallo pastello

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = paper),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 30.dp) // bassa, “sticker”
            .border(2.dp, outline, RoundedCornerShape(22.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()          // <-- FIX: NON fillMaxSize()
                .wrapContentHeight()     // <-- FIX: altezza naturale
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // bollino icona
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(8.dp, CircleShape)
                    .background(accent, CircleShape)
                    .border(2.dp, outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }

        }
    }
}

/* -----------------------------
   BACKGROUND A QUADRETTI
   ----------------------------- */
@Composable
private fun GraphPaperBackground(content: @Composable BoxScope.() -> Unit) {
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
