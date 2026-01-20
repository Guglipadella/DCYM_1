package it.polito.did.dcym.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.ui.components.BottomNavBar
import it.polito.did.dcym.ui.components.BottomTab
import it.polito.did.dcym.ui.components.NavBarMode
import it.polito.did.dcym.ui.theme.AppColors

// 1) SCREEN
@Composable
fun ProductDetailScreen(
    productId: String?,
    onBackClick: () -> Unit,
    onMachineSelect: (String) -> Unit,
    onMachineInfoClick: (String) -> Unit,
    onGoToHomeChoice: () -> Unit,
    onGoToCatalog: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToHelp: () -> Unit,
    viewModel: ProductDetailViewModel = viewModel()
) {
    LaunchedEffect(productId) {
        productId?.toIntOrNull()?.let { viewModel.loadProductData(it) }
    }
    val uiState by viewModel.uiState.collectAsState()

    ProductDetailContent(
        product = uiState.product,
        availableMachines = uiState.availableMachines,
        isLoading = uiState.isLoading,
        onBackClick = onBackClick,
        onMachineSelect = onMachineSelect,
        onMachineInfoClick = onMachineInfoClick,
        onGoToHomeChoice = onGoToHomeChoice,
        onGoToCatalog = onGoToCatalog,
        onGoToProfile = onGoToHomeChoice,
        onGoToHistory = {},
        onGoToHelp = {}
    )
}

// 2) UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailContent(
    product: Product?,
    availableMachines: List<Machine>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onMachineSelect: (String) -> Unit,
    onMachineInfoClick: (String) -> Unit,
    onGoToHomeChoice: () -> Unit,
    onGoToCatalog: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToHelp: () -> Unit,
) {
    val bg = Color.Transparent

    Scaffold(
        containerColor = bg,
        topBar = {
            // Titolo centrato anche con icona a sinistra
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
                    text = "Disponibilità prodotto",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
,
        bottomBar = {
            BottomNavBar(
                mode = NavBarMode.PRODUCT_FLOW,
                selectedTab = BottomTab.CATALOGO,
                onFabClick = onGoToHomeChoice,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomTab.CATALOGO -> onGoToCatalog()
                        BottomTab.PROFILE -> onGoToProfile()
                        BottomTab.HISTORY -> onGoToHistory()
                        BottomTab.HELP -> onGoToHelp()
                        else -> {} // in PRODUCT_FLOW ignoro il resto
                    }
                }
            )
        }
    ) { paddingValues ->

        if (isLoading || product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                ProductTitleCard(product = product)
            }

            item {
                ProductImageCard(product = product)
            }

            item {
                InfoCard(
                    title = "Informazioni",
                    // costo coerente: pill gialla + descrizione sotto
                    topPillText = "Costo: ${product.pricePurchase.toInt()}€",
                    body = product.description
                )
            }

            item {
                SectionTitle(text = "Disponibile in")
            }

            if (availableMachines.isEmpty()) {
                item {
                    InfoCard(
                        title = "Nessuna macchinetta trovata",
                        body = "Al momento questo prodotto non risulta disponibile in nessun distributore."
                    )
                }
            } else {
                items(availableMachines, key = { it.id }) { machine ->
                    MachineAvailabilityCard(
                        machine = machine,
                        productId = product.id,
                        onActionClick = { onMachineSelect(machine.id) },
                        onInfoClick = { onMachineInfoClick(machine.id) }
                    )
                }
            }
        }
    }
}

/* -----------------------------
   HEADER STILE “CARD”
   ----------------------------- */
@Composable
private fun DetailHeader(
    title: String,
    onBackClick: () -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val text = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = text
                )
            }

            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = text,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

/* -----------------------------
   TITOLO PRODOTTO (CENTRATO + PIU’ GRANDE)
   ----------------------------- */
@Composable
private fun ProductTitleCard(product: Product) {
    val displayName = if (product.name.trim().equals("Calcolatrice", ignoreCase = true))
        "Calcolatrice"
    else
        product.name

    Text(
        text = displayName,
        fontSize = 22.sp,                // <- un filo meno di 24 (più bilanciato)
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        textAlign = TextAlign.Center,     // <- più semplice di wrapContentWidth
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

/* -----------------------------
   IMMAGINE (NO QUADRATO BIANCO “PIENO”)
   ----------------------------- */
@Composable
private fun ProductImageCard(product: Product) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface

    val context = LocalContext.current
    val imgRes = remember(product.imageResName) {
        val id = context.resources.getIdentifier(product.imageResName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.ic_logo_png_dcym
    }

    // contenitore "carta" leggero (non bianco pieno enorme)
    Box(
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imgRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/* -----------------------------
   CARD INFO (con pill gialla opzionale)
   ----------------------------- */
@Composable
private fun InfoCard(
    title: String,
    body: String,
    topPillText: String? = null
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface

    Card(
        colors = CardDefaults.cardColors(containerColor = paper),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outline, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (topPillText != null) {
                    StickerPill(
                        text = topPillText,
                        bg = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = body,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Black,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 2.dp, start = 2.dp)
    )
}

/* -----------------------------
   CARD MACCHINETTA (più ordinata)
   ----------------------------- */
@Composable
fun MachineAvailabilityCard(
    machine: Machine,
    productId: Int,
    onActionClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface

    val badgeOrange = MaterialTheme.colorScheme.tertiary
    val badgeOrangeText = MaterialTheme.colorScheme.onTertiary

    val badgeGreen = MaterialTheme.colorScheme.secondary
    val badgeGreenText = MaterialTheme.colorScheme.onSecondary

    val stock = machine.getStockForProduct(productId)
    val distance = "100 m"

    Card(
        colors = CardDefaults.cardColors(containerColor = paper),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outline, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onInfoClick() }
            ) {
                Text(
                    text = machine.name,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StickerPill(
                        text = if (stock == 1) "solo 1 disponibile" else "$stock disponibili",
                        bg = AppColors.GreenPastelMuted,
                        textColor = badgeGreenText
                    )
                    StickerPill(
                        text = distance,
                        bg = badgeOrange,
                        textColor = badgeOrangeText
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Tocca per info distributore",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            Spacer(Modifier.width(10.dp))

            // destra: stato + CTA (affiancati)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.ic_link_broken),
                    contentDescription = "Offline",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(14.dp))
                        .border(2.dp, outline, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onActionClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "Vai",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

        }
    }
}

/* -----------------------------
   PILL “STICKER”
   ----------------------------- */
@Composable
private fun StickerPill(
    text: String,
    bg: Color,
    textColor: Color
) {
    val outline = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .shadow(2.dp, CircleShape)                 // <- meno shadow
            .background(bg, CircleShape)
            .border(2.dp, outline, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp) // <- meno altezza
    ) {
        Text(
            text = text,
            fontSize = 11.sp,                          // <- più coerente
            fontWeight = FontWeight.SemiBold,          // <- NON Black
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

