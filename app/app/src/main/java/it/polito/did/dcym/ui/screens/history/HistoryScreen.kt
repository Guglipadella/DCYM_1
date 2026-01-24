package it.polito.did.dcym.ui.screens.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.did.dcym.R
import it.polito.did.dcym.data.model.OrderStatus
import it.polito.did.dcym.data.model.OrderType
import it.polito.did.dcym.ui.components.BottomNavBar
import it.polito.did.dcym.ui.components.BottomTab
import it.polito.did.dcym.ui.components.GraphPaperBackground
import it.polito.did.dcym.ui.components.NavBarMode
import it.polito.did.dcym.ui.theme.AppColors

import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    onGoToHomeChoice: () -> Unit,
    onGoToCatalog: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToHelp: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedItemForDetail by remember { mutableStateOf<HistoryItem?>(null) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                mode = NavBarMode.PRODUCT_FLOW,
                selectedTab = BottomTab.HISTORY,
                onFabClick = onGoToHomeChoice,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomTab.CATALOGO -> onGoToCatalog()
                        BottomTab.PROFILE -> onGoToProfile()
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

                Text(
                    text = "I tuoi Acquisti",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(16.dp))
                // --- BARRA DI RICERCA AGGIUNTA QUI ---
                HistorySearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) }
                )

                Spacer(Modifier.height(16.dp))

                // --- FILTRI (Tabs) ---
                HistoryFilters(
                    currentFilter = uiState.filter,
                    onFilterSelect = { viewModel.setFilter(it) }
                )

                Spacer(Modifier.height(16.dp))

                // --- LISTA ---
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.filteredItems.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nessun ordine trovato qui.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(uiState.filteredItems) { item ->
                            HistoryItemCard(
                                item = item,
                                onClick = { selectedItemForDetail = item }
                            )
                        }
                    }
                }
            }
        }

        // --- DIALOG DETTAGLIO (Stile Info Acquisto PDF) ---
        if (selectedItemForDetail != null) {
            val item = selectedItemForDetail!!
            HistoryDetailDialog(
                item = item,
                isPlaying = uiState.isPlayingSound,
                onDismiss = { selectedItemForDetail = null },
                onPlaySound = { viewModel.playSound(item.order.pickupCode) }
            )
        }
    }
}

