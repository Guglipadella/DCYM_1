package it.polito.did.dcym.ui.screens.help

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.did.dcym.R
import it.polito.did.dcym.ui.components.*

@Composable
fun HelpScreen(
    onGoToHomeChoice: () -> Unit,
    onGoToCatalog: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToHelp: () -> Unit,
    viewModel: HelpViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                mode = NavBarMode.PRODUCT_FLOW,
                selectedTab = BottomTab.HELP,
                hasActiveRentals = uiState.hasActiveRentals,
                onFabClick = onGoToHomeChoice,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomTab.CATALOGO -> onGoToCatalog()
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
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(Modifier.height(20.dp)) }

                item {
                    Text(
                        text = "Aiuto & Tutorial",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // --- MACRO CARD 1: COME FUNZIONA ---
                item {
                    MacroCard(
                        title = "Come funziona Don't Call Your Mom",
                        subtitle = "Sfoglia il catalogo e seleziona ciò che ti serve",
                        // Nota: ic_write non presente in res, uso ic_help come fallback estetico
                        iconRes = R.drawable.ic_write
                    ) {
                        HelpStepItem(R.drawable.ic_store, "Scegli un prodotto", "Sfoglia il catalogo e seleziona ciò che ti serve")
                        HelpStepItem(R.drawable.ic_distributori, "Trova la macchinetta", "Vedi dove è disponibile e quante unità ci sono")
                        HelpStepItem(R.drawable.ic_creditcard, "Acquista o noleggia", "Puoi restituire gli oggetti noleggiati entro la data indicata")
                        HelpStepItem(R.drawable.ic_refound, "Restituisci (se noleggio)", "Prima restituisci, più rimborso ottieni")
                    }
                }

                // --- MACRO CARD 2: RITIRO E CONSEGNA ---
                item {
                    MacroCard(
                        title = "Ritiro e Consegna",
                        iconRes = R.drawable.ic_storico
                    ) {
                        HelpStepItem(R.drawable.ic_openbox, "Ritiro", "Vai alla macchinetta selezionata e segui le istruzioni a schermo")
                        HelpStepItem(R.drawable.ic_rent, "Restituzione", "Usa la stessa macchinetta e riproduci il codice che hai usato per ritirarla")
                    }
                }

                // --- MACRO CARD 3: DOMANDE FREQUENTI ---
                item {
                    Text(
                        text = "Domande Frequenti",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                itemsIndexed(uiState.faqs) { index, faq ->
                    FAQCard(
                        faq = faq,
                        onClick = { viewModel.toggleFaq(index) }
                    )
                }

                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

// -------------------------------------------------------------------------
// COMPONENTI UI RIFINITI
// -------------------------------------------------------------------------

@Composable
fun MacroCard(
    title: String,
    subtitle: String? = null,
    iconRes: Int? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column {
                    Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    if (subtitle != null) {
                        Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun HelpStepItem(iconRes: Int, title: String, subtitle: String) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
        }
    }
}

@Composable
fun FAQCard(faq: FAQItem, onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outline, RoundedCornerShape(20.dp))
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = faq.question,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (faq.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (faq.isExpanded) {
                Spacer(Modifier.height(12.dp))
                Divider(color = outline.copy(alpha = 0.1f))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = faq.answer,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}