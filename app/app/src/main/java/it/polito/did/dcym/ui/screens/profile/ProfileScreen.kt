package it.polito.did.dcym.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
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

// Immagine
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.ui.graphics.asImageBitmap

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

    // --- LOGICA DI SEPARAZIONE SCADUTI ---
    val now = System.currentTimeMillis()
    val oneDayMs = 24L * 60 * 60 * 1000

    // Dividiamo gli ordini in attesa di ritiro in Validi e Scaduti
    val (expiredPickups, validPickups) = uiState.pendingPickupOrders.partition {
        now > (it.purchaseTimestamp + oneDayMs)
    }

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

                // 1. ORDINI DA RITIRARE (VALIDI)
                if (validPickups.isNotEmpty()) {
                    item { SectionTitle("Da ritirare", "Prodotti in attesa di ritiro") }
                    items(validPickups) { order ->
                        val timeLeftLabel = calculatePickupTimeLeft(order.purchaseTimestamp)
                        ProfileItemCard(
                            order = order,
                            machineName = uiState.machineNames[order.machineId] ?: order.machineId,
                            statusLabel = timeLeftLabel,
                            isYellow = true,
                            onClick = { selectedPickupOrder = order }
                        )
                    }
                }

                // 2. NOLEGGI ATTIVI
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

                // 3. IN VALUTAZIONE (RIMBORSI)
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

                // 4. ORDINI SCADUTI (In fondo, come richiesto)
                if (expiredPickups.isNotEmpty()) {
                    item {
                        SectionTitle("Scaduti", "Tempo per il ritiro esaurito")
                    }
                    items(expiredPickups) { order ->
                        ProfileItemCard(
                            order = order,
                            machineName = uiState.machineNames[order.machineId] ?: order.machineId,
                            statusLabel = "Tempo scaduto",
                            isYellow = false, // Magari non giallo per differenziarli
                            onClick = { selectedPickupOrder = order } // Permette comunque di vedere i dettagli
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
            val liveOrder = uiState.activeRentals.find { it.id == selectedOrderForReturn!!.id }
            ReturnDetailDialog(
                order = liveOrder ?: selectedOrderForReturn!!,
                machineName = uiState.machineNames[selectedOrderForReturn!!.machineId] ?: selectedOrderForReturn!!.machineId,
                refundRows = viewModel.getFutureRefundOptions(selectedOrderForReturn!!),
                isPlaying = uiState.isPlayingSound,
                onDismiss = { selectedOrderForReturn = null },
                onPlaySound = { viewModel.playSound(it) },
                onTerminate = { viewModel.completeReturn(it) }
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
    onPlaySound: (String) -> Unit,
    onTerminate: (String) -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val yellowAction = Color(0xFFFFD54F)

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        capturedBitmap = bitmap
    }

    val canTerminate = capturedBitmap != null && order.status == "RETURNED"

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
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
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 48.dp)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    TutorialItem(1, "Recati alla macchinetta", machineName)

                    Column {
                        TutorialItem(2, "Verifica integrità", "Scatta una foto al prodotto per confermarne lo stato.")
                        Spacer(Modifier.height(12.dp))

                        if (capturedBitmap == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp))
                                    .border(2.dp, outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .clickable { cameraLauncher.launch() },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = "Scatta la foto per procedere.\nSenza foto non potrai terminare.",
                                        fontSize = 13.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    bitmap = capturedBitmap!!.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(2.dp, outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { cameraLauncher.launch() },
                                    modifier = Modifier
                                        .height(44.dp)
                                        .border(2.dp, Color.Red, RoundedCornerShape(12.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.Red
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("RIFAI FOTO", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    TutorialItem(3, "Riponi l'oggetto", "Inseriscilo nel locker e chiudi con cura lo sportello.")
                }

                Spacer(Modifier.height(32.dp))

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
                            val isToday = row.label.contains("Oggi", ignoreCase = true)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = row.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "+${String.format("%.2f", row.amount)} €",
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (capturedBitmap != null && order.status != "RETURNED") {
                    Text(
                        text = "Attendi che la macchinetta rilevi l'oggetto...",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = { showConfirmation = true },
                    enabled = canTerminate,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    Text("TERMINA NOLEGGIO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Conferma Termine", fontWeight = FontWeight.Black) },
            text = { Text("Sei sicuro di voler terminare il noleggio? Assicurati di aver chiuso bene lo sportello.") },
            confirmButton = {
                TextButton(onClick = {
                    onTerminate(order.id)
                    showConfirmation = false
                    onDismiss()
                }) {
                    Text("SÌ, TERMINA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("ANNULLA", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
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
                        text = "Stato Rimborso",
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

    Box(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
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

        if (order.status == "ONGOING" && order.isRent) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color(0xFFEF5350), CircleShape)
                    .border(3.dp, Color.White, CircleShape)
                    .align(Alignment.TopStart)
                    .offset(x = 0.dp, y = (-8).dp)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Normal
        )
    }
}

fun calculatePickupTimeLeft(purchaseTime: Long): String {
    val oneDayMs = 24L * 60 * 60 * 1000
    val expiration = purchaseTime + oneDayMs
    val now = System.currentTimeMillis()
    val diff = expiration - now

    if (diff <= 0) return "Tempo scaduto"

    val hours = diff / (1000 * 60 * 60)
    val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)

    return "Scade in ${hours}h ${minutes}m"
}