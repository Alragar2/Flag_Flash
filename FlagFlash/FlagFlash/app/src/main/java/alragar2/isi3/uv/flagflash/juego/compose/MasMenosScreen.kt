package alragar2.isi3.uv.flagflash.juego.compose

import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.ui.components.FlagFlashButton
import alragar2.isi3.uv.flagflash.ui.theme.*
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MasMenosScreen(
    viewModel: MasMenosViewModel,
    onNavigateBack: () -> Unit,
    onGameFinished: (score: Int, victory: Boolean, timeElapsed: Long, totalCount: Int, guessedCount: Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val bgGradient = Brush.verticalGradient(listOf(SkyBlue, BgLight))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(12.dp)
            ) {
                // Header (Top bar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                    Text(
                        text = if (state.metric == MasMenosMetric.POBLACION) "Más/Menos: Población" else "Más/Menos: Área",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    // Timer (contrarreloj)
                    if (state.timeLeft != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text("⏱️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${state.timeLeft}s",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.timeLeft!! <= 10) RedWrong else DeepSkyBlue,
                                fontFamily = NunitoFamily
                            )
                        }
                    }

                    // Score
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.points),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${state.gameScore}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontFamily = NunitoFamily
                        )
                    }

                    // Active Pet Icon
                    state.activePet?.let { pet ->
                        val petIcon = alragar2.isi3.uv.flagflash.ShopRegistry.getPetImage(pet)
                        petIcon?.let {
                            val alpha by animateFloatAsState(if (state.isPetFed) 1f else 0.4f)
                            Image(
                                painter = painterResource(id = it),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).alpha(alpha)
                            )
                        }
                    }

                    // Lives
                    if (state.timeLeft == null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            repeat(state.maxLives) { index ->
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

                // Main Game Split Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Top Half: Country A (Revealed)
                    state.currentCountry?.let { countryA ->
                        CountryPanel(
                            country = countryA,
                            metric = state.metric,
                            valueText = formatMetricValue(viewModel.getMetricValue(countryA, state.metric), state.metric),
                            isRevealed = true,
                            backgroundColor = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Bottom Half: Country B (Next, Hidden/Revealing)
                    state.nextCountry?.let { countryB ->
                        val valueB = viewModel.getMetricValue(countryB, state.metric)
                        val textB = if (state.revealedValueB) formatMetricValue(valueB, state.metric) else "¿?"
                        
                        val revealBgColor = when (state.isCorrectGuess) {
                            true -> GreenCorrect.copy(alpha = 0.15f)
                            false -> RedWrong.copy(alpha = 0.15f)
                            null -> Color.White.copy(alpha = 0.85f)
                        }

                        CountryPanel(
                            country = countryB,
                            metric = state.metric,
                            valueText = textB,
                            isRevealed = state.revealedValueB,
                            backgroundColor = revealBgColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control Buttons
                if (!state.isGameOver) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // LESS Button
                        FlagFlashButton(
                            text = "MENOS",
                            onClick = { viewModel.makeGuess(guessIsMore = false) },
                            icon = Icons.Default.ArrowDownward,
                            enabled = state.isButtonEnabled,
                            modifier = Modifier.weight(1f),
                            gradientStart = RedWrong,
                            gradientEnd = Color(0xFFDC2626)
                        )

                        // MORE Button
                        FlagFlashButton(
                            text = "MÁS",
                            onClick = { viewModel.makeGuess(guessIsMore = true) },
                            icon = Icons.Default.ArrowUpward,
                            enabled = state.isButtonEnabled,
                            modifier = Modifier.weight(1f),
                            gradientStart = GreenCorrect,
                            gradientEnd = Color(0xFF059669)
                        )
                    }
                } else {
                    // Game Over Banner
                    val bannerBg = if (state.isVictory) {
                        Brush.horizontalGradient(listOf(Color(0xFF86EFAC), Color(0xFF4ADE80)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFFFCA5A5), Color(0xFFF87171)))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(bannerBg)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.isVictory) "¡Victoria! Has completado todas las preguntas 🏆" else "¡Fin del juego! Vuelve a intentarlo.",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlagFlashButton(
                        text = "Ver Resultados",
                        onClick = {
                            onGameFinished(state.score, state.isVictory, state.timeElapsed, state.totalQuestions, state.gameScore / 10)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        gradientStart = DeepSkyBlue,
                        gradientEnd = Color(0xFF1D4ED8)
                    )
                }
            }
        }
    }
}

@Composable
fun CountryPanel(
    country: Map<String, Any>,
    metric: MasMenosMetric,
    valueText: String,
    isRevealed: Boolean,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val flagStr = country["bandera"] as? String
    val name = country["nombre"] as? String ?: ""

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .border(
                width = if (isRevealed && backgroundColor != Color.White.copy(alpha = 0.85f)) 3.dp else 1.dp,
                color = if (isRevealed) {
                    if (backgroundColor.alpha > 0.5f) Color.Transparent else when (valueText) {
                        "¿?" -> Color.LightGray.copy(alpha = 0.5f)
                        else -> Color.Transparent
                    }
                } else Color.LightGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Flag
            val imageModel = getFlagModel(context, flagStr)
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Country Name
            Text(
                text = name,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                fontFamily = NunitoFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Metric Value
            val scale by animateFloatAsState(
                targetValue = if (isRevealed) 1.15f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "revealScale"
            )

            Text(
                text = valueText,
                fontSize = if (valueText == "¿?") 38.sp else 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (valueText == "¿?") TextSecondary else DeepSkyBlue,
                fontFamily = NunitoFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .padding(vertical = 4.dp)
            )
            
            Text(
                text = if (metric == MasMenosMetric.POBLACION) "Población" else "Superficie",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatMetricValue(value: Double, metric: MasMenosMetric): String {
    val formatter = NumberFormat.getInstance(Locale("es", "ES"))
    return when (metric) {
        MasMenosMetric.POBLACION -> {
            "${formatter.format(value.toLong())} hab."
        }
        MasMenosMetric.AREA -> {
            "${formatter.format(value.toLong())} km²"
        }
    }
}

private fun getFlagModel(context: Context, flagStr: String?): Any? {
    if (flagStr == null) return null
    if (flagStr.startsWith("http://") || flagStr.startsWith("https://")) {
        return flagStr
    }
    val resId = context.resources.getIdentifier(flagStr, "drawable", context.packageName)
    if (resId != 0) {
        return resId
    }
    return flagStr
}
