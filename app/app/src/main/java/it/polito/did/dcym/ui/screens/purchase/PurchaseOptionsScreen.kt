package it.polito.did.dcym.ui.screens.purchase

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.did.dcym.R
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Product

@Composable
fun PurchaseOptionsScreen(
    productId: String?,
    machineId: String?,
    onBackClick: () -> Unit,
    onConfirmPurchase: () -> Unit,
    onConfirmRent: () -> Unit,
    viewModel: PurchaseOptionsViewModel = viewModel()
) {
    LaunchedEffect(productId, machineId) {
        if (productId != null && machineId != null) {
            viewModel.loadData(productId.toInt(), machineId)
        }
    }
    val uiState by viewModel.uiState.collectAsState()

    PurchaseOptionsContent(
        product = uiState.product,
        machine = uiState.machine,
        userBalance = uiState.userBalance,
        maxReturnDate = uiState.maxReturnDate,
        isLoading = uiState.isLoading,
        onBackClick = onBackClick,
        onConfirmPurchase = onConfirmPurchase,
        onConfirmRent = onConfirmRent,
        getImageRes = { viewModel.getProductImageRes(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseOptionsContent(
    product: Product?,
    machine: Machine?,
    userBalance: Double,
    maxReturnDate: String,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onConfirmPurchase: () -> Unit,
    onConfirmRent: () -> Unit,
    getImageRes: (String) -> Int
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scegli Opzione", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF3F0F9))
            )
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. MACCHINETTA
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFE0D4FC),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_distributori), contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(machine.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    }
                }

                // 2. PRODOTTO
                Image(
                    painter = painterResource(id = getImageRes(product.imageResName)),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )
                Text(product.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                // Saldo spostato sotto
                Text("Tuo saldo attuale: ${String.format("%.2f", userBalance)} €", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp))

                // 3. OPZIONE NOLEGGIO (Solo se il prodotto è noleggiabile!)
                if (product.priceRent != null) {
                    OptionCard(
                        title = "Noleggia",
                        subtitle = "Paghi la cauzione, rimborsiamo alla restituzione.",
                        price = product.pricePurchase, // Cauzione = Prezzo Pieno
                        userBalance = userBalance,
                        buttonText = "Seleziona Noleggio",
                        onClick = onConfirmRent,
                        extraContent = {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Text("Restituibile entro il $maxReturnDate", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                Text("Dopo tale data il prodotto sarà considerato acquistato.", fontSize = 11.sp, color = Color.Gray, lineHeight = 12.sp, modifier = Modifier.padding(bottom = 12.dp))

                                // ANTEPRIMA RIMBORSI (Solo Min e Max come richiesto)
                                RefundRow(day = "Massimo (1° G.)", refund = "${(product.pricePurchase * 0.8).toInt()}€")
                                RefundRow(day = "Minimo (6° G.)", refund = "${(product.pricePurchase * 0.40).toInt()}€")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 4. OPZIONE ACQUISTO (Sempre presente)
                OptionCard(
                    title = "Acquista",
                    subtitle = "Il prodotto diventa subito tuo.",
                    price = product.pricePurchase,
                    userBalance = userBalance,
                    buttonText = "Seleziona Acquisto",
                    onClick = onConfirmPurchase
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ... OptionCard e RefundRow rimangono uguali al codice precedente ...
// Copia OptionCard e RefundRow dal messaggio precedente o chiedimi se ti servono
// Ricorda solo di tenere il colore del "Resto" in DarkGray come avevamo deciso.
@Composable
fun OptionCard(
    title: String,
    subtitle: String,
    price: Double,
    userBalance: Double,
    buttonText: String,
    onClick: () -> Unit,
    extraContent: @Composable (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(subtitle, fontSize = 12.sp, color = Color.Gray, lineHeight = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${price.toInt()} €", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    val remaining = userBalance - price
                    Text(
                        text = "Resto: ${String.format("%.0f", remaining)} €",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }
            extraContent?.invoke()
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun RefundRow(day: String, refund: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(day, fontSize = 12.sp, color = Color.Gray)
        Text("Rimborso $refund", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4CAF50))
    }
}