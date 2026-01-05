package it.polito.did.dcym.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.did.dcym.R
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Product
import it.polito.did.dcym.ui.components.BottomNavBar

// 1. SCREEN INTELLIGENTE
@Composable
fun ProductDetailScreen(
    productId: String?,
    onBackClick: () -> Unit,
    onMachineSelect: (String) -> Unit, // Clic sulla freccia (va alla scelta acquisto)
    onMachineInfoClick: (String) -> Unit, // Clic sul nome (va alle info macchinetta - futuro)
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
        onMachineInfoClick = onMachineInfoClick
    )
}

// 2. CONTENUTO GRAFICO
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailContent(
    product: Product?,
    availableMachines: List<Machine>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onMachineSelect: (String) -> Unit,
    onMachineInfoClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Disponibilità prodotto", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF3F0F9))
            )
        },
        bottomBar = { BottomNavBar() },
        containerColor = Color(0xFFF3F0F9)
    ) { paddingValues ->
        if (isLoading || product == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TITOLO
                item {
                    Text(product.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                }

                // IMMAGINE
                item {
                    val context = LocalContext.current
                    val imgRes = remember(product.imageResName) {
                        val id = context.resources.getIdentifier(product.imageResName, "drawable", context.packageName)
                        if (id != 0) id else R.drawable.ic_logo_png_dcym
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(250.dp).padding(bottom = 24.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = imgRes),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            )
                        }
                    }
                }

                // INFO BASE
                item {
                    Text("Informazioni", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    Text(product.description, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp))
                }

                // LISTA MACCHINETTE
                item {
                    Text("Disponibile in:", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                }

                if (availableMachines.isEmpty()) {
                    item { Text("Nessuna macchinetta disponibile.", color = Color.Gray) }
                } else {
                    items(availableMachines) { machine ->
                        MachineAvailabilityCard(
                            machine = machine,
                            productId = product.id,
                            onActionClick = { onMachineSelect(machine.id) }, // Freccia
                            onInfoClick = { onMachineInfoClick(machine.id) } // Nome
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun MachineAvailabilityCard(
    machine: Machine,
    productId: Int,
    onActionClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val stock = machine.getStockForProduct(productId)
    // Placeholder per la distanza e l'indirizzo (presi dal modello o statici per ora)
    val distance = "100 m"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // PARTE SINISTRA: Cliccabile per INFO MACCHINETTA
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onInfoClick() } // Clicca qui per info macchinetta
            ) {
                Text(
                    text = machine.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$stock Disponibili • $distance",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // PARTE DESTRA: Icone e Azione (Link + Freccia)
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Icona Link Spezzato (Non connesso)
                Icon(
                    painter = painterResource(id = R.drawable.ic_link_broken), // Assicurati di averla importata
                    contentDescription = "Not Connected",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Tasto Freccia (Azione Principale)
                IconButton(
                    onClick = onActionClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEBEBEB), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right), // Assicurati di averla importata
                        contentDescription = "Go",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}