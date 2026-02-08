package it.polito.did.dcym.ui.screens.machinecatalog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.geometry.Offset
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
import it.polito.did.dcym.data.model.Category // Assicurati di avere questo import
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.ui.components.BottomNavBar
import it.polito.did.dcym.ui.components.BottomTab
import it.polito.did.dcym.ui.components.GraphPaperBackground
import it.polito.did.dcym.ui.components.NavBarMode
import it.polito.did.dcym.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
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
    LaunchedEffect(machineId) {
        viewModel.loadData(machineId)
    }

    val uiState by viewModel.uiState.collectAsState()

    // Lista categorie REALI + Preferiti
    val filters = listOf(MachineCatalogFilter.All) +
            Category.values().map { MachineCatalogFilter.ByCategory(it.name) } +
            MachineCatalogFilter.Favorites

    Scaffold(
        bottomBar = {
            BottomNavBar(
                mode = NavBarMode.MACHINE_FLOW,
                selectedTab = BottomTab.MACHINES,
                hasActiveRentals = uiState.hasActiveRentals,
                onFabClick = onGoToHomeChoice,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomTab.MACHINES -> onBackClick()
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
                Spacer(Modifier.height(12.dp))

                // --- HEADER: Indietro + Nome Macchinetta + SALDO ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Pulsante Indietro
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Info Macchinetta (Titolo)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.machineName.isNotEmpty()) uiState.machineName else "Caricamento...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Catalogo dedicato",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }

                    // --- SALDO (Paghetta) ---
                    Box(
                        modifier = Modifier
                            .shadow(3.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${uiState.userBalance.toInt()}€",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // --- BARRA DI RICERCA ---
                MachineCatalogSearchBar(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) }
                )

                Spacer(Modifier.height(16.dp))

                // --- FILTRI (Sticker Chips uguali al Catalogo) ---
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filters) { filter ->
                        val isSelected = uiState.selectedFilter == filter
                        val label = when (filter) {
                            is MachineCatalogFilter.All -> "Tutti"
                            is MachineCatalogFilter.Favorites -> "Preferiti"
                            is MachineCatalogFilter.ByCategory -> filter.category
                        }

                        FilterChip(
                            text = label,
                            isSelected = isSelected,
                            onClick = { viewModel.selectFilter(filter) }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // --- GRIGLIA PRODOTTI ---
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.filteredProducts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nessun prodotto trovato.", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(uiState.filteredProducts) { product ->
                            MachineProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(product.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTI UI ---

@Composable
fun MachineProductCard(
    product: Product,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val context = LocalContext.current

    val imageResId = remember(product.imageResName) {
        val id = context.resources.getIdentifier(product.imageResName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.ic_logo_png_dcym
    }

    Box {
        Card(
            onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = paper),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
                .border(2.dp, outline, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Badge Noleggio
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    if (product.priceRent != null) {
                        Box(
                            modifier = Modifier
                                .background(AppColors.GreenPastelMuted, CircleShape)
                                .border(1.dp, outline, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Noleggiabile", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
                        }
                    } else {
                        Spacer(Modifier.height(20.dp))
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Immagine
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.85f),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Nome
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(6.dp))

                // Prezzo
                Box(
                    modifier = Modifier
                        .shadow(2.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .border(2.dp, outline, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${product.pricePurchase.toInt()}€",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }

        // --- CUORE PREFERITI (In alto a destra, sovrapposto) ---
        Surface(
            shape = CircleShape,
            color = paper,
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp) // Leggermente fuori bordo
                .size(36.dp)
                .border(2.dp, outline, CircleShape)
                .clickable { onFavoriteClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(
                        id = if (product.isFavorite) R.drawable.ic_heart_full else R.drawable.ic_heart_empty
                    ),
                    contentDescription = "Preferito",
                    tint = Color.Unspecified, // Usa i colori originali dell'SVG/PNG
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    val bg = if (isSelected) AppColors.VioletPastelMuted else MaterialTheme.colorScheme.surface
    val txtColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .shadow(if(isSelected) 3.dp else 0.dp, CircleShape)
            .clip(CircleShape)
            .background(bg)
            .border(2.dp, outline, CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = txtColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineCatalogSearchBar(value: String, onValueChange: (String) -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Cerca in questa macchinetta...", fontSize = 14.sp, color = Color.Gray) },
        leadingIcon = {
            Icon(
                painterResource(android.R.drawable.ic_menu_search),
                null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = paper,
            unfocusedContainerColor = paper,
            disabledContainerColor = paper,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outline, RoundedCornerShape(18.dp))
            .shadow(3.dp, RoundedCornerShape(18.dp))
    )
}

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
                drawLine(if (isMajor) gridMajor else gridMinor, Offset(x, 0f), Offset(x, size.height), if (isMajor) 1.5f else 1f)
                x += step
            }
            var y = 0f
            while (y <= size.height) {
                val isMajor = (y % majorStep) < 1f
                drawLine(if (isMajor) gridMajor else gridMinor, Offset(0f, y), Offset(size.width, y), if (isMajor) 1.5f else 1f)
                y += step
            }
        }
        content()
    }
}