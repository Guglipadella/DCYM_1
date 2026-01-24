package it.polito.did.dcym.ui.screens.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.did.dcym.R
import it.polito.did.dcym.data.model.OrderStatus
import it.polito.did.dcym.ui.components.BottomNavBar
import it.polito.did.dcym.ui.components.BottomTab
import it.polito.did.dcym.ui.components.GraphPaperBackground
import it.polito.did.dcym.ui.components.NavBarMode
import it.polito.did.dcym.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    onGoToHomeChoice: () -> Unit,
    onGoToCatalog: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToHelp: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedItemForDetail by remember { mutableStateOf<HistoryItem?>(null) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                mode = NavBarMode.PRODUCT_FLOW,
                selectedTab = BottomTab.HISTORY,
                onFabClick = onGoToHomeChoice,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomTab.CATALOGO -> onGoToCatalog()
                        BottomTab.PROFILE -> onGoToProfile()
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
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "I tuoi Acquisti",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(16.dp))

                // --- FILTRI (Tabs) ---
                HistoryFilters(
                    currentFilter = uiState.filter,
                    onFilterSelect = { viewModel.setFilter(it) }
                )

                Spacer(Modifier.height(16.dp))

                // --- LISTA ---
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.filteredItems.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nessun ordine trovato qui.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(uiState.filteredItems) { item ->
                            HistoryItemCard(
                                item = item,
                                onClick = { selectedItemForDetail = item }
                            )
                        }
                    }
                }
            }
        }

        // --- DIALOG DETTAGLIO (Stile Info Acquisto PDF) ---
        if (selectedItemForDetail != null) {
            val item = selectedItemForDetail!!
            HistoryDetailDialog(
                item = item,
                isPlaying = uiState.isPlayingSound,
                onDismiss = { selectedItemForDetail = null },
                onPlaySound = { viewModel.playSound(item.order.pickupCode) }
            )
        }
    }
}

@Composable
fun HistoryFilters(
    currentFilter: HistoryFilter,
    onFilterSelect: (HistoryFilter) -> Unit
) {
    val filters = listOf(
        HistoryFilter.IN_CORSO to "In Corso",
        HistoryFilter.TUTTI to "Tutti",
        HistoryFilter.COMPLETATI to "Storico"
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { (filter, label) ->
            val isSelected = currentFilter == filter
            val bg = if (isSelected) AppColors.VioletPastelMuted else MaterialTheme.colorScheme.surface
            val txtColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface

            Box(
                modifier = Modifier
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clip(CircleShape)
                    .background(bg)
                    .clickable { onFilterSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = txtColor)
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: HistoryItem, onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val context = LocalContext.current

    val imageResId = remember(item.productImageRes) {
        context.resources.getIdentifier(item.productImageRes, "drawable", context.packageName)
    }

    val isPending = item.order.status == OrderStatus.PENDING
    val statusColor = if(isPending) Color(0xFFE57373) else AppColors.GreenPastelMuted
    val statusText = if(isPending) "Da ritirare" else "Ritirato"

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = paper),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outline, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Immagine piccola
            Image(
                painter = painterResource(if(imageResId!=0) imageResId else R.drawable.ic_logo_png_dcym),
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(item.machineName, fontSize = 12.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${item.price.toInt()} €", fontWeight = FontWeight.Black, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                // Badge Stato
                Box(
                    modifier = Modifier
                        .background(statusColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryDetailDialog(
    item: HistoryItem,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onPlaySound: () -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val isPending = item.order.status == OrderStatus.PENDING

    // Formattazione Data
    val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(item.order.purchaseTimestamp))

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .background(paper, RoundedCornerShape(24.dp))
                .border(2.dp, outline, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con X
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        text = "Info Acquisto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Info
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Articolo:", color = Color.Gray)
                    Text(item.productName, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Presso:", color = Color.Gray)
                    Text(item.machineName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Data:", color = Color.Gray)
                    Text(dateString, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Importo:", color = Color.Gray)
                    Text("${item.price} €", fontWeight = FontWeight.ExtraBold)
                }

                Spacer(Modifier.height(32.dp))

                if (isPending) {
                    // PULSANTE RIPRODUCI
                    Button(
                        onClick = onPlaySound,
                        enabled = !isPlaying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(2.dp, outline, RoundedCornerShape(16.dp))
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isPlaying) {
                            Text("Riproduzione...", color = MaterialTheme.colorScheme.onSecondary)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("RIPRODUCI CODICE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Avvicina il telefono al microfono", fontSize = 12.sp, color = Color.Gray)
                } else {
                    // Sticker "Ritirato"
                    Box(
                        modifier = Modifier
                            .background(AppColors.GreenPastelMuted, RoundedCornerShape(12.dp))
                            .border(2.dp, outline, RoundedCornerShape(12.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("PRODOTTO RITIRATO", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiary)
                    }
                }
            }
        }
    }
}