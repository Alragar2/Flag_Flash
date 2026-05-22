package alragar2.isi3.uv.flagflash.juego.compose

import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.ShopRegistry
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import coil.compose.AsyncImage
import alragar2.isi3.uv.flagflash.ui.theme.*
import coil.request.ImageRequest

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    mode: GameMode,
    onNavigateBack: () -> Unit,
    onGameFinished: (Int, Boolean, Long, Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
            onGameFinished(state.score, state.isVictory, state.timeElapsed, state.mistakes)
        }
    }

    val bgGradient = Brush.verticalGradient(listOf(SkyBlue, BgLight))
    
    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
                // Header: Score and Lives
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.points),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.score.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Active Pet Icon
                    state.activePet?.let { pet ->
                        val petIcon = ShopRegistry.getPetImage(pet)
                        petIcon?.let {
                            val alpha by animateFloatAsState(if (state.isPetFed) 1f else 0.4f)
                            Image(
                                painter = painterResource(id = it),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).alpha(alpha)
                            )
                        }
                    }

                    // Lives or Timer
                    if (state.gameType == GameType.TIME_ATTACK) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱️ ${state.timeLeft ?: 0}s",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if ((state.timeLeft ?: 0) <= 10) RedWrong else TextPrimary
                            )
                        }
                    } else {
                        Row {
                            val maxLives = if (state.gameType == GameType.SURVIVAL) 1 else 3
                            repeat(maxLives) { index ->
                                val isAlive = index < state.lives
                                Image(
                                    painter = painterResource(id = R.drawable.corazon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(horizontal = 2.dp)
                                        .alpha(if (isAlive) 1f else 0.2f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = state.currentQuestionIndex.toFloat() / state.totalQuestions,
                        modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = DeepSkyBlue,
                        trackColor = SkyBlue.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${state.currentQuestionIndex}/${state.totalQuestions}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Question Area
                state.currentQuestion?.let { question ->
                    val title = when (mode) {
                        GameMode.BANDERA -> "¿Qué país es?"
                        GameMode.PAIS -> "¿Qué bandera es?"
                        GameMode.CAPITAL -> "Adivina la capital"
                        GameMode.ESCUDO -> "¿Qué escudo es?"
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (mode == GameMode.PAIS || mode == GameMode.CAPITAL) {
                        Text(
                            text = question.promptText ?: "",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (question.promptImageUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(question.promptImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Result Tick
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        state.showResultTick?.let { isCorrect ->
                            Image(
                                painter = painterResource(id = if (isCorrect) R.drawable.tick_verde else R.drawable.tick_rojo),
                                contentDescription = null,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options Area
                    if (mode == GameMode.PAIS) {
                        // Grid of Flags
                        val cardShape = RoundedCornerShape(12.dp)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(question.options) { option ->
                                Card(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .aspectRatio(1.5f)
                                        .clip(cardShape)
                                        .clickable { viewModel.onOptionSelected(option) },
                                    shape = cardShape,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    AsyncImage(
                                        model = option.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(cardShape),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    } else {
                        // List of Text Buttons
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                            question.options.forEach { option ->
                                Button(
                                    onClick = { viewModel.onOptionSelected(option) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 32.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            state.selectedOption == option && state.showResultTick == true -> GreenCorrect
                                            state.selectedOption == option && state.showResultTick == false -> RedWrong
                                            else -> DeepSkyBlue
                                        }
                                    )
                                ) {
                                    Text(text = option.text ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Espacio extra al final de la columna cuando el búho está activo para evitar solapamiento con el botón flotante
                if (state.activePet == "buho" && state.isPetFed) {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Owl Ability Button
            if (state.activePet == "buho" && state.isPetFed) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    LargeFloatingActionButton(
                        onClick = { viewModel.useOwlAbility() },
                        modifier = Modifier.align(Alignment.BottomEnd),
                        containerColor = Color(0xFFFFEB3B)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.buho),
                            contentDescription = "Owl Ability",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}
