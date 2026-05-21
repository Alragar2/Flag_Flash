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
import androidx.compose.ui.draw.rotate
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Mitad Superior: Jugador 1 (Rotado 180º)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .rotate(180f)
                ) {
                    LocalPlayerHalf(
                        label = "👤 JUGADOR 1",
                        score = state.player1Score,
                        progress = state.correctGuesses.toFloat() / state.totalQuestions,
                        progressText = "${state.correctGuesses}/${state.totalQuestions}",
                        headerColor = DeepSkyBlue,
                        question = state.question,
                        selectedOption = state.selectedByP1,
                        correctOption = state.question?.correctOption,
                        showResult = state.showResult,
                        isCorrect = state.isCorrectP1,
                        enabled = state.selectedByP1 == null && !state.showResult,
                        onAnswer = { viewModel.player1Answer(it) },
                        isImageMode = mode == MultiGameMode.PAIS,
                        mode = mode,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Línea Divisoria Física
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.Gray.copy(alpha = 0.4f))
                )

                // Mitad Inferior: Jugador 2 (Normal)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LocalPlayerHalf(
                        label = "👤 JUGADOR 2",
                        score = state.player2Score,
                        progress = state.correctGuesses.toFloat() / state.totalQuestions,
                        progressText = "${state.correctGuesses}/${state.totalQuestions}",
                        headerColor = Color(0xFF818CF8),
                        question = state.question,
                        selectedOption = state.selectedByP2,
                        correctOption = state.question?.correctOption,
                        showResult = state.showResult,
                        isCorrect = state.isCorrectP2,
                        enabled = state.selectedByP2 == null && !state.showResult,
                        onAnswer = { viewModel.player2Answer(it) },
                        isImageMode = mode == MultiGameMode.PAIS,
                        mode = mode,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalPlayerHalf(
    label: String,
    score: Int,
    progress: Float,
    progressText: String,
    headerColor: Color,
    question: MultiQuestion?,
    selectedOption: String?,
    correctOption: String?,
    showResult: Boolean,
    isCorrect: Boolean?,
    enabled: Boolean,
    onAnswer: (String) -> Unit,
    isImageMode: Boolean,
    mode: MultiGameMode,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header info
            PlayerHeaderStrip(
                label = label,
                score = score,
                progress = progress,
                progressText = progressText,
                color = headerColor
            )

            // Question Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (question != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val modeTitle = when (mode) {
                            MultiGameMode.BANDERA -> "¿A qué país pertenece esta bandera?"
                            MultiGameMode.PAIS -> "¿Cuál es la bandera de este país?"
                            MultiGameMode.CAPITAL -> "¿Cuál es la capital?"
                        }
                        Text(
                            text = modeTitle,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        question.promptImageUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(85.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                        question.promptText?.let { txt ->
                            Text(
                                text = txt,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Feedback Status Text Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showResult && isCorrect != null) {
                    val statusText = if (isCorrect) "¡Correcto! ✅" else "Incorrecto ❌"
                    val statusColor = if (isCorrect) GreenCorrect else RedWrong
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                } else if (selectedOption != null) {
                    Text(
                        text = "Esperando al rival...",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Answer options
            PlayerAnswerSection(
                options = question?.options ?: emptyList(),
                selected = selectedOption,
                correctOption = correctOption,
                showResult = showResult,
                enabled = enabled,
                onAnswer = onAnswer,
                isImageMode = isImageMode
            )
        }
    }
}

@Composable
private fun PlayerHeaderStrip(
    label: String,
    score: Int,
    progress: Float,
    progressText: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 14.sp, fontFamily = NunitoFamily)
            Text("⭐ $score", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
            Text(progressText, fontSize = 13.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun PlayerAnswerSection(
    options: List<String>,
    selected: String?,
    correctOption: String?,
    showResult: Boolean,
    enabled: Boolean,
    onAnswer: (String) -> Unit,
    isImageMode: Boolean
) {
    if (isImageMode) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { opt ->
                val bgColor = optionColor(opt, selected, correctOption, showResult)
                AsyncImage(
                    model = opt,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, bgColor, RoundedCornerShape(8.dp))
                        .clickable(enabled = enabled) { onAnswer(opt) }
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { opt ->
                val bgColor = optionColor(opt, selected, correctOption, showResult)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                        .shadow(2.dp, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .clickable(enabled = enabled) { onAnswer(opt) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

private fun optionColor(opt: String, selected: String?, correct: String?, showResult: Boolean): Color {
    return if (showResult) {
        when {
            opt == correct -> GreenCorrect
            opt == selected -> RedWrong
            else -> DeepSkyBlue.copy(alpha = 0.5f)
        }
    } else {
        if (opt == selected) {
            Color.Gray
        } else {
            DeepSkyBlue
        }
    }
}
