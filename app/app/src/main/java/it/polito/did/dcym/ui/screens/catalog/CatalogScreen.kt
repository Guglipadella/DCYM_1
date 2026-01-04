package it.polito.did.dcym.ui.screens.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import it.polito.did.dcym.data.model.Category
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.ui.components.BottomNavBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (Int) -> Unit,
    viewModel: CatalogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Per lanciare azioni asincrone (scroll, snackbar)

    // Stato per gestire lo scroll della griglia
    val gridState = rememberLazyGridState()

    // Mostra il pulsante "Torna su" solo se abbiamo sceso un po' (indice > 0)
    val showScrollToTop by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CatalogTopBar(
                searchText = uiState.searchQuery,
                onSearchChange = { viewModel.onSearchQueryChange(it) }
            )
        },
        bottomBar = {
            // Aggiungiamo la Navbar qui
            BottomNavBar()
        },
        floatingActionButton = {
            // Pulsante FRECCIA SU che compare/scompare
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            gridState.animateScrollToItem(0) // Torna al primo elemento
                        }
                    },
                    containerColor = Color(0xFFE0D4FC), // Lilla chiaro
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Torna su")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // TITOLO CATEGORIE
            Text(
                text = "Categorie",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // FILTRI (CHIPS)
            FiltersRow(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.selectFilter(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // GRIGLIA PRODOTTI
            LazyVerticalGrid(
                state = gridState, // Colleghiamo lo stato dello scroll
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = uiState.products,
                    key = { product -> "${product.id}_${product.isFavorite}" } // <--- KEY COMPOSTA
                ) { product ->
                    // CALCOLA L'ID RISORSA QUI
                    val context = LocalContext.current
                    val imageResId = remember(product.imageResName) {
                        context.resources.getIdentifier(product.imageResName, "drawable", context.packageName)
                    }
                    // Se imageResId è 0 (non trovata), usa un fallback

                    ProductCard(
                        product = product,
                        // Passiamo l'ID calcolato invece di product.imageRes
                        imageResId = if (imageResId != 0) imageResId else R.drawable.ic_logo_png_dcym,
                        onClick = { onProductClick(product.id) },
                        onFavoriteClick = {
                            viewModel.toggleFavorite(product.id)
                            scope.launch {
                                if (!product.isFavorite) {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar("Aggiunto ai preferiti!")
                                } else {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar("Rimosso dai preferiti")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun FiltersRow(
    selectedFilter: CatalogFilter,
    onFilterSelected: (CatalogFilter) -> Unit
) {
    val filters = listOf(CatalogFilter.All) +
            Category.values().map { CatalogFilter.ByCategory(it) } +
            CatalogFilter.Favorites

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE0D4FC),
                    selectedLabelColor = Color.Black
                )
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    imageResId: Int,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    // 1. Box Padre: serve per sovrapporre il bottone alla card
    // Aggiungiamo padding top ed end per "fare spazio" al bottone che esce,
    // altrimenti verrebbe tagliato o si sovrapporrebbe alla card vicina.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, end = 8.dp) // Spazio extra per l'effetto "fuori bordo"
    ) {
        // 2. La Card del Prodotto (SOTTO)
        Card(
            onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f) // Leggermente più bassa per compensare il padding esterno
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Immagine
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = product.name,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp), // Spazio dall'alto per non toccare il bordo
                    contentScale = ContentScale.Fit
                )

                // Testi (Nome e Prezzi)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Acquisto: ${product.pricePurchase.toInt()} euro",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    if (product.priceRent != null) {
                        Text(
                            text = "Noleggio: ${product.priceRent.toInt()} euro",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // 3. Il Bottone Cuore "Galleggiante" (SOPRA)
        // Usiamo una Surface per fare il cerchio grigio di sfondo
        Surface(
            shape = CircleShape,
            color = Color(0xFFEBEBEB), // Grigio chiaro come da immagine
            shadowElevation = 2.dp, // Una piccola ombretta per staccarlo
            modifier = Modifier
                .align(Alignment.TopEnd) // Lo piazziamo in alto a destra nel Box padre
                .offset(x = 6.dp, y = (-6).dp) // Lo spostiamo: 6dp a destra (fuori), 6dp in alto (fuori)
                .size(36.dp) // Dimensione del cerchio
        ) {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.padding(4.dp) // Un po' di margine interno per l'icona
            ) {
                Icon(
                    painter = painterResource(
                        id = if (product.isFavorite) R.drawable.ic_heart_full else R.drawable.ic_heart_empty
                    ),
                    contentDescription = "Favorite",
                    tint = Color.Unspecified, // Mantiene i colori originali dell'SVG/PNG
                    modifier = Modifier.size(20.dp) // Dimensione dell'icona dentro il cerchio
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogTopBar(
    searchText: String,
    onSearchChange: (String) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't Call Your Mom!",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Icon(
                painter = painterResource(R.drawable.ic_profile),
                contentDescription = "Profile",
                modifier = Modifier.size(32.dp)
            )
        }
        Text("La tua paghetta: 20€", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar ora FUNZIONANTE
        TextField(
            value = searchText,
            onValueChange = onSearchChange, // Aggiorna il ViewModel
            placeholder = { Text("cerca un prodotto") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_search),
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 24.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            maxLines = 1
        )
    }
}