@Composable
fun HistoryFilters(
    currentFilter: HistoryFilter,
    onFilterSelect: (HistoryFilter) -> Unit
) {
    // Mappatura tra l'Enum logico e l'etichetta visuale
    val filters = listOf(
        HistoryFilter.IN_CORSO to "In Corso",
        HistoryFilter.TUTTI to "Tutti",
        HistoryFilter.NOLEGGI to "Noleggi",
        HistoryFilter.ACQUISTI to "Acquisti"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp) // Piccolo padding laterale per lo scroll
    ) {
        items(filters) { (filter, label) ->
            val isSelected = currentFilter == filter
            // Uso i colori definiti nel tema o custom per dare evidenza alla selezione
            val bg = if (isSelected) AppColors.VioletPastelMuted else MaterialTheme.colorScheme.surface
            val txtColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
            val border = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bg)
                    .border(
                        width = if (isSelected) 0.dp else 2.dp,
                        color = border,
                        shape = CircleShape
                    )
                    .clickable { onFilterSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = txtColor
                )
            }
        }
    }
}
@Composable
fun HistoryItemCard(item: HistoryItem, onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val yellowAction = Color(0xFFFFD54F) // Giallo brand

    // Icone e Colori Stato
    val isRental = item.order.type == OrderType.RENTAL
    val typeIcon = if (isRental) R.drawable.ic_rent else R.drawable.ic_buy

    val statusColor = when(item.order.status) {
        OrderStatus.PENDING -> Color(0xFFE57373) // Rosso
        OrderStatus.ONGOING -> Color(0xFFFFB74D) // Arancione
        else -> AppColors.GreenPastelMuted
    }

    val expirationLabel = when(item.order.status) {
        OrderStatus.PENDING -> "Ritira entro:"
        OrderStatus.ONGOING -> "Restituisci entro:"
        else -> ""
    }

    // Tempo rimanente formattato semplice per la card
    val timeRemaining = if(item.order.status == OrderStatus.PENDING || item.order.status == OrderStatus.ONGOING) {
        formatTimeRemaining(item.order.expirationTimestamp)
    } else ""

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = paper),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp), // Card stondata
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outline, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ICONA TIPO (Buy/Rent)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(typeIcon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(16.dp))

            // INFO CENTRALI
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.machineName, fontSize = 12.sp, color = Color.Gray)

                if (expirationLabel.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(expirationLabel, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    Text(timeRemaining, fontSize = 14.sp, fontWeight = FontWeight.Black, color = statusColor)
                }
            }

            // PREZZO E FRECCIA (Stile Catalogo Corretto)
            Column(horizontalAlignment = Alignment.End) {
                Text("${item.price.toInt()} €", fontWeight = FontWeight.Black, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))

                // --- CORREZIONE QUI: Quadrato Stondato, non Cerchio ---
                Box(
                    modifier = Modifier
                        .size(36.dp) // Leggermente più grande per il touch
                        .background(yellowAction, RoundedCornerShape(12.dp)) // <--- Bordo stondato
                        .border(1.dp, outline, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_right),
                        contentDescription = "Vai al dettaglio",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HistorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text("Cerca una transazione...", color = Color.Gray)
        },
        leadingIcon = {
            Icon(

                painter = painterResource(id = android.R.drawable.ic_menu_search),

                contentDescription = null,

                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp), // Stesso raggio delle card
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent, // Rimuoviamo la linea sotto default di Android
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = modifier
            .fillMaxWidth()
            // Bordo personalizzato per matchare lo stile "tecnico" dell'app
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .shadow(2.dp, RoundedCornerShape(16.dp))
    )
}


