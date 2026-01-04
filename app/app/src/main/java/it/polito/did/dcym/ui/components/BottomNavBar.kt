package it.polito.did.dcym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.polito.did.dcym.R

// Colori presi dal design
private val BarColor = Color.White
private val FabColor = Color(0xFFD9D9D9) // Grigio chiaro del cerchio QR
private val SelectedColor = Color.Black
private val UnselectedColor = Color.Gray // O un colore più scuro se preferisci
private val IndicatorColor = Color(0xFFE0D4FC) // Lilla per lo sfondo icona attiva

@Composable
fun BottomNavBar(
    onHomeClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onQrClick: () -> Unit = {},
    onMachinesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Altezza totale della barra
    val barHeight = 80.dp
    // Quanto il bottone QR esce fuori dalla barra (verso l'alto)
    val fabOffset = 30.dp
    // Dimensione del bottone QR
    val fabSize = 64.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight + fabOffset) // Spazio extra per il bottone che esce
        , contentAlignment = Alignment.BottomCenter
    ) {

        // 1. LO SFONDO DELLA BARRA CON IL "TAGLIO" (Curva)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .shadow(
                    elevation = 8.dp, // Ombra sotto la barra
                    shape = CurvedBottomBarShape(fabSize.value + 16f) // +16f per dare aria attorno al cerchio
                )
                .background(
                    color = BarColor,
                    shape = CurvedBottomBarShape(fabSize.value + 16f)
                )
        )

        // 2. IL BOTTONE QR GALLEGGIANTE (FAB)
        // Lo posizioniamo in alto al centro
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter) // Si allinea in alto nel Box padre
                .offset(y = 10.dp) // Piccolo aggiustamento fine
                .size(fabSize)
                .shadow(4.dp, CircleShape)
                .background(FabColor, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onQrClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_qr_code),
                contentDescription = "QR Code",
                modifier = Modifier.size(32.dp),
                tint = Color.Black // O Unspecified se l'icona ha già i suoi colori
            )
        }

        // 3. LE ICONE (HOME, STORICO, MACCHINETTE, PROFILO)
        // Usiamo una Row che occupa solo la parte bassa (la barra bianca)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lato Sinistro
            NavBarItem(
                iconRes = R.drawable.ic_home,
                label = "Home",
                isSelected = true, // Logica da collegare allo stato vero poi
                onClick = onHomeClick,
                modifier = Modifier.weight(1f)
            )

            NavBarItem(
                iconRes = R.drawable.ic_storico,
                label = "Storico",
                isSelected = false,
                onClick = onHistoryClick,
                modifier = Modifier.weight(1f)
            )

            // SPAZIO VUOTO AL CENTRO (Per il QR)
            Spacer(modifier = Modifier.width(fabSize + 16.dp)) // Largo quanto il buco

            // Lato Destro
            NavBarItem(
                iconRes = R.drawable.ic_distributori,
                label = "Macchinette", // Testo su due righe se troppo lungo?
                isSelected = false,
                onClick = onMachinesClick,
                modifier = Modifier.weight(1f)
            )

            NavBarItem(
                iconRes = R.drawable.ic_profile,
                label = "Profilo",
                isSelected = false,
                onClick = onProfileClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// COMPONENTE PER IL SINGOLO ELEMENTO DELLA BARRA
@Composable
fun NavBarItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Rimuove l'effetto ripple grigio al click
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Indicatore di selezione (opzionale, come da design Material)
        Box(
            contentAlignment = Alignment.Center,
            modifier = if (isSelected) Modifier
                .background(IndicatorColor, CircleShape)
                .padding(horizontal = 16.dp, vertical = 4.dp)
            else Modifier
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) SelectedColor else UnselectedColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) SelectedColor else UnselectedColor,
            lineHeight = 12.sp
        )
    }
}

// -----------------------------------------------------------
// MATEMATICA PER DISEGNARE LA CURVA (Il "Cutout")
// -----------------------------------------------------------
class CurvedBottomBarShape(private val circleRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(path = drawCurvedPath(size, circleRadius))
    }

    private fun drawCurvedPath(size: Size, radius: Float): Path {
        return Path().apply {
            val centerX = size.width / 2f

            // Inizia in alto a sinistra
            moveTo(0f, 0f)

            // Disegna linea fino all'inizio della curva sinistra
            lineTo(centerX - radius * 1.2f, 0f)

            // Disegna la curva verso il basso (Cubic Bezier)
            // Punto di controllo 1, Punto di controllo 2, Punto finale
            cubicTo(
                centerX - radius * 0.8f, 0f, // Controllo 1 (inizio curva morbida)
                centerX - radius * 0.8f, radius * 0.8f, // Controllo 2 (giù)
                centerX, radius * 0.8f // Punto più basso (centro)
            )

            // Disegna la curva verso l'alto (risalita)
            cubicTo(
                centerX + radius * 0.8f, radius * 0.8f, // Controllo 1
                centerX + radius * 0.8f, 0f, // Controllo 2
                centerX + radius * 1.2f, 0f // Fine curva
            )

            // Continua fino alla fine a destra
            lineTo(size.width, 0f)

            // Chiudi il rettangolo
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
    }
}