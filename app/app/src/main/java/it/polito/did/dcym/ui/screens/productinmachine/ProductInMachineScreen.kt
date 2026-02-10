package it.polito.did.dcym.ui.screens.productinmachine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.ui.components.BottomNavBar
import it.polito.did.dcym.ui.components.BottomTab
import it.polito.did.dcym.ui.components.GraphPaperBackground
import it.polito.did.dcym.ui.components.NavBarMode
import it.polito.did.dcym.ui.theme.AppColors

@Composable
fun ProductInMachineScreen(
    productId: Int,
    machineId: String,
    onBackClick: () -> Unit,
    onOptionSelected: (Boolean) -> Unit, // true = rent, false = buy
    onGoToHomeChoice: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToHelp: () -> Unit,
    viewModel: ProductInMachineViewModel = viewModel()
) {
    LaunchedEffect(productId, machineId) {
        viewModel.loadData(productId, machineId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val product = uiState.product
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                mode = NavBarMode.MACHINE_FLOW,
                selectedTab = BottomTab.MACHINES,
                hasActiveRentals = uiState.hasActiveRentals,
                onFabClick = onGoToHomeChoice,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomTab.MACHINES -> onBackClick() // Torna al catalogo macchinetta
                        BottomTab.PROFILE -> onGoToProfile()
                        BottomTab.HISTORY -> onGoToHistory()
                        BottomTab.HELP -> onGoToHelp()
                        else -> {}
                    }
                }
            )
        }
    ) { paddingValues ->
        GraphPaperBackground {
            if (uiState.isLoading || product == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- HEADER (Come Detail) ---
                    item {
                        DetailHeader(title = "Dettaglio prodotto", onBackClick = onBackClick)
                    }

                    // --- TITOLO E PREZZO (Come Detail) ---
                    item {
                        ProductTitleCard(product = product)
                    }

                    // --- IMMAGINE (Come Detail) ---
                    item {
                        ProductImageCard(product = product)
                    }

                    // --- DESCRIZIONE (Come Detail) ---
                    item {
                        InfoCard(title = "Informazioni", body = product.description)
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Scegli un'opzione",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // --- OPZIONE ACQUISTO (Come PurchaseOptions) ---
                    item {
                        OptionCardSticker(
                            title = "Acquisto",
                            subtitle = "Il prodotto sarà tuo per sempre",
                            price = product.pricePurchase,
                            userBalance = uiState.userBalance,
                            buttonText = "ACQUISTA ORA",
                            pricePillBg = MaterialTheme.colorScheme.secondary, // Giallo
                            pricePillText = MaterialTheme.colorScheme.onSecondary,
                            onClick = { onOptionSelected(false) } // isRent = false
                        )
                    }

                    // --- OPZIONE NOLEGGIO (Se disponibile) ---
                    if (product.priceRent != null) {
                        item {
                            OptionCardSticker(
                                title = "Noleggio",
                                subtitle = "Usa il prodotto e restituiscilo",
                                price = product.priceRent,
                                userBalance = uiState.userBalance,
                                buttonText = "NOLEGGIA ORA",
                                pricePillBg = AppColors.VioletPastelMuted,
                                pricePillText = Color.White,
                                onClick = { onOptionSelected(true) }, // isRent = true
                                extraContent = {
                                    Column {
                                        Text(
                                            text = "Rimborsi (anteprima)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        RefundRowSticker("Ottime condizioni", "+ ${String.format("%.1f", product.pricePurchase * 0.5)}€", AppColors.GreenPastelMuted)
                                        Spacer(Modifier.height(6.dp))
                                        RefundRowSticker("Danneggiato", "+ 0.00€", Color(0xFFEF9A9A))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// COMPONENTI UI (Copiati e adattati per garantire il mix perfetto)
// -------------------------------------------------------------------------

@Composable
private fun DetailHeader(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
        }
        Spacer(Modifier.width(16.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ProductTitleCard(product: Product) {
    val outline = MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = product.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 28.sp
        )
        Spacer(Modifier.width(12.dp))
        // Sticker Giallo Prezzo
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(14.dp))
                .background(Color(0xFFFFD54F), RoundedCornerShape(14.dp))
                .border(2.dp, outline, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("${product.pricePurchase.toInt()}€", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black)
        }
    }
}

@Composable
private fun ProductImageCard(product: Product) {
    val context = LocalContext.current
    val imgRes = remember(product.imageResName) {
        val id = context.resources.getIdentifier(product.imageResName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.ic_logo_png_dcym
    }
    // Stile "Cartolina" pulito
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(220.dp)) {
        Image(
            painter = painterResource(id = imgRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    Card(
        colors = CardDefaults.cardColors(containerColor = paper),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().border(2.dp, outline, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text(body, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }
}

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
    val remaining = userBalance - price
    val remainingColor = Color.Gray

    Card(
        colors = CardDefaults.cardColors(containerColor = paper),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth().border(2.dp, outline, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(Modifier.width(12.dp))
                // Prezzo Pillola piccola
                Box(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(10.dp))
                        .background(pricePillBg, RoundedCornerShape(10.dp))
                        .border(1.dp, outline, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("${if(price % 1.0 == 0.0) price.toInt() else price}€", fontSize = 16.sp, fontWeight = FontWeight.Black, color = pricePillText)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Resto
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dopo questa scelta:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Text("Resto: ${String.format("%.2f", remaining)}€", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = remainingColor)
            }

            extraContent?.let {
                Spacer(Modifier.height(14.dp))
                Divider(color = outline.copy(alpha = 0.2f))
                Spacer(Modifier.height(14.dp))
                it()
            }

            Spacer(Modifier.height(16.dp))

            // Bottone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(14.dp))
                    .border(2.dp, outline, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onClick() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(buttonText, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}

@Composable
private fun RefundRowSticker(left: String, right: String, rightColor: Color) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(10.dp))
            .background(paper, RoundedCornerShape(10.dp))
            .border(1.dp, outline.copy(alpha=0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(left, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(right, fontSize = 13.sp, fontWeight = FontWeight.Black, color = rightColor)
    }
}