@Composable
fun HistoryDetailDialog(
    item: HistoryItem,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onPlaySound: () -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val context = LocalContext.current

    // Risorse Immagine
    val imageResId = remember(item.productImageRes) {
        context.resources.getIdentifier(item.productImageRes, "drawable", context.packageName)
    }

    // Stati
    val isRental = item.order.type == OrderType.RENTAL
    val isPending = item.order.status == OrderStatus.PENDING
    val isOngoing = item.order.status == OrderStatus.ONGOING

    // Calcolo Tabella Rimborsi
    val refundRows = remember(item) {
        if (isRental) {
            val baseTime = if (isPending) System.currentTimeMillis() else item.order.purchaseTimestamp
            calculateDynamicRefunds(item.price, baseTime)
        } else emptyList()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .background(paper, RoundedCornerShape(24.dp))
                .border(2.dp, outline, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // HEADER
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        text = "Info Transazione",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 1. OGGETTO E ICONA
                Image(
                    painter = painterResource(if (imageResId != 0) imageResId else R.drawable.ic_logo_png_dcym),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
                Text(item.productName, fontWeight = FontWeight.Black, fontSize = 22.sp)

                Spacer(Modifier.height(24.dp))

                // 2. GRIGLIA INFORMAZIONI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoRow("Acquistato presso:", item.machineName)
                    InfoRow("Data acquisto:", formatDate(item.order.purchaseTimestamp))
                    InfoRow("Prezzo pagato:", "${String.format("%.2f", item.price)} €")

                    // Tempo Rimanente (Evidenziato)
                    if (isPending || isOngoing) {
                        val label = if (isPending) "Tempo per ritiro:" else "Tempo per restituzione:"
                        val timeColor = if (isPending) Color(0xFFE57373) else Color(0xFFFFB74D)
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, fontSize = 14.sp, color = Color.Gray)
                            Text(
                                formatTimeRemaining(item.order.expirationTimestamp),
                                fontWeight = FontWeight.Black,
                                color = timeColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- 3. AZIONI (SPOSTATO IN ALTO) ---
                // Il pulsante appare subito dopo le info, prima della tabella rimborsi
                if (isPending) {
                    Button(
                        onClick = onPlaySound,
                        enabled = !isPlaying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(2.dp, outline, RoundedCornerShape(16.dp))
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isPlaying) {
                            Text("Riproduzione...", color = MaterialTheme.colorScheme.onSecondary)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("RIPRODUCI CODICE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Avvicina il telefono al microfono", fontSize = 12.sp, color = Color.Gray)

                    // Spazio extra se c'è anche la tabella sotto
                    if (isRental) Spacer(Modifier.height(24.dp))
                }

                // --- 4. TABELLA RIMBORSI (SPOSTATO SOTTO) ---
                if (isRental && (isPending || isOngoing)) {
                    Text(
                        "Valore Rimborso Attuale",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(Modifier.height(8.dp))

                    if (refundRows.isEmpty()) {
                        Text("Tempo scaduto per il rimborso.", color = Color.Red, fontSize = 12.sp)
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, outline, RoundedCornerShape(8.dp))
                        ) {
                            refundRows.forEachIndexed { idx, row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (idx == 0) AppColors.GreenPastelMuted.copy(alpha=0.2f) else Color.Transparent)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(row.label, fontSize = 14.sp, fontWeight = if(idx==0) FontWeight.Bold else FontWeight.Normal)
                                    Text(
                                        "+${String.format("%.2f", row.amount)} €",
                                        fontWeight = FontWeight.Bold,
                                        color = if(idx==0) Color(0xFF2E7D32) else Color.Black
                                    )
                                }
                                if (idx < refundRows.size - 1) Divider(color = Color.LightGray, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper locale per le righe info
@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// --- LOGICA RIMBORSI ---

data class RefundRow(
    val label: String,
    val amount: Double,
    val isActive: Boolean // Indica se è l'opzione corrente/futura
)

fun calculateDynamicRefunds(price: Double, startTime: Long): List<RefundRow> {
    // Percentuali da specifica: 1gg->80%, 2gg->72%, ... 6gg->40%
    val percentages = listOf(0.80, 0.72, 0.64, 0.56, 0.48, 0.40)
    val msPerDay = 24L * 60 * 60 * 1000
    val now = System.currentTimeMillis()

    // Giorni passati dal ritiro (0 = primo giorno, 1 = secondo, ecc.)
    val daysElapsed = ((now - startTime) / msPerDay).toInt().coerceAtLeast(0)

    val rows = mutableListOf<RefundRow>()

    percentages.forEachIndexed { index, percent ->
        // Mostriamo la riga solo se il giorno non è ancora passato irrimediabilmente
        // Esempio: se sono al giorno 2 (daysElapsed=2), i giorni 0 e 1 sono andati.
        // Ma l'utente vuole vedere quanto prende OGGI (che è l'indice corretto) e nei prossimi giorni.
        if (index >= daysElapsed) {
            val amount = price * percent
            // Etichetta amichevole
            val dayNum = index + 1
            val label = if (index == daysElapsed) "Oggi (Entro 24h)" else "Entro il $dayNum° giorno"

            rows.add(RefundRow(label, amount, isActive = true))
        }
    }
    return rows
}

// Formatta timestamp in data leggibile
fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
// Aggiungi questa funzione in fondo al file HistoryScreen.kt

fun formatTimeRemaining(expiryTime: Long): String {
    val diff = expiryTime - System.currentTimeMillis()
    if (diff <= 0) return "Scaduto"

    val hours = diff / (1000 * 60 * 60)
    // Se mancano meno di 24 ore, mostra le ore (es. "4h")
    if (hours < 24) return "${hours}h"

    // Altrimenti mostra i giorni (es. "2gg")
    val days = hours / 24
    return "${days}gg"
}
