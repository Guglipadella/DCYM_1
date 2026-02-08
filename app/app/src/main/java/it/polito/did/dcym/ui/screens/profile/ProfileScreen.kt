package it.polito.did.dcym.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
                hasActiveRentals = uiState.activeRentals.isNotEmpty(),
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
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(20.dp)) }
                item { ProfileHeader(uiState.userName) }
                item { BalanceCard(uiState.userBalance) }

                if (uiState.pendingPickupOrders.isNotEmpty()) {
                    item { SectionTitle("Da ritirare", "Prodotti in attesa di ritiro") }
                    items(uiState.pendingPickupOrders) { order ->
                        ProfileItemCard(
                            order = order,
                            machineName = uiState.machineNames[order.machineId] ?: order.machineId,
                            statusLabel = "Ritira entro 24h",
                            isYellow = true,
                            onClick = { selectedPickupOrder = order }
                        )
                    }
                }

                item { SectionTitle("Noleggi attivi", "Seleziona per gestire la restituzione") }
                if (uiState.activeRentals.isEmpty()) {
                    item { Text("Nessun noleggio attivo.", color = Color.Gray, fontSize = 13.sp) }
                } else {
                    items(uiState.activeRentals) { order ->
                        ProfileItemCard(
                            order = order,
                            machineName = uiState.machineNames[order.machineId] ?: order.machineId,
                            statusLabel = "In corso",
                            onClick = { selectedOrderForReturn = order }
                        )
                    }
                }

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

        if (selectedPickupOrder != null) {
            val order = selectedPickupOrder!!
            HistoryDetailDialog(
                item = HistoryItem(order, order.productName, uiState.machineNames[order.machineId] ?: order.machineId, uiState.productImages[order.productId.toString()] ?: "ic_logo_png_dcym", order.totalCost),
                isPlaying = uiState.isPlayingSound,
                onDismiss = { selectedPickupOrder = null },
                onPlaySound = { viewModel.playSound(order.pickupCode) }
            )
        }

        // DIALOG RESTITUZIONE PRODOTTO
        if (selectedOrderForReturn != null) {
            ReturnDetailDialog(
                order = selectedOrderForReturn!!,
                machineName = uiState.machineNames[selectedOrderForReturn!!.machineId] ?: selectedOrderForReturn!!.machineId,
                refundRows = viewModel.getFutureRefundOptions(selectedOrderForReturn!!),
                isPlaying = uiState.isPlayingSound,
                onDismiss = { selectedOrderForReturn = null },
                onPlaySound = { viewModel.playSound(it) }
            )
        }

        // DIALOG VALUTAZIONE RIMBORSO
        if (selectedOrderForRefund != null) {
            RefundEvaluationDialog(
                order = selectedOrderForRefund!!,
                machineName = uiState.machineNames[selectedOrderForRefund!!.machineId] ?: selectedOrderForRefund!!.machineId,
                onDismiss = { selectedOrderForRefund = null }
            )
        }
    }
}

// ---------------- DIALOGS ----------------

@Composable
fun ReturnDetailDialog(
    order: Order,
    machineName: String,
    refundRows: List<ProfileViewModel.RefundRow>,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onPlaySound: (String) -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val yellowAction = Color(0xFFFFD54F)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .background(paper, RoundedCornerShape(24.dp))
                .border(2.dp, outline, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        text = "Restituzione Prodotto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 48.dp)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // TUTORIAL SPAZIATO
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), // Spazio extra sopra/sotto
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    TutorialItem(1, "Recati alla macchinetta", machineName)
                    TutorialItem(2, "Verifica integrità", "Scatta una foto al prodotto per confermarne lo stato.")
                    TutorialItem(3, "Riponi l'oggetto", "Inseriscilo nel locker e chiudi con cura lo sportello.")
                }

                Spacer(Modifier.height(32.dp))

                // TASTO PLAY GIALLO
                Button(
                    onClick = { onPlaySound(order.pickupCode) },
                    enabled = !isPlaying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(2.dp, outline, RoundedCornerShape(16.dp))
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = yellowAction,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) "RIPRODUZIONE..." else "RIPRODUCI SUONO SBLOCCO",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    "Piano Rimborsi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        refundRows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(row.label, fontSize = 13.sp)
                                Text(
                                    "+${String.format("%.2f", row.amount)} €",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("TERMINA NOLEGGIO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TutorialItem(number: Int, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(description, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
        }
    }
}

@Composable
fun RefundEvaluationDialog(order: Order, machineName: String, onDismiss: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .background(paper, RoundedCornerShape(24.dp))
                .border(2.dp, outline, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        text = "Stato Rimborso", // <--- Aggiungi il titolo
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 48.dp)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Icon(painterResource(R.drawable.ic_refound), null, modifier = Modifier.size(64.dp), tint = Color(0xFFFFD54F))

                Spacer(Modifier.height(16.dp))

                Text("Valutazione in corso", fontSize = 20.sp, fontWeight = FontWeight.Black)

                Spacer(Modifier.height(8.dp))

                Text(
                    "Hai restituito: ${order.productName}\npresso $machineName",
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "Il rimborso verrà accreditato entro 24 ore previa verifica dell'integrità.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text("CHIUDI")
                }
            }
        }
    }
}

// ---------------- ALTRI COMPONENTI ----------------

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
            Spacer(Modifier.weight(1f))
            Button(onClick = {}, shape = RoundedCornerShape(12.dp)) { Text("Ricarica") }
        }
    }
}

@Composable
fun ProfileItemCard(
    order: Order,
    machineName: String,
    statusLabel: String,
    isYellow: Boolean = false,
    isRefund: Boolean = false,
    onClick: () -> Unit
) {
    val yellowAction = Color(0xFFFFD54F)
    val iconRes = if (isRefund) R.drawable.ic_refound else if (order.isRent) R.drawable.ic_rent else R.drawable.ic_buy

    // ⬇️ WRAPPER BOX per far uscire il pallino
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp) // Spazio per il pallino
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isYellow) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(iconRes), null, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(order.productName, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(machineName, fontSize = 12.sp, color = Color.Gray)
                    Text(statusLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = if (isYellow) Color.Black else Color(0xFF6A5AE0))
                }
                Box(
                    Modifier.size(36.dp)
                        .background(yellowAction, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_arrow_right), null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }
        }

        // ⬇️ PALLINO ROSSO per noleggi ONGOING
        if (order.status == "ONGOING" && order.isRent) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color(0xFFEF5350), CircleShape)
                    .border(3.dp, Color.White, CircleShape)
                    .align(Alignment.TopStart)
                    .offset(x = 4.dp, y = (-4).dp)
            )
        }
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
            Image(painterResource(R.drawable.user), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
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