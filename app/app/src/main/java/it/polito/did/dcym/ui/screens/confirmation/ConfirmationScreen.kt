package it.polito.did.dcym.ui.screens.confirmation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.did.dcym.R
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Product

@Composable
fun ConfirmationScreen(
    productId: String?,
    machineId: String?,
    isRent: Boolean,
    onBackClick: () -> Unit,
    onFinalConfirmClick: () -> Unit,
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
        onFinalConfirmClick = onFinalConfirmClick,
        getImageRes = { viewModel.getProductImageRes(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationContent(
    product: Product?,
    machine: Machine?,
    isRent: Boolean,
    userBalance: Double,
    maxReturnDate: String,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onFinalConfirmClick: () -> Unit,
    getImageRes: (String) -> Int
) {
    val titleText = if (isRent) "Conferma Noleggio" else "Conferma Acquisto"
    val buttonText = if (isRent) "Paga e Noleggia" else "Paga e Acquista"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titleText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF3F0F9))
            )
        },
        bottomBar = {
            if (!isLoading && product != null) {
                PaddingValues(16.dp)
                Button(
                    onClick = onFinalConfirmClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFFF3F0F9)
    ) { paddingValues ->
        if (isLoading || product == null || machine == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                // CARD PRINCIPALE
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        // 1. INFO PRODOTTO COMPATTE
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = getImageRes(product.imageResName)),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painterResource(R.drawable.ic_distributori), null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(machine.name, fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color(0xFFF0F0F0))

                        // 2. TOTALE DA PAGARE (Ben visibile)
                        Text("Totale da pagare subito", fontSize = 14.sp, color = Color.Gray)
                        Text("${product.pricePurchase.toInt()} €", fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = Color.Black)

                        Spacer(modifier = Modifier.height(24.dp))

                        // 3. BOX SALDO (Senza verde, senza barrato, solo freccia)
                        WalletStatusCard(
                            currentBalance = userBalance,
                            cost = product.pricePurchase
                        )

                        // 4. SEZIONE NOLEGGIO DETTAGLIATA
                        if (isRent) {
                            Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color(0xFFF0F0F0))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(R.drawable.ic_rent), null, tint = Color(0xFF6A5AE0))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Piano Rimborsi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // NUOVA DESCRIZIONE PIÙ CHIARA
                            Text(
                                "L'importo del rimborso diminuisce col passare dei giorni. Restituisci prima per ottenere di più.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Scadenza: $maxReturnDate", fontSize = 13.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(16.dp))

                            // TABELLA
                            val percentages = listOf(80, 72, 64, 56, 48, 40)
                            percentages.forEachIndexed { index, percent ->
                                val day = index + 1
                                val refundAmount = product.pricePurchase * (percent / 100.0)
                                FullRefundRow(day = day, percent = percent, amount = refundAmount)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun WalletStatusCard(currentBalance: Double, cost: Double) {
    val finalBalance = currentBalance - cost

    Container(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF5F5F5), // Sfondo Grigio Chiaro
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_profile), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tuo Saldo", fontWeight = FontWeight.SemiBold, color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Prima (Grigio, semplice)
                Text(
                    "${currentBalance.toInt()}€",
                    fontSize = 15.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(12.dp))
                // Freccina
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(12.dp))

                // Dopo (Nero, Bold -> Neutro, non è un guadagno)
                Text(
                    "${String.format("%.2f", finalBalance)}€",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black // Nero = Neutro
                )
            }
        }
    }
}

// Helper container
@Composable
fun Container(modifier: Modifier, color: Color, shape: androidx.compose.ui.graphics.Shape, content: @Composable () -> Unit) {
    Surface(modifier = modifier, color = color, shape = shape, content = content)
}

@Composable
fun FullRefundRow(day: Int, percent: Int, amount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp), // Un po' più di aria
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Giorno $day", fontSize = 13.sp, color = Color.DarkGray)
        Row {
            Text("$percent%", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(end = 12.dp))
            // QUI IL VERDE: Perché sono soldi che tornano in tasca!
            Text("+${String.format("%.2f", amount)}€", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
        }
    }
}