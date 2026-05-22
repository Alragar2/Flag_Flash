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
import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.ui.components.FlagFlashButton
import alragar2.isi3.uv.flagflash.ui.theme.*
import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.musica.SoundEffectsManager
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import java.util.Locale

@Composable
fun VictoriaIndividualScreen(
    score: Int,
    timeElapsed: Long,
    mistakes: Int,
    originMode: String,
    continent: String,
    questions: String,
    userPreferences: UserPreferences,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val minutes = timeElapsed / 60
    val seconds = timeElapsed % 60
    
    val questionsCount = remember(questions) {
        questions.toIntOrNull() ?: 10
    }
    
    val precision = remember(questionsCount, mistakes) {
        if (questionsCount + mistakes > 0) {
            (questionsCount.toDouble() / (questionsCount + mistakes) * 100).toInt()
        } else {
            100
        }
    }
    
    val coinsGained = remember(questionsCount, timeElapsed) {
        val baseCoins = questionsCount
        val mult1 = 1.0 + (Math.random() * 0.3) // Entre x1 y x1.3
        val isFast = timeElapsed <= 2 * questionsCount
        val finalMultiplier = if (isFast) {
            val mult2 = 1.7 + (Math.random() * 0.3) // Entre x1.7 y x2
            mult1 + mult2
        } else {
            mult1
        }
        Math.round(baseCoins * finalMultiplier).toInt()
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    LaunchedEffect(Unit) {
        userPreferences.getCoins { c -> userPreferences.setCoins(c + coinsGained) }
        activity?.let { alragar2.isi3.uv.flagflash.InterstitialAdManager.showAdWithProbability(it, 0.4f) }
        SoundEffectsManager.playSound(context, R.raw.win)
    }

    val confettiParty = remember {
        listOf(
            Party(
                emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(60),
                position = Position.Relative(0.5, 0.0)
            )
        )
    }

    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF86EFAC), Color(0xFFBFF3FF)))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        KonfettiView(parties = confettiParty, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🏆", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¡Victoria!",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "¡Excelente conocimiento geográfico!",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Stats cards
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ResultStatCard("⭐ Puntos", "$score", Modifier.weight(1f))
                ResultStatCard("🎯 Precisión", "$precision%", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ResultStatCard("⏱️ Tiempo", String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds), Modifier.weight(1f))
                ResultStatCard("🪙 Ganadas", "+$coinsGained", Modifier.weight(1f))
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
fun DerrotaIndividualScreen(
    score: Int,
    originMode: String,
    continent: String,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val bgGradient = Brush.verticalGradient(listOf(Color(0xFFFCA5A5), Color(0xFFBFF3FF)))
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    LaunchedEffect(Unit) {
        activity?.let { alragar2.isi3.uv.flagflash.InterstitialAdManager.showAdWithProbability(it, 0.4f) }
        SoundEffectsManager.playSound(context, R.raw.game_over)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("💀", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¡Game Over!",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "No te rindas, puedes hacerlo mejor",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))
            ResultStatCard("⭐ Puntos", "$score", Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(32.dp))

            FlagFlashButton(
                text = "Intentar de nuevo",
                onClick = onPlayAgain,
                icon = Icons.Default.Refresh,
                modifier = Modifier.fillMaxWidth(),
                gradientStart = RedWrong,
                gradientEnd = Color(0xFFDC2626)
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
private fun ResultStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
    }
}
