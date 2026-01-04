package it.polito.did.dcym.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.did.dcym.R
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.ui.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String?,
    onBackClick: () -> Unit,
    onMachineClick: (String) -> Unit,
    viewModel: ProductDetailViewModel = viewModel()
) {
    LaunchedEffect(productId) {
        productId?.toIntOrNull()?.let { viewModel.loadProductData(it) }
    }

    val uiState by viewModel.uiState.collectAsState()
    val product = uiState.product

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Disponibilità prodotto", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF3F0F9)
                )
            )
        },
        bottomBar = { BottomNavBar() },
        containerColor = Color(0xFFF3F0F9)
    ) { paddingValues ->

        if (uiState.isLoading || product == null) {
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
                // 1. TITOLO PRODOTTO
                item {
                    Text(
                        text = product.name,
                        fontSize = 22.sp, // Leggermente più grande
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                    )
                }

                // 2. IMMAGINE GRANDE (MODIFICATA)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp) // Aumentata l'altezza della card
                            .padding(bottom = 24.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = viewModel.getProductImageRes(product.imageResName)),
                                contentDescription = product.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize() // Riempie lo spazio disponibile
                                    .padding(16.dp) // Padding ridotto per farla più grande
                            )
                        }
                    }
                }

                // 3. DESCRIZIONE
                item {
                    Text(
                        text = "Informazioni su questo articolo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = product.description,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 24.dp).fillMaxWidth()
                    )
                }

                // 4. COSTI
                item {
                    Text(
                        text = "Costo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PriceItem(R.drawable.ic_buy, "acquisto", product.pricePurchase)
                        Spacer(modifier = Modifier.width(24.dp))
                        if (product.priceRent != null) {
                            PriceItem(R.drawable.ic_rent, "noleggio", product.priceRent)
                        }
                    }
                }

                // 5. LISTA MACCHINETTE (Con Titolo di Debug)
                item {
                    Text(
                        text = "Disponibile in:", // Titolo aggiunto
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }

                if (uiState.availableMachines.isEmpty()) {
                    item {
                        Text("Nessuna macchinetta disponibile al momento.", color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))
                    }
                } else {
                    items(uiState.availableMachines) { machine ->
                        MachineAvailabilityCard(
                            machine = machine,
                            productId = product.id,
                            onClick = { onMachineClick(machine.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// ... (Le funzioni PriceItem e MachineAvailabilityCard rimangono uguali, non serve ricopiarle se le hai già) ...
@Composable
fun PriceItem(iconRes: Int, label: String, price: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: ${price.toInt()} €",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun MachineAvailabilityCard(
    machine: Machine,
    productId: Int,
    onClick: () -> Unit
) {
    val stock = machine.getStockForProduct(productId)

    Card(
        onClick = onClick,
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
            Column {
                Text(
                    text = machine.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$stock Disponibili",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "100 m",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEBEBEB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_placeholder_vending_machine),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFC4C4C4)
                    )
                }
            }
        }
    }
}