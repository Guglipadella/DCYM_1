package it.polito.did.dcym.ui.screens.confirmation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.did.dcym.R
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.ui.components.GraphPaperBackground
import it.polito.did.dcym.ui.theme.AppColors

@Composable
fun ConfirmationScreen(
    productId: String?,
    machineId: String?,
    isRent: Boolean,
    onBackClick: () -> Unit,
    onFinalConfirmClick: () -> Unit,
    onPaymentSuccess: (String) -> Unit,
    viewModel: ConfirmationViewModel = viewModel()
) {
    LaunchedEffect(productId, machineId) {
        if (productId != null && machineId != null) {
            viewModel.loadData(productId.toInt(), machineId)
        }
    }
    val uiState by viewModel.uiState.collectAsState()

    ConfirmationContent(
        product = uiState.product,
        machine = uiState.machine,
        isRent = isRent,
        userBalance = uiState.userBalance,
        maxReturnDate = uiState.maxReturnDate,
        isLoading = uiState.isLoading,
        onBackClick = onBackClick,
        onConfirmAndPay = {
            // --- CORREZIONE: Passiamo isRent al ViewModel ---
            viewModel.confirmPurchase(isRent) { orderId ->
                onPaymentSuccess(orderId)
            }
        },
        getImageRes = { viewModel.getProductImageRes(it) }
    )
}

@Composable
fun ConfirmationContent(
    product: Product?,
    machine: Machine?,
    isRent: Boolean,
    userBalance: Double,
    maxReturnDate: String,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onConfirmAndPay: () -> Unit,
    getImageRes: (String) -> Int
) {
    val titleText = if (isRent) "Conferma Noleggio" else "Conferma Acquisto"
    val buttonText = if (isRent) "Paga e Noleggia" else "Paga e Acquista"
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    var showConfirmDialog by remember { mutableStateOf(false) }

    GraphPaperBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { StickerTopBar(titleText, onBackClick) },
            bottomBar = {
                if (!isLoading && product != null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp)) {
                        Button(
                            // --- MODIFICA ONCLICK PER ATTIVARE IL DIALOG ---
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(2.dp, outline, RoundedCornerShape(24.dp))
                                .shadow(8.dp, RoundedCornerShape(18.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.VioletIntense,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { paddingValues ->
            if (isLoading || product == null || machine == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                ) {
                    // Card Principale
                    Card(
                        colors = CardDefaults.cardColors(containerColor = paper),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        modifier = Modifier.fillMaxWidth().border(2.dp, outline, RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(22.dp)) {
                            // Header Prodotto
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = getImageRes(product.imageResName)),
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    MachinePill(name = machine.name)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp), color = Color(0xFFF0F0F0))

                            // Prezzo
                            Text(
                                if (isRent) "Cauzione (importo scalato)" else "Totale da pagare subito",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text("${product.pricePurchase} €", fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = Color.Black)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Saldo Residuo
                            WalletStatusCardSticker(userBalance, product.pricePurchase)

                            // --- SEZIONE NOLEGGIO ---
                            if (isRent) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp), color = Color(0xFFF0F0F0))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painter = painterResource(R.drawable.ic_rent), contentDescription = null, tint = Color(0xFF6A5AE0))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Piano Rimborsi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Se restituisci entro:", fontSize = 13.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))

                                // TABELLA RIMBORSI
                                val percentages = listOf(0.80, 0.72, 0.64, 0.56, 0.48, 0.40)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    percentages.forEachIndexed { index, percent ->
                                        val amount = product.pricePurchase * percent
                                        val day = index + 1
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("$day° Giorno", fontSize = 14.sp, color = Color.DarkGray)
                                            Text(
                                                "Rimborso ${String.format("%.2f", amount)}€",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                        if (index < percentages.size - 1) {
                                            HorizontalDivider(color = Color.LightGray.copy(alpha=0.4f))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Dopo il 6° giorno non è previsto alcun rimborso.", fontSize = 11.sp, color = Color.Gray, lineHeight = 14.sp)
                                Text("Scadenza massima: $maxReturnDate", fontSize = 12.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(90.dp))
                }

            }


        }

    }
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Conferma Operazione", fontWeight = FontWeight.Black) },
            text = { Text("Sei sicuro di voler procedere con il ${if (isRent) "noleggio" else "acquisto"}?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onConfirmAndPay() // Esegue il pagamento solo qui
                }) {
                    Text("SÌ, PROCEDI", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("ANNULLA", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

// Helpers
@Composable
private fun StickerTopBar(title: String, onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
        IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
        }
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun WalletStatusCardSticker(currentBalance: Double, cost: Double) {
    val finalBalance = currentBalance - cost
    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)).padding(14.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Tuo Saldo", color = Color.Gray)
            Row {
                Text("${currentBalance.toInt()}€", color = Color.Gray)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.padding(horizontal = 8.dp).size(16.dp))
                Text("${String.format("%.2f", finalBalance)}€", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MachinePill(name: String) {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(99.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
        Text(name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onTertiary)
    }
}