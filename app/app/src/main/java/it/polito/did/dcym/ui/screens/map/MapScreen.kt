package it.polito.did.dcym.ui.screens.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.ui.components.BottomNavBar
import it.polito.did.dcym.ui.components.BottomTab
import it.polito.did.dcym.ui.components.GraphPaperBackground
import it.polito.did.dcym.ui.components.NavBarMode
import it.polito.did.dcym.ui.theme.AppColors

@Composable
fun MapScreen(
    onGoToHomeChoice: () -> Unit,
    onMachineClick: (String) -> Unit,
    onGoToProfile: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToHelp: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val outline = MaterialTheme.colorScheme.outline

    val mapImageRes = remember {
        val id = context.resources.getIdentifier("img_mappa", "drawable", context.packageName)
        if (id != 0) id else R.drawable.ic_placeholder_vending_machine
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                mode = NavBarMode.MACHINE_FLOW,
                selectedTab = BottomTab.MACHINES,
                onFabClick = onGoToHomeChoice,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomTab.MACHINES -> { }
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
                    // Spostiamo il padding orizzontale qui per tutto il contenuto
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // 1. MAPPA INCORNICIATA (Stile Card)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .shadow(6.dp, RoundedCornerShape(24.dp)) // Ombra e stondatura
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                        .border(2.dp, outline, RoundedCornerShape(24.dp)) // Bordo spesso
                        .clip(RoundedCornerShape(24.dp)) // Ritaglia l'immagine dentro il bordo
                ) {
                    Image(
                        painter = painterResource(mapImageRes),
                        contentDescription = "Mappa",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(24.dp))

                // 2. TITOLO
                Text(
                    text = "Trova distributore",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(12.dp))

                // 3. BARRA DI RICERCA
                MapSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) }
                )

                Spacer(Modifier.height(16.dp))

                // 4. SEZIONE POSIZIONE ATTUALE
                CurrentLocationSection()

                Spacer(Modifier.height(16.dp))
                Divider(color = outline.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))

                // 5. LISTA MACCHINETTE
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(uiState.filteredMachines) { machine ->
                            MachineListCard(
                                machine = machine,
                                onClick = { onMachineClick(machine.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Componenti di supporto rimangono uguali ---
@Composable
fun CurrentLocationSection() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Posizione",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "La tua posizione attuale",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Corso Duca degli Abruzzi, 24",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun MachineListCard(machine: Machine, onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    val paper = MaterialTheme.colorScheme.surface
    val iconRes = R.drawable.ic_distributori

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
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                    .border(1.dp, outline.copy(alpha=0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = "Distributore",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = machine.name,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Politecnico di Torino",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MachineStatusPill(isOpen = true)
                    MachineInfoPill(text = "100m", colorBg = Color(0xFFEEEEEE), colorTxt = Color.Black)
                }
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                    .border(2.dp, outline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "Vai",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MapSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text("Cerca macchinetta o zona...", color = Color.Gray, fontSize = 14.sp)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .shadow(2.dp, RoundedCornerShape(16.dp))
    )
}

@Composable
fun MachineStatusPill(isOpen: Boolean) {
    val bg = if (isOpen) AppColors.GreenPastelMuted else Color(0xFFEF9A9A)
    val txtColor = if (isOpen) Color(0xFF1B5E20) else Color(0xFFB71C1C)
    Box(
        modifier = Modifier.background(bg, CircleShape).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.2f), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)
    ) { Text(if (isOpen) "APERTO" else "CHIUSO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = txtColor) }
}

@Composable
fun MachineInfoPill(text: String, colorBg: Color, colorTxt: Color) {
    Box(
        modifier = Modifier.background(colorBg, CircleShape).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.2f), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)
    ) { Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colorTxt) }
}