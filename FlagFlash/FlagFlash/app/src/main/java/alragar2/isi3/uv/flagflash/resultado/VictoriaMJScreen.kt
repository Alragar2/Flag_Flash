package alragar2.isi3.uv.flagflash.resultado

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import alragar2.isi3.uv.flagflash.ui.components.FlagFlashButton
import alragar2.isi3.uv.flagflash.ui.theme.*
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun VictoriaMJScreen(
    player1Score: Int,
    player2Score: Int,
    gameMode: String,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val winner = when {
        player1Score > player2Score -> "Jugador 1"
        player2Score > player1Score -> "Jugador 2"
        else -> null // Empate
    }

    val confettiParty = remember {
        listOf(
            Party(
                emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(50),
                position = Position.Relative(0.5, 0.0)
            )
        )
    }

    val bgGradient = Brush.verticalGradient(
        if (winner != null) listOf(Color(0xFF86EFAC), BgLight)
        else listOf(SkyBlue, BgLight)
    )

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        if (winner != null) {
            KonfettiView(parties = confettiParty, modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(if (winner != null) "🏆" else "🤝", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (winner != null) "¡$winner gana!" else "¡Empate!",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))

            // Score cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreCard(
                    label = "👤 Jugador 1",
                    score = player1Score,
                    isWinner = player1Score > player2Score,
                    modifier = Modifier.weight(1f)
                )
                ScoreCard(
                    label = "👤 Jugador 2",
                    score = player2Score,
                    isWinner = player2Score > player1Score,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            FlagFlashButton(
                text = "Jugar de nuevo",
                onClick = onPlayAgain,
                icon = Icons.Default.Refresh,
                modifier = Modifier.fillMaxWidth(),
                gradientStart = GreenCorrect,
                gradientEnd = Color(0xFF059669)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onMainMenu,
                modifier = Modifier.fillMaxWidth(),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.Default.Home, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Menú principal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ScoreCard(label: String, score: Int, isWinner: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shadow(if (isWinner) 8.dp else 4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isWinner) Gold.copy(alpha = 0.3f) else SurfaceOverlay)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isWinner) Text("🏆", fontSize = 24.sp)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
        Text(
            score.toString(),
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
