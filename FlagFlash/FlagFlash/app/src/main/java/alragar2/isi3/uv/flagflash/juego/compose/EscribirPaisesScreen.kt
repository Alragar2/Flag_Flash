package alragar2.isi3.uv.flagflash.juego.compose

import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.ui.components.FlagFlashButton
import alragar2.isi3.uv.flagflash.ui.theme.*
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscribirPaisesScreen(
    viewModel: EscribirPaisesViewModel,
    onNavigateBack: () -> Unit,
    onGameFinished: (score: Int, victory: Boolean, timeElapsed: Long, totalCount: Int, guessedCount: Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    val bgGradient = Brush.verticalGradient(listOf(SkyBlue, BgLight))

    // Formatear el tiempo restante como MM:SS
    val minutes = state.timeLeft / 60
    val seconds = state.timeLeft % 60
    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                    Text(
                        text = "Escribir: ${state.continent}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sticky Control Panel
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Timer and Progress Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Timer
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏱️", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timeStr,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (state.timeLeft <= 30) RedWrong else DeepSkyBlue,
                                    fontFamily = NunitoFamily
                                )
                            }

                            // Score / Progress text
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${state.guessedCountries.size} / ${state.allCountries.size}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary,
                                    fontFamily = NunitoFamily
                                )
                                Text(
                                    text = "Aciertos",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        val progress = if (state.allCountries.isNotEmpty()) {
                            state.guessedCountries.size.toFloat() / state.allCountries.size
                        } else {
                            0f
                        }
                        LinearProgressIndicator(
                            progress = progress,
                            color = GreenCorrect,
                            trackColor = Color.LightGray.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!state.isGameOver) {
                            // Input field
                            OutlinedTextField(
                                value = state.inputText,
                                onValueChange = { viewModel.onInputChanged(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Escribe el nombre de un país...") },
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true,
                                maxLines = 1,
                                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null, tint = DeepSkyBlue) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DeepSkyBlue,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedContainerColor = BgLight.copy(alpha = 0.5f),
                                    unfocusedContainerColor = BgLight.copy(alpha = 0.2f)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Give up button
                            Button(
                                onClick = { showConfirmDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RedWrong),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Rendirse", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            }
                        } else {
                            // Game finished banner
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
                                    text = if (state.isVictory) "¡Completado con éxito! 🏆" else "¡Fin del tiempo! Revelando países restantes.",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Finish CTA button
                            FlagFlashButton(
                                text = "Ver Resultados",
                                onClick = {
                                    val elapsed = (state.totalTime - state.timeLeft).toLong()
                                    onGameFinished(state.score, state.isVictory, elapsed, state.allCountries.size, state.guessedCountries.size)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                gradientStart = DeepSkyBlue,
                                gradientEnd = Color(0xFF1D4ED8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Grid of Flags
                Text(
                    text = "Banderas en este continente:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.allCountries) { country ->
                        val countryName = country["nombre"] as? String ?: ""
                        val isGuessed = state.guessedCountries.contains(countryName)
                        val isGameOver = state.isGameOver

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isGuessed) Color.White else Color.LightGray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.85f)
                                .border(
                                    width = if (isGuessed) 2.dp else 0.dp,
                                    color = if (isGuessed) GreenCorrect else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Flag Image
                                val imageModel = getFlagModel(context, country["bandera"] as? String)
                                if (imageModel != null) {
                                    AsyncImage(
                                        model = imageModel,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.LightGray.copy(alpha = 0.3f)),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Country Label
                                val labelText: String
                                val labelColor: Color
                                when {
                                    isGuessed -> {
                                        labelText = countryName
                                        labelColor = GreenCorrect
                                    }
                                    isGameOver -> {
                                        labelText = countryName
                                        labelColor = RedWrong
                                    }
                                    else -> {
                                        labelText = "???"
                                        labelColor = Color.DarkGray
                                    }
                                }

                                Text(
                                    text = labelText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = labelColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    lineHeight = 13.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Giving Up
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("¿Rendirse?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que quieres rendirte? Se revelarán las respuestas correctas de todos los países.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.giveUp()
                    }
                ) {
                    Text("Sí, Rendirme", color = RedWrong, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar", color = TextPrimary)
                }
            }
        )
    }
}

/**
 * Resuelve el modelo de imagen para Coil. Soporta tanto URLs remotas como drawables locales.
 */
private fun getFlagModel(context: Context, flagStr: String?): Any? {
    if (flagStr == null) return null
    if (flagStr.startsWith("http://") || flagStr.startsWith("https://")) {
        return flagStr
    }
    // Intentar resolver como drawable local
    val resId = context.resources.getIdentifier(flagStr, "drawable", context.packageName)
    if (resId != 0) {
        return resId
    }
    return flagStr
}
