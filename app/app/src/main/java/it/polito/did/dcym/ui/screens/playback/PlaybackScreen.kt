package it.polito.did.dcym.ui.screens.playback

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.did.dcym.R
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.ui.components.BottomNavBar
import it.polito.did.dcym.ui.components.BottomTab
import it.polito.did.dcym.ui.components.GraphPaperBackground
import it.polito.did.dcym.ui.components.NavBarMode
import it.polito.did.dcym.ui.theme.AppColors


@Composable
fun PlaybackScreen(
    orderId: String,
    onGoToHomeChoice: () -> Unit,
    onGoToCatalog: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToHelp: () -> Unit,
    onGoToHome: () -> Unit,
    onGoToHistory: () -> Unit,
    viewModel: PlaybackViewModel = viewModel()
) {
    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    val uiState by viewModel.uiState.collectAsState()

    val outline = MaterialTheme.colorScheme.outline

    Scaffold(
        bottomBar = {
            BottomNavBar(
                mode = NavBarMode.PRODUCT_FLOW,
                selectedTab = BottomTab.HISTORY,
                hasActiveRentals = uiState.hasActiveRentals,// Manteniamo focus su storico o home
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // TITOLO
                Text(
                    text = "Codice Ritiro",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Avvicina il telefono al microfono della macchinetta e premi play.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(40.dp))

                // CODICE VISUALE
                val code = uiState.order?.pickupCode ?: "......"
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    code.forEachIndexed { index, char ->
                        val isActive = index == uiState.currentDigitIndex
                        val color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        val textColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(40.dp)
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .background(color, RoundedCornerShape(8.dp))
                                .border(1.dp, outline, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(50.dp))

                // AREA CENTRALE: PLAY O COMPLETATO
                if (uiState.isCompleted) {
                    // Stato Completato
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(160.dp)
                            .background(AppColors.GreenPastelMuted, CircleShape)
                            .border(3.dp, outline, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completato",
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Ritiro completato!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1B5E20))
                } else {
                    // Stato Attivo (Play + Countdown)
                    Box(
                        modifier = Modifier
                            .background(AppColors.GreenPastelMuted, RoundedCornerShape(12.dp))
                            .border(2.dp, outline, RoundedCornerShape(12.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Scade tra: ${uiState.timeLeftString}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                    }

                    Spacer(Modifier.height(50.dp))

                    val scale by animateFloatAsState(if (uiState.isPlaying) 1.1f else 1.0f)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(160.dp)
                            .scale(scale)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            .border(3.dp, outline, CircleShape)
                            .clip(CircleShape)
                            .clickable(enabled = !uiState.isPlaying) { viewModel.playCodeSound() }
                    ) {
                        Icon(
                            // Se ic_link_connected non esiste, usa un'icona standard
                            painter = if (uiState.isPlaying) painterResource(R.drawable.ic_link_connected) else painterResource(android.R.drawable.ic_media_play),
                            contentDescription = "Play",
                            tint = MaterialTheme.colorScheme.onSecondary, // Contrasto corretto
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }
        }
    }
}