package it.polito.did.dcym.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.did.dcym.R
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.ui.components.*
import it.polito.did.dcym.ui.screens.history.HistoryDetailDialog
import it.polito.did.dcym.ui.screens.history.HistoryItem

@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedOrderForReturn by remember { mutableStateOf<Order?>(null) }
    var selectedOrderForRefund by remember { mutableStateOf<Order?>(null) }
    var selectedPickupOrder by remember { mutableStateOf<Order?>(null) }

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
        GraphPaperBackground { // Sfondo a quadretti
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(20.dp)) }
                item { ProfileHeader(uiState.userName) }
                item { BalanceCard(uiState.userBalance) }

                // --- DA RITIRARE (PENDING) ---
                if (uiState.pendingPickupOrders.isNotEmpty()) {
                    item { SectionTitle("Da ritirare", "Prodotti in attesa di ritiro") }
                    items(uiState.pendingPickupOrders) { order ->
                        ProfileItemCard(
                            order = order,
                            machineName = uiState.machineNames[order.machineId] ?: order.machineId,
                            statusLabel = "Ritira entro 24h",
                            isYellow = true,
                            onClick = { selectedPickupOrder = order } // Apre HistoryDetailDialog [Punto 5]
                        )
                    }
                }

                // --- NOLEGGI ATTIVI (ONGOING) ---
                item { SectionTitle("Noleggi attivi", "Seleziona per gestire la restituzione") }
                items(uiState.activeRentals) { order ->
                    ProfileItemCard(
                        order = order,
                        machineName = uiState.machineNames[order.machineId] ?: order.machineId,
                        statusLabel = "In corso",
                        onClick = { selectedOrderForReturn = order }
                    )
                }

                // --- IN VALUTAZIONE (PENDING_REFUND) ---
                if (uiState.pendingRefundOrders.isNotEmpty()) {
                    item { SectionTitle("In valutazione", "Oggetti restituiti") }
                    items(uiState.pendingRefundOrders) { order ->
                        ProfileItemCard(
                            order = order,
                            machineName = uiState.machineNames[order.machineId] ?: order.machineId,
                            statusLabel = "Verifica integrità",
                            isRefund = true,
                            onClick = { selectedOrderForRefund = order }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        // DIALOG RITIRO (Stesso di History) [Punto 5]
        if (selectedPickupOrder != null) {
            val order = selectedPickupOrder!!
            HistoryDetailDialog(
                item = HistoryItem(
                    order = order,
                    productName = order.productName,
                    machineName = uiState.machineNames[order.machineId] ?: order.machineId,
                    productImageRes = "ic_logo_png_dcym",
                    price = order.totalCost
                ),
                isPlaying = uiState.isPlayingSound,
                onDismiss = { selectedPickupOrder = null },
                onPlaySound = { viewModel.playSound(order.pickupCode) }
            )
        }

        // SHEET RESTITUZIONE [Punti 2, 4z]
        if (selectedOrderForReturn != null) {
            ModalBottomSheet(onDismissRequest = { selectedOrderForReturn = null }) {
                ReturnDetailSheet(
                    order = selectedOrderForReturn!!,
                    machineName = uiState.machineNames[selectedOrderForReturn!!.machineId] ?: selectedOrderForReturn!!.machineId,
                    refundRows = viewModel.getFutureRefundOptions(selectedOrderForReturn!!),
                    onPlaySound = { viewModel.playSound(it) },
                    onClose = { selectedOrderForReturn = null }
                )
            }
        }

        // SHEET RIMBORSO
        if (selectedOrderForRefund != null) {
            ModalBottomSheet(onDismissRequest = { selectedOrderForRefund = null }) {
                RefundWaitingSheet(selectedOrderForRefund!!, uiState.machineNames[selectedOrderForRefund!!.machineId] ?: selectedOrderForRefund!!.machineId)
            }
        }
    }
}

// --- COMPONENTI ---

@Composable
fun BalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Disponibilità", fontSize = 12.sp, color = Color.Gray)
                Text("${balance.toInt()}€", fontSize = 32.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.weight(1f)) // [Punto 1] Sposta il tasto a destra
            Button(onClick = {}, shape = RoundedCornerShape(12.dp)) { Text("Ricarica") }
        }
    }
}

@Composable
fun ProfileItemCard(order: Order, machineName: String, statusLabel: String, isYellow: Boolean = false, isRefund: Boolean = false, onClick: () -> Unit) {
    val yellowAction = Color(0xFFFFD54F) // [Punto 6]
    val iconRes = if (isRefund) R.drawable.ic_refound else if (order.isRent) R.drawable.ic_rent else R.drawable.ic_buy

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = if (isYellow) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(painterResource(iconRes), null, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(order.productName, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(machineName, fontSize = 12.sp, color = Color.Gray) // [Punto 3] Nome reale
                Text(statusLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isYellow) Color.Black else Color(0xFF6A5AE0))
            }
            // [Punto 6] Box giallo stondato
            Box(Modifier.size(36.dp).background(yellowAction, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_arrow_right), null, tint = Color.Black, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ReturnDetailSheet(order: Order, machineName: String, refundRows: List<ProfileViewModel.RefundRow>, onPlaySound: (String) -> Unit, onClose: () -> Unit) {
    Column(Modifier.padding(24.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Restituzione Prodotto", fontSize = 22.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))

        // Istruzioni [Punto 4z]
        Column(Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)).padding(16.dp)) {
            Text("1. Recati alla macchinetta: $machineName", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("2. Scatta una foto al prodotto per confermarne l'integrità.", fontSize = 14.sp)
            Text("3. Inserisci l'oggetto nel locker e chiudi lo sportello.", fontSize = 14.sp)
        }

        Spacer(Modifier.height(24.dp))

        // Tasto Play [Punto 2]
        Button(
            onClick = { onPlaySound(order.pickupCode) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PlayArrow, null)
            Spacer(Modifier.width(8.dp))
            Text("RIPRODUCI SUONO SBLOCCO")
        }

        Spacer(Modifier.height(24.dp))
        Text("Piano Rimborsi", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

        Card(Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(12.dp)) {
                refundRows.forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(row.label, fontSize = 13.sp)
                        Text("+${String.format("%.2f", row.amount)} €", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        // Tasto Termina Noleggio Coerente [Punto 4z]
        Button(
            onClick = { onClose() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("TERMINA NOLEGGIO")
        }
    }
}

@Composable
fun RefundWaitingSheet(order: Order, machineName: String) {
    Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(painterResource(R.drawable.ic_refound), null, Modifier.size(64.dp), tint = Color(0xFFFFD54F))
        Spacer(Modifier.height(16.dp))
        SectionTitle("In valutazione", "Hai restituito: ${order.productName}")
        Text("Luogo: $machineName", fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Text("Riceverai il rimborso entro 24 ore previa verifica.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ProfileHeader(name: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("Ciao,", fontSize = 24.sp)
            Text(name, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
        Box(modifier = Modifier.size(60.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)) {
            Image(painterResource(R.drawable.user), null, contentScale = ContentScale.Crop)
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String) {
    Column {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(subtitle, fontSize = 13.sp, color = Color.Gray)
    }
}