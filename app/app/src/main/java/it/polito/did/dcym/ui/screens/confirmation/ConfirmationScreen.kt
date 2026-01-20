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
import androidx.compose.ui.draw.clip
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

    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val bg = MaterialTheme.colorScheme.background

    GraphPaperBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                StickerTopBar(
                    title = titleText,
                    onBackClick = onBackClick
                )
            },
            bottomBar = {
                if (!isLoading && product != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 18.dp)
                    ) {
                        Button(
                            onClick = onFinalConfirmClick,
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 18.dp),
            ) {
                // CARD PRINCIPALE (sticker)
                Card(
                    colors = CardDefaults.cardColors(containerColor = paper),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, outline, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {

                        // 1) INFO PRODOTTO
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

                        Divider(
                            modifier = Modifier.padding(vertical = 18.dp),
                            color = Color(0xFFF0F0F0)
                        )

                        // 2) TOTALE (come prima, colori uguali)
                        Text("Totale da pagare subito", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            "${product.pricePurchase.toInt()} €",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3) WALLET (sticker style, testi stessi colori)
                        WalletStatusCardSticker(
                            currentBalance = userBalance,
                            cost = product.pricePurchase
                        )

                        // 4) NOLEGGIO (solo se isRent)
                        if (isRent) {
                            Divider(
                                modifier = Modifier.padding(vertical = 18.dp),
                                color = Color(0xFFF0F0F0)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_rent),
                                    contentDescription = null,
                                    tint = Color(0xFF6A5AE0)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Piano Rimborsi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "L'importo del rimborso diminuisce col passare dei giorni. Restituisci prima per ottenere di più.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "Scadenza: $maxReturnDate",
                                fontSize = 13.sp,
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // TABELLA (sticker row)
                            val percentages = listOf(80, 72, 64, 56, 48, 40)
                            percentages.forEachIndexed { index, percent ->
                                val day = index + 1
                                val refundAmount = product.pricePurchase * (percent / 100.0)
                                FullRefundRowSticker(
                                    day = day,
                                    percent = percent,
                                    amount = refundAmount
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

/* -----------------------------
   TOPBAR TRASPARENTE + TITOLO CENTRATO
   ----------------------------- */
@Composable
private fun StickerTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Indietro",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
}

/* -----------------------------
   WALLET CARD (STICKER)
   ----------------------------- */
@Composable
private fun WalletStatusCardSticker(currentBalance: Double, cost: Double) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val finalBalance = currentBalance - cost
    val accent = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(paper, RoundedCornerShape(16.dp))
            .border(2.dp, outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.user),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tuo Saldo", fontWeight = FontWeight.SemiBold, color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${currentBalance.toInt()}€",
                    fontSize = 15.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Black
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    "${String.format("%.2f", finalBalance)}€",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }
    }
}
@Composable
private fun MachinePill(name: String) {
    val outline = MaterialTheme.colorScheme.outline
    val bg = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onTertiary

    Row(
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(999.dp))
            .background(bg, RoundedCornerShape(999.dp))
            .border(2.dp, outline, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_distributori),
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = textColor
        )
    }
}

/* -----------------------------
   RIGA RIMBORSO (STICKER ROW)
   ----------------------------- */
@Composable
private fun FullRefundRowSticker(day: Int, percent: Int, amount: Double) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(14.dp))
            .background(paper, RoundedCornerShape(14.dp))
           .border(2.dp, outline, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Giorno $day", fontSize = 13.sp, color = Color.DarkGray)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$percent%",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    "+${String.format("%.2f", amount)}€",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50) // verde come ora
                )
            }
        }
    }
}
