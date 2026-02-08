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
import androidx.compose.ui.graphics.Color
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
 * Tab selezionata SOLO per evidenziare l'icona (UI).
 * La navigazione la gestisci fuori (MainActivity / NavHost) via callback.
 */
enum class BottomTab { HOME, HISTORY, MACHINES, PROFILE, HELP, CATALOGO }

private data class NavItem(
    val tab: BottomTab,
    val label: String,
    val iconRes: Int
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
    hasActiveRentals: Boolean = false, // ⬅️ PARAMETRO AGGIUNTO
    onFabClick: () -> Unit = {},
    onTabSelected: (BottomTab) -> Unit = {}
) {
    // Layout
    val barHeight = 76.dp
    val fabSize = 75.dp
    val cutoutRadius = (fabSize.value / 2f) + 14f
    val barShape = CurvedBottomBarShape(cutoutRadius)

    // Colori theme
    val barColor = MaterialTheme.colorScheme.background
    val outline = MaterialTheme.colorScheme.outline
    val selectedColor = MaterialTheme.colorScheme.onSurface
    val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    val indicator = MaterialTheme.colorScheme.secondary

    val fabColor = MaterialTheme.colorScheme.primary
    val fabIconTint = MaterialTheme.colorScheme.onPrimary

    // Config tabs
    val config = when (mode) {
        NavBarMode.PRODUCT_FLOW -> NavBarConfig(
            left1 = NavItem(
                tab = BottomTab.PROFILE,
                label = "profilo",
                iconRes = R.drawable.ic_profile
            ),
            left2 = NavItem(
                tab = BottomTab.HISTORY,
                label = "acquisti",
                iconRes = R.drawable.ic_storico
            ),
            right1 = NavItem(
                tab = BottomTab.CATALOGO,
                label = "catalogo",
                iconRes = R.drawable.ic_store
            ),
            right2 = NavItem(
                tab = BottomTab.HELP,
                label = "aiuto",
                iconRes = R.drawable.ic_help
            )
        )

        NavBarMode.MACHINE_FLOW -> NavBarConfig(
            left1 = NavItem(
                tab = BottomTab.PROFILE,
                label = "profilo",
                iconRes = R.drawable.ic_profile
            ),
            left2 = NavItem(
                tab = BottomTab.HISTORY,
                label = "storico",
                iconRes = R.drawable.ic_storico
            ),
            right1 = NavItem(
                tab = BottomTab.MACHINES,
                label = "macchinette",
                iconRes = R.drawable.ic_distributori
            ),
            right2 = NavItem(
                tab = BottomTab.HELP,
                label = "aiuto",
                iconRes = R.drawable.ic_help
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Barra
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .shadow(10.dp, barShape)
                .background(barColor, barShape)
                .border(2.dp, outline, barShape)
        )

        // FAB centrale
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(fabSize)
                .shadow(12.dp, CircleShape)
                .background(fabColor, CircleShape)
                .border(2.dp, outline, CircleShape)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onFabClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_home),
                contentDescription = "home",
                modifier = Modifier.size(30.dp),
                tint = fabIconTint
            )
        }

        // Items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .padding(horizontal = 10.dp)
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT 1 - PROFILO (con possibile badge)
            NavBarItem(
                item = config.left1,
                isSelected = selectedTab == config.left1.tab,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                indicatorColor = indicator,
                outlineColor = outline,
                onClick = { onTabSelected(config.left1.tab) },
                modifier = Modifier.weight(1f),
                showBadge = hasActiveRentals && config.left1.tab == BottomTab.PROFILE
            )

            // LEFT 2 - HISTORY
            NavBarItem(
                item = config.left2,
                isSelected = selectedTab == config.left2.tab,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                indicatorColor = indicator,
                outlineColor = outline,
                onClick = { onTabSelected(config.left2.tab) },
                modifier = Modifier.weight(1f),
                showBadge = false
            )

            Spacer(modifier = Modifier.width(fabSize + 18.dp))

            // RIGHT 1 - CATALOGO/MACHINES
            NavBarItem(
                item = config.right1,
                isSelected = selectedTab == config.right1.tab,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                indicatorColor = indicator,
                outlineColor = outline,
                onClick = { onTabSelected(config.right1.tab) },
                modifier = Modifier.weight(1f),
                showBadge = false
            )

            // RIGHT 2 - HELP
            NavBarItem(
                item = config.right2,
                isSelected = selectedTab == config.right2.tab,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                indicatorColor = indicator,
                outlineColor = outline,
                onClick = { onTabSelected(config.right2.tab) },
                modifier = Modifier.weight(1f),
                showBadge = false
            )
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    selectedColor: androidx.compose.ui.graphics.Color,
    unselectedColor: androidx.compose.ui.graphics.Color,
    indicatorColor: androidx.compose.ui.graphics.Color,
    outlineColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBadge: Boolean = false // ⬅️ PARAMETRO AGGIUNTO
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val pillModifier = if (isSelected) {
            Modifier
                .shadow(4.dp, CircleShape)
                .background(indicatorColor, CircleShape)
                .border(2.dp, outlineColor, CircleShape)
                .padding(horizontal = 14.dp, vertical = 5.dp)
        } else {
            Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
        }

        // ⬇️ BOX WRAPPER per contenere icona + badge
        Box(contentAlignment = Alignment.Center) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = pillModifier
            ) {
                Icon(
                    painter = painterResource(item.iconRes),
                    contentDescription = item.label,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) selectedColor else unselectedColor
                )
            }

            // ⬇️ PALLINO ROSSO (badge notification)
            if (showBadge) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .shadow(3.dp, CircleShape)
                        .background(Color(0xFFEF5350), CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-2).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.label,
            fontSize = 11.sp,
            color = if (isSelected) selectedColor else unselectedColor,
            lineHeight = 12.sp
        )
    }
}

// -----------------------------------------------------------
// Shape con cutout centrale
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