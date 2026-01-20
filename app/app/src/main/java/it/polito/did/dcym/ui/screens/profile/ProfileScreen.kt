package it.polito.did.dcym.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.polito.did.dcym.ui.components.BottomNavBar
import it.polito.did.dcym.ui.components.BottomTab
import it.polito.did.dcym.ui.components.NavBarMode
import it.polito.did.dcym.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color

@Composable
fun ProfileScreen(
    onGoToHomeChoice: () -> Unit,
    onGoToCatalog: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToHelp: () -> Unit,
) {
    val bg = MaterialTheme.colorScheme.background
    val paper = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline

    Scaffold(
        containerColor = bg,
        bottomBar = {
            BottomNavBar(
                mode = NavBarMode.PRODUCT_FLOW,
                selectedTab = BottomTab.CATALOGO,
                onFabClick = onGoToHomeChoice,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomTab.CATALOGO -> onGoToCatalog()
                        BottomTab.PROFILE -> onGoToProfile()
                        BottomTab.HISTORY -> onGoToHistory()
                        BottomTab.HELP -> onGoToHelp()
                        else -> {} // in PRODUCT_FLOW ignoro il resto
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Header semplice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(18.dp))
                    .background(paper, RoundedCornerShape(18.dp))
                    .border(2.dp, outline, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(6.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            .border(2.dp, outline, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.user), // se non esiste, cambia con un'icona
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Profilo",
                            fontSize = 18.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Qui potrai modificare i tuoi dati",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Card placeholder: “Modifica nome”
            ProfileItemCard(title = "Nome e cognome", subtitle = "In costruzione")

            // Card placeholder: “Avatar”
            ProfileItemCard(title = "Immagine profilo", subtitle = "In costruzione")

            // Card placeholder: “Preferenze”
            ProfileItemCard(title = "Preferenze", subtitle = "In costruzione")
        }
    }
}

@Composable
private fun ProfileItemCard(title: String, subtitle: String) {
    val paper = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline

    Card(
        colors = CardDefaults.cardColors(containerColor = paper),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outline, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
