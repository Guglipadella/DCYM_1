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
import it.polito.did.dcym.ui.components.BottomNavBar
import it.polito.did.dcym.ui.components.BottomTab
import it.polito.did.dcym.ui.components.GraphPaperBackground
import it.polito.did.dcym.ui.components.NavBarMode
import it.polito.did.dcym.ui.theme.AppColors

@Composable
fun PlaybackScreen(
    orderId: String,
    onGoToHome: () -> Unit,
    onGoToCatalog: () -> Unit,
    onGoToProfile: () -> Unit,
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
                selectedTab = BottomTab.CATALOGO,
                onFabClick = onGoToHome,
                onTabSelected = { tab ->
                    when(tab) {
                        BottomTab.CATALOGO -> onGoToCatalog()
                        BottomTab.PROFILE -> onGoToProfile()
                        BottomTab.HISTORY -> onGoToHistory()
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ✅ Usa l'helper property invece dell'Enum
                if (uiState.order?.isCompleted == true) {
                    Icon(Icons.Default.Check, null, Modifier.size(100.dp), tint = AppColors.GreenPastelMuted)
                    Text("Prodotto Ritirato!", fontSize = 28.sp, fontWeight = FontWeight.Black)
                } else {
                    Text("Ritira il tuo ordine", fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("Hai 24 ore di tempo.", fontSize = 15.sp, color = Color.Gray)
                    Spacer(Modifier.height(40.dp))

                    Box(
                        modifier = Modifier
                            .background(AppColors.GreenPastelMuted, RoundedCornerShape(12.dp))
                            .border(2.dp, outline, RoundedCornerShape(12.dp))
                            .padding(24.dp)
                    ) {
                        Text("Scade tra: ${uiState.timeLeftString}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                            if (uiState.isPlaying) painterResource(R.drawable.ic_link_connected) else painterResource(android.R.drawable.ic_media_play),
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }
        }
    }
}