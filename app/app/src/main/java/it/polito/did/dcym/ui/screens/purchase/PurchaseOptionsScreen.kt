package it.polito.did.dcym.ui.screens.purchase

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
    // Background a quadretti visibile: Scaffold trasparente + niente background pieni nei contenuti
    GraphPaperBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Titolo centrato senza barra bianca
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        text = "Scegli opzione",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Center)
                    )
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
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // PILL MACCHINETTA
                StickerPill(
                    text = machine.name,
                    iconRes = R.drawable.ic_distributori,
                    bg = MaterialTheme.colorScheme.tertiary,
                    textColor = MaterialTheme.colorScheme.onTertiary
                )

                Spacer(Modifier.height(12.dp))

                // IMMAGINE PRODOTTO (niente riquadro pieno dietro)
                Image(
                    painter = painterResource(id = getImageRes(product.imageResName)),
                    contentDescription = null,
                    modifier = Modifier.size(110.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(10.dp))

                // NOME PRODOTTO
                Text(
                    text = product.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                // SALDO (pill gialla coerente)
                val outline = MaterialTheme.colorScheme.outline
                Box(
                    modifier = Modifier
                        .shadow(3.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .border(2.dp, outline, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "Tuo saldo: ${String.format("%.2f", userBalance)} €",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                Spacer(Modifier.height(18.dp))

                // NOLEGGIO (solo se noleggiabile)
                if (product.priceRent != null) {
                    OptionCardSticker(
                        title = "Noleggio",
                        subtitle = "Paghi la cauzione, rimborso alla restituzione.",
                        price = product.pricePurchase, // cauzione = prezzo pieno
                        userBalance = userBalance,
                        buttonText = "Seleziona noleggio",
                        onClick = onConfirmRent,
                        pricePillBg = MaterialTheme.colorScheme.secondary,
                        pricePillText = MaterialTheme.colorScheme.onSecondary,
                        extraContent = {
                            val paper = MaterialTheme.colorScheme.surface
                            val text = MaterialTheme.colorScheme.onSurface
                            val green = Color(0xFF4CAF50)

                            // Box “attenzione” leggibile
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(3.dp, RoundedCornerShape(14.dp))
                                    .background(paper, RoundedCornerShape(14.dp))
                                    .border(2.dp, outline, RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Restituisci entro: $maxReturnDate",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFD32F2F)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Dopo questa data, il prodotto sarà considerato acquistato.",
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = text.copy(alpha = 0.78f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Rimborsi (anteprima)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.height(8.dp))

                            RefundRowSticker(
                                left = "Massimo (1° giorno)",
                                right = "Rimborso    ${(product.pricePurchase * 0.80).toInt()}€",
                                rightColor = green
                            )

                            Spacer(Modifier.height(8.dp))

                            RefundRowSticker(
                                left = "Minimo (6° giorno)",
                                right = "Rimborso    ${(product.pricePurchase * 0.40).toInt()}€",
                                rightColor = green
                            )
                        }
                    )

                    Spacer(Modifier.height(14.dp))
                }

                // ACQUISTO (sempre)
                OptionCardSticker(
                    title = "Acquisto",
                    subtitle = "Il prodotto diventa subito tuo.",
                    price = product.pricePurchase,
                    userBalance = userBalance,
                    buttonText = "Seleziona acquisto",
                    onClick = onConfirmPurchase,
                    pricePillBg = MaterialTheme.colorScheme.secondary,
                    pricePillText = MaterialTheme.colorScheme.onSecondary
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/* -----------------------------
   BACKGROUND A QUADRETTI (LEGGERO)
   ----------------------------- */
@Composable
private fun GraphPaperBackground(content: @Composable BoxScope.() -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    val gridMinor = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
    val gridMajor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(bg)

            val step = 18.dp.toPx()
            val majorStep = step * 5

            var x = 0f
            while (x <= size.width) {
                val isMajor = (x % majorStep) < 1f
                drawLine(
                    color = if (isMajor) gridMajor else gridMinor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = if (isMajor) 1.5f else 1f
                )
                x += step
            }

            var y = 0f
            while (y <= size.height) {
                val isMajor = (y % majorStep) < 1f
                drawLine(
                    color = if (isMajor) gridMajor else gridMinor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (isMajor) 1.5f else 1f
                )
                y += step
            }
        }

        content()
    }
}

/* -----------------------------
   PILL MACCHINETTA
   ----------------------------- */
@Composable
private fun StickerPill(
    text: String,
    iconRes: Int,
    bg: Color,
    textColor: Color
) {
    val outline = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .shadow(3.dp, CircleShape)
            .background(bg, CircleShape)
            .border(2.dp, outline, CircleShape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            maxLines = 1
        )
    }
}

/* -----------------------------
   CARD OPZIONE “STICKER” (LEGGIBILE)
   ----------------------------- */
@Composable
private fun OptionCardSticker(
    title: String,
    subtitle: String,
    price: Double,
    userBalance: Double,
    buttonText: String,
    onClick: () -> Unit,
    pricePillBg: Color,
    pricePillText: Color,
    extraContent: @Composable (() -> Unit)? = null
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val text = MaterialTheme.colorScheme.onSurface
    val priceHighlight = Color(0xFFFFD54F)

    val remaining = userBalance - price
    // MODIFICA QUI: Il resto è sempre grigio, neutro.
    val remainingColor = Color.Gray

    Card(
        colors = CardDefaults.cardColors(containerColor = paper),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outline, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // HEADER (Titolo + Prezzo Giallo)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = text
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = text.copy(alpha = 0.74f)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(priceHighlight, RoundedCornerShape(12.dp))
                        .border(2.dp, outline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${price.toInt()}€",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // RESTO (Ora Grigio)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dopo questa scelta:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = text.copy(alpha = 0.70f)
                )
                Text(
                    text = "Resto: ${String.format("%.0f", remaining)}€",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, // Meno pesante di Black
                    color = remainingColor // Grigio
                )
            }

            extraContent?.let {
                Spacer(Modifier.height(14.dp))
                Divider(color = outline.copy(alpha = 0.25f), thickness = 1.dp)
                Spacer(Modifier.height(14.dp))
                it()
            }

            Spacer(Modifier.height(16.dp))

            // BOTTONE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(14.dp))
                    .border(2.dp, outline, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText.uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

/* -----------------------------
   RIGA RIMBORSO (BOX LEGGIBILE)
   ----------------------------- */
@Composable
private fun RefundRowSticker(
    left: String,
    right: String,
    rightColor: Color
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .background(paper, RoundedCornerShape(12.dp))
            .border(2.dp, outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp), // Più spazio interno
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = left,
            fontSize = 14.sp, // Aumentato da 12 a 14
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = right,
            fontSize = 14.sp, // Aumentato da 12 a 14
            fontWeight = FontWeight.Black, // Più evidente
            color = rightColor
        )
    }
}
