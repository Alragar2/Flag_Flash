package alragar2.isi3.uv.flagflash.juego

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import alragar2.isi3.uv.flagflash.ui.theme.*

/** Single Compose screen that handles all 3 local multiplayer modes */
@Composable
fun MultijugadorLocalScreen(
    viewModel: MultijugadorLocalViewModel,
    mode: MultiGameMode,
    onGameFinished: (player1Score: Int, player2Score: Int) -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Trigger init
    LaunchedEffect(mode) { viewModel.initGame(mode) }

    // Navigate on game over
    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
            onGameFinished(state.player1Score, state.player2Score)
        }
    }

    val bgGradient = Brush.verticalGradient(listOf(SkyBlue, BgLight))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = DeepSkyBlue)
        } else {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

                // Top header: Jugador 1 side
                PlayerHeaderStrip(
                    label = "👤 J1",
                    score = state.player1Score,
                    progress = state.correctGuesses.toFloat() / state.totalQuestions,
                    progressText = "${state.correctGuesses}/${state.totalQuestions}",
                    color = DeepSkyBlue
                )

                // Question area (center)
                state.question?.let { q ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val modeTitle = when (mode) {
                            MultiGameMode.BANDERA -> "¿A qué país pertenece esta bandera?"
                            MultiGameMode.PAIS -> "¿Cuál es la bandera de este país?"
                            MultiGameMode.CAPITAL -> "¿Cuál es la capital?"
                        }
                        Text(modeTitle, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp, textAlign = TextAlign.Center)

                        q.promptImageUrl?.let { url ->
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                        q.promptText?.let { txt ->
                            Text(txt, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = TextPrimary, textAlign = TextAlign.Center)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Result feedback
                        AnimatedVisibility(visible = state.showResult) {
                            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (state.isCorrectP1 == true) "✅ J1 Correcto" else "❌ J1 Fallo",
                                    color = if (state.isCorrectP1 == true) GreenCorrect else RedWrong,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (state.isCorrectP2 == true) "✅ J2 Correcto" else "❌ J2 Fallo",
                                    color = if (state.isCorrectP2 == true) GreenCorrect else RedWrong,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Player 2 options (bottom half, rotated)
                PlayerAnswerSection(
                    label = "JUGADOR 2 ↓",
                    options = state.question?.options ?: emptyList(),
                    selected = state.selectedByP2,
                    correctOption = state.question?.correctOption,
                    showResult = state.showResult,
                    enabled = state.selectedByP2 == null && !state.showResult,
                    onAnswer = { viewModel.player2Answer(it) },
                    isImageMode = mode == MultiGameMode.PAIS
                )

                // Player 2 header
                PlayerHeaderStrip(
                    label = "👤 J2",
                    score = state.player2Score,
                    progress = state.correctGuesses.toFloat() / state.totalQuestions,
                    progressText = "${state.correctGuesses}/${state.totalQuestions}",
                    color = Color(0xFF818CF8),
                    flipped = true
                )

                // Player 1 options (top but shown at bottom of their section)
                PlayerAnswerSection(
                    label = "JUGADOR 1 ↑",
                    options = state.question?.options ?: emptyList(),
                    selected = state.selectedByP1,
                    correctOption = state.question?.correctOption,
                    showResult = state.showResult,
                    enabled = state.selectedByP1 == null && !state.showResult,
                    onAnswer = { viewModel.player1Answer(it) },
                    isImageMode = mode == MultiGameMode.PAIS
                )
            }
        }
    }
}

@Composable
private fun PlayerHeaderStrip(
    label: String,
    score: Int,
    progress: Float,
    progressText: String,
    color: Color,
    flipped: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 14.sp, fontFamily = NunitoFamily)
            Text("⭐ $score", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
            Text(progressText, fontSize = 13.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun PlayerAnswerSection(
    label: String,
    options: List<String>,
    selected: String?,
    correctOption: String?,
    showResult: Boolean,
    enabled: Boolean,
    onAnswer: (String) -> Unit,
    isImageMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        if (isImageMode) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { opt ->
                    val bgColor = optionColor(opt, selected, correctOption, showResult)
                    AsyncImage(
                        model = opt, contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, bgColor, RoundedCornerShape(8.dp))
                            .clickable(enabled = enabled) { onAnswer(opt) }
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { opt ->
                    val bgColor = optionColor(opt, selected, correctOption, showResult)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .shadow(2.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .clickable(enabled = enabled) { onAnswer(opt) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(opt, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }
}

private fun optionColor(opt: String, selected: String?, correct: String?, showResult: Boolean): Color {
    if (showResult || selected != null) {
        if (opt == correct) return GreenCorrect
        if (opt == selected) return RedWrong
    }
    return DeepSkyBlue
}
