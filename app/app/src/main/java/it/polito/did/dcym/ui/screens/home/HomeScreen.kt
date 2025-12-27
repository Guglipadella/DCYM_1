package it.polito.did.dcym.ui.screens.home


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.polito.did.dcym.R

@Composable
fun HomeScreen(
    onFindProductClick: () -> Unit,
    onFindMachineClick: () -> Unit
) {
    // Colonna principale che centra tutto
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(60
                .dp), // Margine esterno generale
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // 1. IL LOGO (Assicurati di avere logo_dcym.png in res/drawable)
        Image(
            painter = painterResource(id = R.drawable.ic_logo_png_dcym),
            contentDescription = "Logo Don't Call Your Mom",
            modifier = Modifier
                .size(250.dp) // Dimensione approssimativa da Figma
                .padding(bottom = 16.dp)
        )

        // 2. IL TITOLO
        Text(
            text = "Ciao! Cosa vuoi fare?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black // O usa i colori del tuo Theme
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // 3. I DUE PULSANTONI (HomeOptionCard è definito sotto)
        HomeOptionCard(
            text = "Trova un prodotto",
            onClick = onFindProductClick
        )

        Spacer(modifier = Modifier.height(42.dp)) // Spazio tra i bottoni

        HomeOptionCard(
            text = "Trova una macchinetta",
            onClick = onFindMachineClick
        )
    }
}

// COMPONENTE MODULARE PER I PULSANTI
// Lo creiamo qui privato perché serve solo alla Home,
// se servisse altrove lo sposteremmo in ui/components
@Composable
private fun HomeOptionCard(
    text: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth() // Largo quanto lo schermo (meno il padding)
            .height(120.dp), // Altezza fissa per farlo "Cicciotto" come su Figma
        shape = RoundedCornerShape(16.dp), // Arrotondamento angoli
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Ombra
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Sfondo bianco
        )
    ) {
        // Centriamo il testo dentro la Card
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen({}, {})
}