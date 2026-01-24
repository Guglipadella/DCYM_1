package it.polito.did.dcym.ui.screens.machinecatalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
fun MachineCatalogScreen(
    machineId: String,
    onBackClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    onGoToHomeChoice: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToHelp: () -> Unit,
    viewModel: MachineCatalogViewModel = viewModel()
) {
    // Carichiamo i dati appena entriamo
    LaunchedEffect(machineId) {
        viewModel.loadData(machineId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val categories = listOf("Tutti", "Elettronica", "Cartoleria", "Snack", "Bevande", "Utilita")

    Scaffold(
        bottomBar = {
            // Rimaniamo nel flusso MACCHINETTE
            BottomNavBar(
                mode = NavBarMode.MACHINE_FLOW,
                selectedTab = BottomTab.MACHINES,
                onFabClick = onGoToHomeChoice,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomTab.MACHINES -> onBackClick() // Torna alla mappa se clicchi macchine
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
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // --- HEADER MODIFICATO ---
                // Titolo "Catalogo" + Nome Macchinetta
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }

                    Text(
                        text = "Catalogo",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(Modifier.width(12.dp))

                    // Pillola Nome Macchinetta
                    if (uiState.machineName.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape) // Arancione Macchinette
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = uiState.machineName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- FILTRI CATEGORIE (Identico al Catalogo) ---
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = uiState.selectedCategory == category
                        val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        val txtColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        val border = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(bg)
                                .border(if (isSelected) 0.dp else 2.dp, border, CircleShape)
                                .clickable { viewModel.selectCategory(category) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = txtColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- LISTA PRODOTTI ---
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.filteredProducts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nessun prodotto disponibile in questa categoria.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(uiState.filteredProducts) { product ->
                            MachineProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Card Prodotto (Stile identico al Catalogo originale)
@Composable
fun MachineProductCard(product: Product, onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val context = LocalContext.current

    val imageResId = remember(product.imageResName) {
        val id = context.resources.getIdentifier(product.imageResName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.ic_logo_png_dcym
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = paper),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outline, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Immagine Prodotto
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(imageResId),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(16.dp))

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Prezzi
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Prezzo Acquisto
                    Text(
                        "${product.pricePurchase.toInt()}€",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    // Prezzo Noleggio (se esiste)
                    if (product.priceRent != null) {
                        Spacer(Modifier.width(8.dp))
                        Text("|", color = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_rent),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF6A5AE0)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${String.format("%.2f", product.priceRent)}€",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF6A5AE0)
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Bottone Freccia (Stile Catalogo)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                    .border(2.dp, outline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = Color.Black
                )
            }
        }
    }
}