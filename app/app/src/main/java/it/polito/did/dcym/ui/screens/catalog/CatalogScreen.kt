package it.polito.did.dcym.ui.screens.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import it.polito.did.dcym.data.model.Category
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.ui.components.BottomNavBar
import it.polito.did.dcym.ui.components.BottomTab
import it.polito.did.dcym.ui.components.NavBarMode
import it.polito.did.dcym.ui.theme.AppColors
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (Int) -> Unit,
    onGoToHomeChoice: () -> Unit,
    onGoToCatalog: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToHelp: () -> Unit,
    viewModel: CatalogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val gridState = rememberLazyGridState()
    val showScrollToTop by remember { derivedStateOf { gridState.firstVisibleItemIndex > 0 } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // lasciamo solo spazio in alto (header lo gestiamo nel body)
            Spacer(Modifier.height(0.dp))
        },
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


        },
        floatingActionButton = {
            AnimatedVisibility(visible = showScrollToTop, enter = fadeIn(), exit = fadeOut()) {
                FloatingActionButton(
                    onClick = { scope.launch { gridState.animateScrollToItem(0) } },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Torna su")
                }
            }
        }
    ) { paddingValues ->

        GraphPaperBackground {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(10.dp))

                // HEADER "CARD"
                CatalogHeaderCard(
                    userName = "Mario",
                    balanceText = "La tua paghetta: 20€",
                    onProfileClick = { /* se vuoi: nav al profilo */ }
                )

                Spacer(Modifier.height(14.dp))

                // SEZIONE RICERCA
                Text(
                    text = "Catalogo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(10.dp))

                CatalogSearchBar(
                    searchText = uiState.searchQuery,
                    onSearchChange = { viewModel.onSearchQueryChange(it) }
                )

                Spacer(Modifier.height(12.dp))

                // CHIPS (senza titolo "Categorie")
                FiltersRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { viewModel.selectFilter(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // GRIGLIA PRODOTTI
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        top = 10.dp,     // IMPORTANTISSIMO per non troncare il cuore
                        bottom = 16.dp
                    )
                ) {
                    items(
                        items = uiState.products,
                        key = { product -> "${product.id}_${product.isFavorite}" }
                    ) { product ->
                        val context = LocalContext.current
                        val imageResId = remember(product.imageResName) {
                            context.resources.getIdentifier(product.imageResName, "drawable", context.packageName)
                        }

                        ProductCard(
                            product = product,
                            imageResId = if (imageResId != 0) imageResId else R.drawable.ic_logo_png_dcym,
                            onClick = { onProductClick(product.id) },
                            onFavoriteClick = {
                                viewModel.toggleFavorite(product.id)
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(
                                        if (!product.isFavorite) "Aggiunto ai preferiti!" else "Rimosso dai preferiti"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/* -----------------------------
   HEADER CARD
   ----------------------------- */
@Composable
private fun CatalogHeaderCard(
    userName: String,
    balanceText: String,
    onProfileClick: () -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val accent = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .background(paper, RoundedCornerShape(18.dp))
            .border(2.dp, outline, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ciao $userName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Don't Call Your Mom!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(10.dp))

                // sticker paghetta
                Box(
                    modifier = Modifier
                        .shadow(3.dp, CircleShape)
                        .background(accent, CircleShape)
                        .border(2.dp, outline, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = balanceText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            // profilo grande a destra
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(6.dp, CircleShape)
                    .background(paper, CircleShape)
                    .border(2.dp, outline, CircleShape)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.user), // <-- user.png
                    contentDescription = "Profilo",
                    modifier = Modifier.fillMaxSize(),          // riempie il cerchio
                    contentScale = ContentScale.Crop            // taglio “avatar”
                )
            }
        }
    }
}

/* -----------------------------
   SEARCH BAR (più stretta)
   ----------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogSearchBar(
    searchText: String,
    onSearchChange: (String) -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TextField(
            value = searchText,
            onValueChange = onSearchChange,
            placeholder = { Text("cerca un prodotto") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            },
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(54.dp)
                .shadow(3.dp, RoundedCornerShape(18.dp))
                .border(2.dp, outline, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = paper,
                unfocusedContainerColor = paper,
                disabledContainerColor = paper,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            maxLines = 1
        )
    }
}

/* -----------------------------
   FILTRI “STICKER CHIPS”
   ----------------------------- */
@Composable
fun FiltersRow(
    selectedFilter: CatalogFilter,
    onFilterSelected: (CatalogFilter) -> Unit
) {
    val filters = listOf(CatalogFilter.All) +
            Category.values().map { CatalogFilter.ByCategory(it) } +
            CatalogFilter.Favorites

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 5.dp)
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            StickerChip(
                text = filter.label,
                selected = isSelected,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun StickerChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val selectedBg = AppColors.VioletPastelMuted
    val paper = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .then(if (selected) Modifier.shadow(3.dp, CircleShape) else Modifier)
            .background(if (selected) selectedBg else paper, CircleShape)
            .border(2.dp, outline, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
        )
    }
}

/* -----------------------------
   CARD PRODOTTO (più compatta)
   ----------------------------- */
@Composable
fun ProductCard(
    product: Product,
    imageResId: Int,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val badge = AppColors.GreenPastelMuted
    val badgeText = MaterialTheme.colorScheme.onTertiary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, end = 2.dp)
    ) {
        Card(
            onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = paper),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.88f)
                .border(2.dp, outline, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // riga top: badge (se serve)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (product.priceRent != null) {
                        Box(
                            modifier = Modifier
                                .shadow(2.dp, CircleShape)
                                .background(badge, CircleShape)
                                .border(2.dp, outline, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Noleggiabile",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = badgeText
                            )
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    // spazio per "bilanciare" visivamente con il cuore esterno
                    Spacer(Modifier.width(24.dp))
                }

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .fillMaxHeight(0.92f),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(4.dp))

                val outline = MaterialTheme.colorScheme.outline
                val priceBg = MaterialTheme.colorScheme.secondary
                val priceText = MaterialTheme.colorScheme.onSecondary

                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .shadow(2.dp, CircleShape)
                        .background(priceBg, CircleShape)
                        .border(2.dp, outline, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Costo: ${product.pricePurchase.toInt()}€",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = priceText
                    )
                }

            }
        }

        // cuore “sticker”
        Surface(
            shape = CircleShape,
            color = paper,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp)
                .size(38.dp)
                .border(2.dp, outline, CircleShape)
        ) {
            IconButton(onClick = onFavoriteClick, modifier = Modifier.padding(4.dp)) {
                Icon(
                    painter = painterResource(
                        id = if (product.isFavorite) R.drawable.ic_heart_full else R.drawable.ic_heart_empty
                    ),
                    contentDescription = "Preferito",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/* -----------------------------
   QUADERNO A QUADRETTI (LEGGERO)
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
