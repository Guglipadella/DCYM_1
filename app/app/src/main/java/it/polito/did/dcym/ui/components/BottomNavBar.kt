package it.polito.did.dcym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.polito.did.dcym.R

/**
 * Due navbar diverse in base al percorso scelto all'inizio (come PDF)
 */
enum class NavBarMode { PRODUCT_FLOW, MACHINE_FLOW }

/**
 * Tab selezionata SOLO per evidenziare l’icona (UI). Non è logica di navigazione.
 */
enum class BottomTab { HOME, HISTORY, MACHINES, PROFILE, HELP, CATALOGO }

private data class NavItem(
    val tab: BottomTab,
    val label: String,
    val iconRes: Int,
    val onClick: () -> Unit
)

private data class NavBarConfig(
    val left1: NavItem,
    val left2: NavItem,
    val right1: NavItem,
    val right2: NavItem
)

@Composable
fun BottomNavBar(
    mode: NavBarMode = NavBarMode.PRODUCT_FLOW,
    selectedTab: BottomTab = BottomTab.CATALOGO,
    onHomeClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onQrClick: () -> Unit = {},
    onMachinesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Layout
    val barHeight = 80.dp
    val fabOffset = 30.dp
    val fabSize = 64.dp
    val barShape = CurvedBottomBarShape(fabSize.value + 16f)

    // Colori dal Theme (che ora usa la tua palette)
    val barColor = MaterialTheme.colorScheme.background      // “carta”
    val outline = MaterialTheme.colorScheme.outline          // magenta scuro
    val selectedColor = MaterialTheme.colorScheme.onSurface  // magenta scuro
    val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

    // Pill selezione (giallo pastello)
    val indicator = MaterialTheme.colorScheme.secondary

    // FAB (viola intenso)
    val fabColor = MaterialTheme.colorScheme.primary
    val fabIconTint = MaterialTheme.colorScheme.onPrimary
    val isCenterSelected = selectedTab == BottomTab.HOME
    val fabBorderColor = if (isCenterSelected) selectedColor else outline
    val fabElevation = if (isCenterSelected) 14.dp else 8.dp


    val config = when (mode) {
        NavBarMode.PRODUCT_FLOW -> NavBarConfig(
            left1 = NavItem(
                tab = BottomTab.PROFILE,
                label = "profilo",
                iconRes = R.drawable.ic_profile,          // <-- icona HOME del PDF
                onClick = onProfileClick
            ),
            left2 = NavItem(
                tab = BottomTab.HISTORY,
                label = "Acquisti",
                iconRes = R.drawable.ic_storico,       // <-- icona STORICO del PDF
                onClick = onHistoryClick
            ),
            right1 = NavItem(
                tab = BottomTab.CATALOGO,
                label = "catalogo",
                iconRes = R.drawable.ic_store,
                onClick = onHomeClick // per ora lo riusiamo come callback "vai al catalogo"
            ),
            right2 = NavItem(
                tab = BottomTab.HELP,
                label = "aiuto",
                iconRes = R.drawable.ic_help,       // <-- icona PROFILO del PDF
                onClick = onProfileClick
            )
        )

        NavBarMode.MACHINE_FLOW -> NavBarConfig(
            left1 = NavItem(
                tab = BottomTab.MACHINES,
                label = "Mappa",
                iconRes = R.drawable.ic_distributori,  // <-- se hai un'icona mappa, mettila qui (es. ic_map)
                onClick = onMachinesClick
            ),
            left2 = NavItem(
                tab = BottomTab.HISTORY,
                label = "Storico",
                iconRes = R.drawable.ic_storico,
                onClick = onHistoryClick
            ),
            right1 = NavItem(
                tab = BottomTab.HOME,
                label = "Prodotti",
                iconRes = R.drawable.ic_home,          // <-- se hai un’icona “prodotti”, mettila qui (es. ic_catalog)
                onClick = onHomeClick
            ),
            right2 = NavItem(
                tab = BottomTab.PROFILE,
                label = "Profilo",
                iconRes = R.drawable.ic_profile,
                onClick = onProfileClick
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight + fabOffset),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1) Fondo barra + ombra
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .shadow(10.dp, barShape)
                .background(barColor, barShape)
        )

        // 1b) Bordo (outline) sopra la barra
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter)
                .border(2.dp, outline, barShape)
        )

        // 2) FAB centrale
        Box(

            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 10.dp)
                .size(fabSize)
                .shadow(12.dp, CircleShape)
                .background(fabColor, CircleShape)
                .border(2.dp, outline, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onQrClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_home),

                contentDescription = "CATALOGO",
                modifier = Modifier.size(32.dp),
                tint = fabIconTint
            )
        }

        // 3) Items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                iconRes = config.left1.iconRes,
                label = config.left1.label,
                isSelected = selectedTab == config.left1.tab,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                indicatorColor = indicator,
                outlineColor = outline,
                onClick = config.left1.onClick,
                modifier = Modifier.weight(1f)
            )

            NavBarItem(
                iconRes = config.left2.iconRes,
                label = config.left2.label,
                isSelected = selectedTab == config.left2.tab,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                indicatorColor = indicator,
                outlineColor = outline,
                onClick = config.left2.onClick,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(fabSize + 16.dp))

            NavBarItem(
                iconRes = config.right1.iconRes,
                label = config.right1.label,
                isSelected = selectedTab == config.right1.tab,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                indicatorColor = indicator,
                outlineColor = outline,
                onClick = config.right1.onClick,
                modifier = Modifier.weight(1f)
            )

            NavBarItem(
                iconRes = config.right2.iconRes,
                label = config.right2.label,
                isSelected = selectedTab == config.right2.tab,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                indicatorColor = indicator,
                outlineColor = outline,
                onClick = config.right2.onClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavBarItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    selectedColor: androidx.compose.ui.graphics.Color,
    unselectedColor: androidx.compose.ui.graphics.Color,
    indicatorColor: androidx.compose.ui.graphics.Color,
    outlineColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // “pill” selezionata stile sticker (con bordo + ombra)
        val pillModifier = if (isSelected) {
            Modifier
                .shadow(4.dp, CircleShape)
                .background(indicatorColor, CircleShape)
                .border(2.dp, outlineColor, CircleShape)
                .padding(horizontal = 14.dp, vertical = 5.dp)
        } else Modifier

        Box(
            contentAlignment = Alignment.Center,
            modifier = pillModifier
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) selectedColor else unselectedColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) selectedColor else unselectedColor,
            lineHeight = 12.sp
        )
    }
}

// -----------------------------------------------------------
// Curva cutout invariata
// -----------------------------------------------------------
class CurvedBottomBarShape(private val circleRadius: Float) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Outline.Generic(path = drawCurvedPath(size, circleRadius))

    private fun drawCurvedPath(size: androidx.compose.ui.geometry.Size, radius: Float): Path {
        return Path().apply {
            val centerX = size.width / 2f

            moveTo(0f, 0f)
            lineTo(centerX - radius * 1.2f, 0f)

            cubicTo(
                centerX - radius * 0.8f, 0f,
                centerX - radius * 0.8f, radius * 0.8f,
                centerX, radius * 0.8f
            )

            cubicTo(
                centerX + radius * 0.8f, radius * 0.8f,
                centerX + radius * 0.8f, 0f,
                centerX + radius * 1.2f, 0f
            )

            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
    }
}
