package alragar2.isi3.uv.flagflash.online

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import alragar2.isi3.uv.flagflash.ui.components.FlagFlashButton
import alragar2.isi3.uv.flagflash.ui.theme.*
import alragar2.isi3.uv.flagflash.ranking.PlayerAvatar
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────────────────────────
// ONLINE MENU (Create or Join)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun OnlineMenuScreen(
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onBack: () -> Unit
) {
    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF818CF8), BgLight))
    Column(
        modifier = Modifier.fillMaxSize().background(bgGradient),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) }
            Text("🌐 Online", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("🌍", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Multijugador Online", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary, textAlign = TextAlign.Center)
        Text("Hasta 4 jugadores desde cualquier lugar", style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(40.dp))
        Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FlagFlashButton(text = "Crear sala", onClick = onCreateRoom, icon = Icons.Default.Add, modifier = Modifier.fillMaxWidth())
            FlagFlashButton(text = "Unirse a sala", onClick = onJoinRoom, icon = Icons.Default.Login, modifier = Modifier.fillMaxWidth(),
                gradientStart = Color(0xFF818CF8), gradientEnd = Color(0xFF4F46E5))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CREATE ROOM
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CreateRoomScreen(viewModel: OnlineGameViewModel, onRoomCreated: (code: String) -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var selectedMode by remember { mutableStateOf("BANDERA") }
    val modes = listOf(Triple("🚩", "Banderas", "BANDERA"), Triple("🗺️", "País", "PAIS"), Triple("🏙️", "Capitales", "CAPITAL"))

    LaunchedEffect(state.roomCode) {
        if (state.roomCode.isNotEmpty()) onRoomCreated(state.roomCode)
    }

    val bgGradient = Brush.verticalGradient(listOf(SkyBlue, BgLight))
    Column(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) }
            Text("Crear sala", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Modo de juego", fontWeight = FontWeight.Bold, color = TextSecondary, style = MaterialTheme.typography.titleLarge)
            modes.forEach { (emoji, label, mode) ->
                val isSelected = selectedMode == mode
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .shadow(if (isSelected) 6.dp else 2.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) DeepSkyBlue else Color.White)
                        .clickable { selectedMode = mode }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(label, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else TextPrimary, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = DeepSkyBlue)
            } else {
                FlagFlashButton(text = "Crear sala", onClick = { viewModel.createRoom(selectedMode) }, icon = Icons.Default.Add, modifier = Modifier.fillMaxWidth())
            }
            if (state.errorMsg.isNotEmpty()) {
                Text(state.errorMsg, color = RedWrong, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// JOIN ROOM
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun JoinRoomScreen(viewModel: OnlineGameViewModel, onJoined: (code: String) -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var codeInput by remember { mutableStateOf("") }

    LaunchedEffect(state.roomCode) {
        if (state.roomCode.isNotEmpty()) onJoined(state.roomCode)
    }

    val bgGradient = Brush.verticalGradient(listOf(SkyBlue, BgLight))
    Column(
        modifier = Modifier.fillMaxSize().background(bgGradient),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) }
            Text("Unirse a sala", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(modifier = Modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("🔑", fontSize = 64.sp)
            Text("Introduce el código de sala", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = codeInput,
                onValueChange = { if (it.length <= 6) codeInput = it.uppercase() },
                label = { Text("Código de sala") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepSkyBlue, unfocusedBorderColor = SkyBlue)
            )
            if (state.isLoading) {
                CircularProgressIndicator(color = DeepSkyBlue)
            } else {
                FlagFlashButton(text = "Unirse", onClick = { viewModel.joinRoom(codeInput) }, icon = Icons.Default.Login,
                    modifier = Modifier.fillMaxWidth(), enabled = codeInput.length == 6)
            }
            if (state.errorMsg.isNotEmpty()) Text(state.errorMsg, color = RedWrong)
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WAITING ROOM (Lobby)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun WaitingRoomScreen(viewModel: OnlineGameViewModel, onGameStarted: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val room = state.room
    val isHost = room?.hostUid == state.myUid
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(room?.status) {
        if (room?.status == "IN_PROGRESS") onGameStarted()
    }

    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF818CF8), BgLight))
    Column(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.leaveRoom(); onBack() }) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) }
            Text("Sala de espera", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Room code
            Column(
                modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp))
                    .background(Color.White).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Código de sala", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(state.roomCode, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold, color = DeepSkyBlue, letterSpacing = 8.sp)
                    IconButton(onClick = {
                        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Código de Sala", state.roomCode)
                        clipboardManager.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Código copiado", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Share, contentDescription = "Copiar código", tint = DeepSkyBlue)
                    }
                }
                Text("Comparte este código con tus amigos", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
            }

            Text("Jugadores (${room?.players?.size ?: 0}/4)", fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(room?.players?.values?.toList() ?: emptyList()) { player ->
                    Row(
                        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp))
                            .background(Color.White).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlayerAvatar(avatar = player.avatar, frame = player.frame, size = 32.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(player.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        if (player.uid == room?.hostUid) {
                            Badge(containerColor = Gold) { Text("HOST", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
                        }
                    }
                }
            }

            if (isHost) {
                Spacer(modifier = Modifier.height(8.dp))
                FlagFlashButton(
                    text = "Iniciar partida",
                    onClick = { viewModel.startGame() },
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = (room?.players?.size ?: 0) >= 2
                )
                if ((room?.players?.size ?: 0) < 2) {
                    Text("Necesitas al menos 2 jugadores", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DeepSkyBlue, strokeWidth = 2.dp)
                        Text("Esperando al host...", color = TextSecondary)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ONLINE GAME
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun OnlineGameScreen(viewModel: OnlineGameViewModel, onGameOver: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val room = state.room
    val q = state.currentQuestion

    LaunchedEffect(state.isGameOver) { if (state.isGameOver) onGameOver() }

    val bgGradient = Brush.verticalGradient(listOf(SkyBlue, BgLight))
    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        if (q == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = DeepSkyBlue)
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Progress
                val index = room?.questionIndex ?: 0
                val total = room?.totalQuestions ?: 15
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { (index.toFloat() / total).coerceIn(0f, 1f) },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = DeepSkyBlue, trackColor = SkyBlue.copy(0.3f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$index/$total", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scoreboard
                room?.players?.let { players ->
                    LazyColumn(modifier = Modifier.height(100.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(players.values.sortedByDescending { it.score }) { player ->
                            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(if (player.uid == state.myUid) Color(0xFFE0F2FE) else Color.White)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PlayerAvatar(avatar = player.avatar, frame = player.frame, size = 28.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(player.name, fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text("⭐ ${player.score}", fontWeight = FontWeight.ExtraBold, color = DeepSkyBlue)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question
                q.promptText?.let { Text(it, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = TextPrimary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                q.promptImageUrl?.let { url ->
                    AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Result tick
                AnimatedVisibility(visible = state.showResult) {
                    Text(
                        text = if (state.isCorrect == true) "✅ ¡Correcto!" else "❌ Incorrecto",
                        color = if (state.isCorrect == true) GreenCorrect else RedWrong,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Options
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    q.options.forEach { opt ->
                        val selected = state.selectedOption == opt
                        val showResult = state.showResult
                        val isCorrectOpt = opt == q.correctOption
                        val bgColor = when {
                            showResult && isCorrectOpt -> GreenCorrect
                            showResult && selected -> RedWrong
                            else -> DeepSkyBlue
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                                .shadow(4.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(bgColor)
                                .clickable(enabled = state.selectedOption == null) { viewModel.submitAnswer(opt) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (opt.startsWith("http")) {
                                AsyncImage(
                                    model = opt,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                                )
                            } else {
                                Text(opt, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ONLINE RESULT
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun OnlineResultScreen(viewModel: OnlineGameViewModel, onMainMenu: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val room = state.room
    val sorted = room?.players?.values?.sortedByDescending { it.score } ?: emptyList()
    val winner = sorted.firstOrNull()
    val iWon = winner?.uid == state.myUid

    val confetti = remember {
        listOf(Party(emitter = Emitter(3, TimeUnit.SECONDS).perSecond(60), position = Position.Relative(0.5, 0.0)))
    }

    val bgGradient = Brush.verticalGradient(
        if (iWon) listOf(Color(0xFF86EFAC), BgLight) else listOf(SkyBlue, BgLight)
    )

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        if (iWon) KonfettiView(parties = confetti, modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(if (iWon) "🏆" else if (winner?.uid == state.myUid) "🥇" else "🎮", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (iWon) "¡Ganaste!" else "Fin de la partida",
                style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold,
                color = TextPrimary, textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Clasificación final", fontWeight = FontWeight.Bold, color = TextSecondary, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))
            sorted.forEachIndexed { idx, player ->
                val medal = when(idx) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "${idx+1}." }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .shadow(if (player.uid == state.myUid) 6.dp else 2.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (player.uid == state.myUid) Color(0xFFE0F2FE) else Color.White)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(medal, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        PlayerAvatar(avatar = player.avatar, frame = player.frame, size = 32.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(player.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text("⭐ ${player.score}", fontWeight = FontWeight.ExtraBold, color = DeepSkyBlue)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            FlagFlashButton(text = "Menú principal", onClick = onMainMenu, icon = Icons.Default.Home, modifier = Modifier.fillMaxWidth())
        }
    }
}
