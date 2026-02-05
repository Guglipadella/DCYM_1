package it.polito.did.dcym.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.did.dcym.R
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.ui.components.BottomNavBar
import it.polito.did.dcym.ui.components.BottomTab
import it.polito.did.dcym.ui.components.GraphPaperBackground
import it.polito.did.dcym.ui.components.NavBarMode
import it.polito.did.dcym.ui.theme.AppColors

@Composable
fun ProfileScreen(
    onGoToHomeChoice: () -> Unit,
    onGoToCatalog: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToHelp: () -> Unit,
    onGoToProfile: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                mode = NavBarMode.PRODUCT_FLOW,
                selectedTab = BottomTab.PROFILE,
                onFabClick = onGoToHomeChoice,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomTab.CATALOGO -> onGoToCatalog()
                        BottomTab.HISTORY -> onGoToHistory()
                        BottomTab.HELP -> onGoToHelp()
                        else -> {}
                    }
                }
            )
        }
    ) { paddingValues ->
        GraphPaperBackground {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(20.dp))

                // 1. HEADER (Ciao Nome + Foto)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ciao,",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = uiState.userName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Foto Profilo
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(4.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clip(CircleShape)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.user),
                            contentDescription = "Profilo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 2. RIQUADRO DISPONIBILITÀ (Stondato + Bottone Ricarica)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Disponibilità",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${uiState.userBalance.toInt()}€", // Mostra saldo intero
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Pulsante Ricarica (Mock)
                        Button(
                            onClick = { /* TODO: Ricarica */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Ricarica", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // 3. TITOLO E DESCRIZIONE
                Text(
                    text = "Noleggi attivi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Qui sono presenti i tuoi noleggi attivi.\nSeleziona un prodotto per visualizzare i dettagli o avviare la restituzione.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(16.dp))

                // 4. BARRA DI RICERCA
                ProfileSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) }
                )

                Spacer(Modifier.height(16.dp))

                // 5. LISTA CARD
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.filteredRentals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nessun noleggio attivo trovato.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(uiState.filteredRentals) { order ->
                            ActiveRentalCard(order = order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveRentalCard(order: Order) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val context = LocalContext.current

    // Logica icone e colori
    val isOngoing = order.status.equals("ONGOING", ignoreCase = true)

    // Icona: ic_rent se attivo, ic_refound se in attesa di rimborso
    val iconRes = if (isOngoing) {
        R.drawable.ic_rent
    } else {
        // Cerca ic_refound, fallback a ic_storico
        val refoundId = context.resources.getIdentifier("ic_refound", "drawable", context.packageName)
        if (refoundId != 0) refoundId else R.drawable.ic_storico
    }

    val statusColor = if (isOngoing) AppColors.VioletPastelMuted else Color(0xFFEF9A9A) // Viola o Rosso
    val statusTitle = if (isOngoing) "Noleggio attivo" else "In attesa di conferma"

    val statusBody = if (isOngoing) "Tempo rimanente: 23h" else "In fase di valutazione"

    Card(
        colors = CardDefaults.cardColors(containerColor = paper),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outline, RoundedCornerShape(20.dp))
            .clickable { /* Navigazione dettaglio */ }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ICONA
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(statusColor, CircleShape)
                    .border(1.dp, outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // INFO
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if(order.productName.isNotEmpty()) order.productName else "Prodotto #${order.productId}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = statusTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if(isOngoing) Color(0xFF6A5AE0) else Color(0xFFD32F2F)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = statusBody,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.width(12.dp))

            // FRECCIA STONDATA
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                    .border(2.dp, outline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = "Vai",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Cerca nei noleggi...", fontSize = 14.sp, color = Color.Gray) },
        leadingIcon = {
            Icon(
                painterResource(android.R.drawable.ic_menu_search),
                null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .shadow(2.dp, RoundedCornerShape(16.dp))
    )
}